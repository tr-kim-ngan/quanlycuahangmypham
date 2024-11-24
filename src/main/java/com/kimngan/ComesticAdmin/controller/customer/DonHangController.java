package com.kimngan.ComesticAdmin.controller.customer;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.GioHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/customer/order")
public class DonHangController {

	@Autowired
	private DonHangService donHangService;

	@Autowired
	private GioHangService gioHangService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;
	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private SanPhamService sanPhamService;

	// Hiển thị danh sách đơn hàng của người dùng hiện tại
	@GetMapping
	public String viewOrders(Principal principal, Model model) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		// Lấy thông tin người dùng hiện tại
		String username = principal.getName();
		List<DonHang> donHangs = donHangService.getOrdersByUser(username);

		model.addAttribute("donHangs", donHangs);
		return "customer/order";
	}



	// Phương thức hiển thị chi tiết đơn hàng
	// Phương thức hiển thị chi tiết đơn hàng
	@GetMapping("/{maDonHang}")
	public String viewOrderDetail(@PathVariable Integer maDonHang, Model model) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		LocalDate today = LocalDate.now();

		// Tính giá trị thành tiền cho từng sản phẩm trong đơn hàng
		Map<ChiTietDonHang, BigDecimal> thanhTienMap = new HashMap<>();
		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
			SanPham sanPham = chiTiet.getSanPham();
			BigDecimal giaSauKhuyenMai = chiTiet.getGiaTaiThoiDiemDat(); // Lấy giá tại thời điểm đặt từ chi tiết đơn
																			// hàng

			// Tính thành tiền cho sản phẩm này
			BigDecimal thanhTien = giaSauKhuyenMai.multiply(BigDecimal.valueOf(chiTiet.getSoLuong()));
			thanhTienMap.put(chiTiet, thanhTien);
		}

		// Đưa `thanhTienMap` vào model để sử dụng trong view
		model.addAttribute("donHang", donHang);
		model.addAttribute("thanhTienMap", thanhTienMap);

		return "customer/order_detail"; // Trả về trang order_detail.html
	}
	// Phương thức hiển thị chi tiết đơn hàng
//    @GetMapping("/{maDonHang}")
//    public String viewOrderDetail(@PathVariable Integer maDonHang, Model model) {
//        DonHang donHang = donHangService.getDonHangById(maDonHang);
//        LocalDate today = LocalDate.now();
//
//        // Tính giá trị thành tiền cho từng sản phẩm trong đơn hàng và định dạng
//        DecimalFormat formatter = new DecimalFormat("#,###.##");
//        List<Map<String, String>> formattedChiTietDonHangs = new ArrayList<>();
//        
//        for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//            Map<String, String> formattedChiTiet = new HashMap<>();
//            formattedChiTiet.put("maSanPham", chiTiet.getSanPham().getMaSanPham().toString());
//            formattedChiTiet.put("tenSanPham", chiTiet.getSanPham().getTenSanPham());
//            formattedChiTiet.put("soLuong", String.valueOf(chiTiet.getSoLuong()));
//            
//            // Định dạng giá tại thời điểm đặt
//            BigDecimal giaTaiThoiDiemDat = chiTiet.getGiaTaiThoiDiemDat();
//            formattedChiTiet.put("giaTaiThoiDiemDat", formatter.format(giaTaiThoiDiemDat) + " VND");
//            
//            // Tính thành tiền và định dạng
//            BigDecimal thanhTien = giaTaiThoiDiemDat.multiply(BigDecimal.valueOf(chiTiet.getSoLuong()));
//            formattedChiTiet.put("thanhTien", formatter.format(thanhTien) + " VND");
//
//            formattedChiTietDonHangs.add(formattedChiTiet);
//        }
//
//        // Đưa `formattedChiTietDonHangs` vào model để sử dụng trong view
//        model.addAttribute("donHang", donHang);
//        model.addAttribute("chiTietDonHangs", formattedChiTietDonHangs);
//
//        return "customer/order_detail"; // Trả về trang order_detail.html
//    }
	@PostMapping("/create")
	public String createOrder(Principal principal, @RequestParam("address") String address,
			@RequestParam("phone") String phone, Model model) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		try {
			String username = principal.getName();
			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(username);
			List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);
			LocalDate today = LocalDate.now();

			// Kiểm tra giỏ hàng có trống không
			if (cartItems.isEmpty()) {
				model.addAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
				return "redirect:/customer/cart";
			}

			// Tạo đối tượng DonHang và thiết lập các thông tin ban đầu
			DonHang donHang = new DonHang();
			donHang.setNguoiDung(currentUser);
			donHang.setDiaChiGiaoHang(address);
			donHang.setSdtNhanHang(phone);
			donHang.setNgayDat(LocalDateTime.now());
			donHang.setTrangThaiDonHang("Đang xử lý");

			BigDecimal tongGiaTriDonHang = BigDecimal.ZERO;
			BigDecimal phiVanChuyen = BigDecimal.valueOf(30000); // Giá trị phí vận chuyển mặc định

			// Duyệt qua các sản phẩm trong giỏ hàng để tính tổng giá trị đơn hàng trước
			for (ChiTietGioHang cartItem : cartItems) {
				// Tính giá tại thời điểm đặt (bao gồm khuyến mãi nếu có)
				BigDecimal giaTaiThoiDiemDat = cartItem.getSanPham().getDonGiaBan();
				Optional<KhuyenMai> highestCurrentKhuyenMai = cartItem.getSanPham().getKhuyenMais().stream()
						.filter(KhuyenMai::getTrangThai)
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				if (highestCurrentKhuyenMai.isPresent()) {
					BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					giaTaiThoiDiemDat = giaTaiThoiDiemDat
							.subtract(giaTaiThoiDiemDat.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				}

				// Tính thành tiền cho sản phẩm này và cộng vào tổng giá trị đơn hàng
				BigDecimal thanhTien = giaTaiThoiDiemDat.multiply(BigDecimal.valueOf(cartItem.getSoLuong()));
				tongGiaTriDonHang = tongGiaTriDonHang.add(thanhTien);
			}

			// Đặt tổng giá trị đơn hàng và phí vận chuyển
			tongGiaTriDonHang = tongGiaTriDonHang.add(phiVanChuyen);
			donHang.setTongGiaTriDonHang(tongGiaTriDonHang);
			donHang.setPhiVanChuyen(phiVanChuyen);

			// Lưu đơn hàng lần đầu vào cơ sở dữ liệu
			donHangService.save(donHang);

			// Duyệt qua các sản phẩm trong giỏ hàng và lưu chi tiết đơn hàng
			for (ChiTietGioHang cartItem : cartItems) {
				// Tạo ID tổng hợp cho chi tiết đơn hàng
				ChiTietDonHangId chiTietDonHangId = new ChiTietDonHangId(donHang.getMaDonHang(),
						cartItem.getSanPham().getMaSanPham());

				// Tính giá tại thời điểm đặt (bao gồm khuyến mãi nếu có)
				BigDecimal giaTaiThoiDiemDat = cartItem.getSanPham().getDonGiaBan();
				Optional<KhuyenMai> highestCurrentKhuyenMai = cartItem.getSanPham().getKhuyenMais().stream()
						.filter(KhuyenMai::getTrangThai)
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				BigDecimal phanTramGiam = BigDecimal.ZERO;
				if (highestCurrentKhuyenMai.isPresent()) {
					phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					giaTaiThoiDiemDat = giaTaiThoiDiemDat
							.subtract(giaTaiThoiDiemDat.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				}

				// Tạo chi tiết đơn hàng
				ChiTietDonHang chiTietDonHang = new ChiTietDonHang(chiTietDonHangId, donHang, cartItem.getSanPham(),
						cartItem.getSoLuong(), giaTaiThoiDiemDat, phanTramGiam);

				// Lưu chi tiết đơn hàng vào cơ sở dữ liệu
				chiTietDonHangService.save(chiTietDonHang);
				// Trừ số lượng sản phẩm
				SanPham sanPham = cartItem.getSanPham();
				int soLuongConLai = sanPham.getSoLuong() - cartItem.getSoLuong();
				if (soLuongConLai < 0) {
					throw new IllegalStateException("Số lượng sản phẩm không đủ để đặt hàng.");
				}
				sanPham.setSoLuong(soLuongConLai);
				sanPhamService.update(sanPham);
			}

			// Xóa giỏ hàng sau khi tạo đơn hàng
			gioHangService.clearCart(currentUser);

			return "redirect:/customer/order"; // Chuyển đến danh sách đơn hàng
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "Không thể tạo đơn hàng: " + e.getMessage());
			return "redirect:/customer/cart";
		}
	}


	// Hủy đơn hàng
	@PostMapping("/cancel")
	public String cancelOrder(@RequestParam("maDonHang") Integer maDonHang, RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);

		if (donHang == null || !"Đang xử lý".equals(donHang.getTrangThaiDonHang())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng này.");
			return "redirect:/customer/order";
		}

		// Cập nhật trạng thái đơn hàng thành "Đã hủy"
		donHang.setTrangThaiDonHang("Đã hủy");
		donHangService.updateDonHang(donHang);

		// Cộng lại số lượng sản phẩm
		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
			SanPham sanPham = chiTiet.getSanPham();
			int soLuongConLai = sanPham.getSoLuong() + chiTiet.getSoLuong();
			sanPham.setSoLuong(soLuongConLai);
			sanPhamService.update(sanPham);
		}

		redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công.");
		return "redirect:/customer/order";
	}

}
