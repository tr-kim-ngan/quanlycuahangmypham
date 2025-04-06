package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
public class CustomerHoaDonController {

	@Autowired
	private HoaDonService hoaDonService;

	@Autowired
	private DonHangService donHangService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private DanhGiaService danhGiaService;

	@ModelAttribute
	public void addAttributes(Model model, Principal principal) {
		if (principal != null) {
			// Lấy tên đăng nhập từ Principal
			String username = principal.getName();

			// Tìm thông tin người dùng
			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(username);

			// Thêm thông tin người dùng và timestamp vào Model
			model.addAttribute("currentUser", currentUser);
			model.addAttribute("timestamp", System.currentTimeMillis()); // Timestamp luôn được cập nhật
		}
	}

	// Xem danh sách hóa đơn
	@GetMapping("/hoadon")
	public String getHoaDons(Model model) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		List<HoaDon> hoaDons = hoaDonService.getHoaDonsByCustomer(username).stream()
				.filter(hoaDon -> "Đã xác nhận".equals(hoaDon.getTrangThaiThanhToan())
						|| "Đã hoàn thành".equals(hoaDon.getTrangThaiThanhToan()))
				.sorted(Comparator.comparing(HoaDon::getMaHoaDon).reversed()) // 🔽 Sắp xếp giảm dần theo mã
				.collect(Collectors.toList());

		model.addAttribute("hoaDons", hoaDons);
		return "customer/hoadon";
	}


	@GetMapping("/hoadon/{maDonHang}")
	public String viewHoaDon(@PathVariable("maDonHang") Integer maDonHang, Model model, Principal principal) {
		// Lấy đơn hàng từ mã đơn hàng
		System.out.println(" của viewRating Lấy đơn hàng với mã: " + maDonHang);
		System.out.println("URL gọi với maDonHang: " + maDonHang);
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			System.out.println("Không tìm thấy đơn hàng với mã: " + maDonHang);
			throw new RuntimeException("Đơn hàng không tồn tại với mã: " + maDonHang);
		}
		System.out.println("của viewRating Lấy hóa đơn liên kết với đơn hàng: " + donHang.getMaDonHang());
		// Lấy hóa đơn từ đơn hàng
		HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
		if (hoaDon == null) {
			model.addAttribute("errorMessage", "Không tìm thấy hóa đơn liên kết với đơn hàng.");
			return "redirect:/customer/hoadon";
		}

		String username = principal.getName();
		NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);

		// Lấy danh sách chi tiết đơn hàng
		List<ChiTietDonHang> chiTietDonHangs = donHang.getChiTietDonHangs();
		model.addAttribute("chiTietDonHangs", chiTietDonHangs);

		// Kiểm tra trạng thái đánh giá cho từng sản phẩm trong đơn hàng
		List<Boolean> danhGiaStatuses = new ArrayList<>();
		for (ChiTietDonHang chiTiet : chiTietDonHangs) {
			boolean daDanhGia = danhGiaService.existsByHoaDonAndSanPhamAndNguoiDung(hoaDon, chiTiet.getSanPham(),
					nguoiDung);
			danhGiaStatuses.add(daDanhGia);
		}

		// ✅ Tính tổng giá trị sản phẩm
		BigDecimal tongGiaTriSanPham = BigDecimal.ZERO;
		Map<ChiTietDonHang, BigDecimal> thanhTienMap = new HashMap<>();

		for (ChiTietDonHang chiTiet : chiTietDonHangs) {
			BigDecimal giaSauKhuyenMai = chiTiet.getGiaTaiThoiDiemDat();
			BigDecimal thanhTien = giaSauKhuyenMai.multiply(BigDecimal.valueOf(chiTiet.getSoLuong()));
			thanhTienMap.put(chiTiet, thanhTien);
			tongGiaTriSanPham = tongGiaTriSanPham.add(thanhTien);
		}
		// ✅ Lấy phí vận chuyển từ đơn hàng
		BigDecimal phiVanChuyen = donHang.getPhiVanChuyen();

		// ✅ Tính tổng giá trị đơn hàng
		BigDecimal tongGiaTriDonHang = tongGiaTriSanPham.add(phiVanChuyen);

		// ✅ Debug log
		System.out.println("💰 Tổng giá trị sản phẩm: " + tongGiaTriSanPham);
		System.out.println("🚚 Phí vận chuyển: " + phiVanChuyen);
		System.out.println("🛒 Tổng giá trị hóa đơn: " + tongGiaTriDonHang);

		// ✅ Định dạng số tiền
		DecimalFormat formatter = new DecimalFormat("#,###.##");
		String formattedTongGiaTriSanPham = formatter.format(tongGiaTriSanPham);
		String formattedPhiVanChuyen = formatter.format(phiVanChuyen);
		String formattedTongGiaTriDonHang = formatter.format(tongGiaTriDonHang);

		// Gửi dữ liệu tới view
		model.addAttribute("hoaDon", hoaDon);
		model.addAttribute("danhGiaStatuses", danhGiaStatuses);
		model.addAttribute("nguoiDung", nguoiDung);
		model.addAttribute("thanhTienMap", thanhTienMap);
		model.addAttribute("tongGiaTriSanPham", formattedTongGiaTriSanPham);
		model.addAttribute("phiVanChuyen", formattedPhiVanChuyen);
		model.addAttribute("tongGiaTriDonHang", formattedTongGiaTriDonHang);
		// Định dạng tổng giá trị
		// DecimalFormat formatter = new DecimalFormat("#,###.##");
		String formattedTotal = formatter.format(hoaDon.getTongTien());
		model.addAttribute("formattedTotal", formattedTotal);

		return "customer/hoadon";
	}

	@GetMapping("/hoadon/{maHoaDon}/danhgia/{maSanPham}")
	public String showRatingForm(@PathVariable("maHoaDon") Integer maHoaDon,
			@PathVariable("maSanPham") Integer maSanPham, Model model, Principal principal) {
		// Lấy thông tin hóa đơn và sản phẩm
		HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
		SanPham sanPham = sanPhamService.findById(maSanPham);

		if (hoaDon == null || sanPham == null) {
			model.addAttribute("errorMessage", "Không tìm thấy hóa đơn hoặc sản phẩm để đánh giá.");
			return "redirect:/customer/hoadon/" + maHoaDon;
		}

		String username = principal.getName();
		NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);

		// Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
		if (danhGiaService.existsByHoaDonAndSanPhamAndNguoiDung(hoaDon, sanPham, nguoiDung)) {
			model.addAttribute("errorMessage", "Bạn đã đánh giá sản phẩm này rồi.");
			return "redirect:/customer/hoadon/" + maHoaDon;
		}

		// Thêm thông tin sản phẩm vào model để hiển thị trên form
		model.addAttribute("hoaDon", hoaDon);
		model.addAttribute("sanPham", sanPham);
		return "customer/rating_form";
	}

	@PostMapping("/hoadon/{maHoaDon}/danhgia/{maSanPham}")
	public String submitRating(@PathVariable("maHoaDon") Integer maHoaDon, @PathVariable("maSanPham") Integer maSanPham,
			@RequestParam("rating") int rating, @RequestParam("comment") String comment, Principal principal,
			RedirectAttributes redirectAttributes) {
		String username = principal.getName();
		NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);

		HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
		if (hoaDon == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn với mã: " + maHoaDon);
			return "redirect:/customer/hoadon";
		}

		SanPham sanPham = sanPhamService.findById(maSanPham);
		if (sanPham == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm với mã: " + maSanPham);
			return "redirect:/customer/hoadon/" + maHoaDon;
		}

		if (danhGiaService.existsByHoaDonAndSanPhamAndNguoiDung(hoaDon, sanPham, nguoiDung)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá sản phẩm này rồi.");
			return "redirect:/customer/hoadon/" + maHoaDon;
		}

		// Tạo đánh giá mới
		DanhGia danhGia = new DanhGia();
		danhGia.setHoaDon(hoaDon);
		danhGia.setSanPham(sanPham);
		danhGia.setNguoiDung(nguoiDung);
		danhGia.setSoSao(rating);
		danhGia.setNoiDung(comment);
		danhGia.setThoiGianDanhGia(LocalDateTime.now());
		danhGiaService.create(danhGia);

		// Chuyển hướng về trang xem đánh giá sau khi đánh giá xong
		redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm.");
		return "redirect:/customer/hoadon/" + maHoaDon + "/danhgia/" + maSanPham + "/view";
	}

	@GetMapping("/hoadon/{maHoaDon}/danhgia/{maSanPham}/view")
	public String viewRating(@PathVariable("maHoaDon") Integer maHoaDon, @PathVariable("maSanPham") Integer maSanPham,
			Model model, Principal principal) {
		String username = principal.getName();
		NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);
		HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
		SanPham sanPham = sanPhamService.findById(maSanPham);

		if (hoaDon == null) {
			model.addAttribute("errorMessage", "Không tìm thấy hóa đơn với mã: " + maHoaDon);
			return "redirect:/customer/hoadon";
		}

		if (sanPham == null) {
			model.addAttribute("errorMessage", "Không tìm thấy sản phẩm với mã: " + maSanPham);
			return "redirect:/customer/hoadon/" + maHoaDon;
		}

		DanhGia danhGia = danhGiaService.findByHoaDonAndSanPhamAndNguoiDung(hoaDon, sanPham, nguoiDung);

		if (danhGia == null) {
			model.addAttribute("errorMessage", "Không tìm thấy đánh giá cho sản phẩm này.");
			return "redirect:/customer/hoadon/" + maHoaDon;
		}

		model.addAttribute("danhGia", danhGia);
		return "customer/view_rating"; // Đảm bảo view này tồn tại và đường dẫn đúng
	}

}
