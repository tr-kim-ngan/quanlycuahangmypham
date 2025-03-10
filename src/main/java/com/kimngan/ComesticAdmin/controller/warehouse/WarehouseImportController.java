package com.kimngan.ComesticAdmin.controller.warehouse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHangId;
import com.kimngan.ComesticAdmin.entity.DonNhapHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NhaCungCap;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonNhapHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.NhaCungCapService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import jakarta.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/warehouse/import")
public class WarehouseImportController {

	@Autowired
	private DonNhapHangService donNhapHangService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private NhaCungCapService nhaCungCapService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	// Lấy thông tin nhân viên nhập kho hiện tại
	@ModelAttribute("user")
	public NguoiDung getCurrentUser(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	// Trang đăng nhập nhân viên nhập kho
	@GetMapping("/login")
	public String showLoginPage() {
		return "warehouse/import/login";
	}

	// Đăng xuất
	@GetMapping("/logout")
	public String logout() {
		return "redirect:/warehouse/import/login";
	}

	@GetMapping("/purchaseorder")
	public String index(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<DonNhapHang> pageDonNhapHang;

		if (keyword != null && !keyword.isEmpty()) {
			pageDonNhapHang = donNhapHangService.findByNhaCungCap_Ten(keyword,
					PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonNhapHang")));
			model.addAttribute("keyword", keyword);
		} else {
			pageDonNhapHang = donNhapHangService
					.findAllActive(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonNhapHang")));
		}

		Map<Integer, String> formattedTotalValues = new HashMap<>();
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

		Map<Integer, String> formattedNgayNhapHangValues = new HashMap<>();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		for (DonNhapHang donNhapHang : pageDonNhapHang.getContent()) {
			BigDecimal tongGiaTri = donNhapHang.getTongGiaTriNhapHang();
			String formattedValue = tongGiaTri != null ? decimalFormat.format(tongGiaTri) + " VND" : "0.00 VND";
			formattedTotalValues.put(donNhapHang.getMaDonNhapHang(), formattedValue);

			LocalDate ngayNhapHang = donNhapHang.getNgayNhapHang();
			String formattedNgayNhap = ngayNhapHang != null ? ngayNhapHang.format(dateFormatter) : "N/A";
			formattedNgayNhapHangValues.put(donNhapHang.getMaDonNhapHang(), formattedNgayNhap);
		}

		model.addAttribute("formattedTotalValues", formattedTotalValues);
		model.addAttribute("formattedNgayNhapHangValues", formattedNgayNhapHangValues);
		model.addAttribute("listDonNhapHang", pageDonNhapHang.getContent());
		model.addAttribute("currentPage", pageDonNhapHang.getNumber());
		model.addAttribute("totalPages", pageDonNhapHang.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/warehouse/import/purchaseorder");

		return "warehouse/import/index";
	}

	@GetMapping("/purchaseorder/details/{id}")
	public String viewWarehousePurchaseOrderDetails(@PathVariable("id") Integer maDonNhapHang,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Model model) {

		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		if (donNhapHang == null) {
			model.addAttribute("errorMessage", "Không tìm thấy đơn nhập hàng.");
			return "redirect:/warehouse/import/purchaseorder";
		}

		// Phân trang cho chi tiết đơn nhập hàng
		Page<ChiTietDonNhapHang> chiTietPage = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang,
				PageRequest.of(page, size));

		// Lấy toàn bộ danh sách chi tiết để tính tổng giá trị
		List<ChiTietDonNhapHang> allChiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);

		// Định dạng ngày nhập hàng
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String formattedNgayNhap = donNhapHang.getNgayNhapHang().format(formatter);

		// Định dạng đơn giá và tổng giá tiền cho từng chi tiết
		Map<Integer, String> formattedChiTietValues = new HashMap<>();
		Map<Integer, String> formattedTotalPrices = new HashMap<>();
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		BigDecimal totalOrderPrice = BigDecimal.ZERO;

		for (ChiTietDonNhapHang chiTiet : allChiTietList) {
			BigDecimal donGiaNhap = chiTiet.getDonGiaNhap();
			int soLuongNhap = chiTiet.getSoLuongNhap();
			BigDecimal totalPrice = donGiaNhap.multiply(new BigDecimal(soLuongNhap));

			if (chiTietPage.getContent().contains(chiTiet)) {
				String formattedValue = decimalFormat.format(donGiaNhap) + " VND";
				String formattedTotalPrice = decimalFormat.format(totalPrice) + " VND";
				formattedChiTietValues.put(chiTiet.getSanPham().getMaSanPham(), formattedValue);
				formattedTotalPrices.put(chiTiet.getSanPham().getMaSanPham(), formattedTotalPrice);
			}

			totalOrderPrice = totalOrderPrice.add(totalPrice);
		}

		String formattedTotalOrderPrice = decimalFormat.format(totalOrderPrice) + " VND";

		// Gửi dữ liệu xuống view
		model.addAttribute("formattedChiTietValues", formattedChiTietValues);
		model.addAttribute("formattedTotalPrices", formattedTotalPrices);
		model.addAttribute("formattedTotalOrderPrice", formattedTotalOrderPrice);
		model.addAttribute("formattedNgayNhap", formattedNgayNhap);
		model.addAttribute("donNhapHang", donNhapHang);
		model.addAttribute("chiTietPage", chiTietPage);

		return "warehouse/import/detail";
	}

	@GetMapping("/ton-kho")
	public String hienThiTonKho(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "trangThai", required = false, defaultValue = "true") Boolean trangThai) {

		Page<SanPham> pageSanPham;

		if (keyword != null && !keyword.isEmpty()) {
			pageSanPham = sanPhamService.searchAllActiveProductsWithOrderDetails(keyword,
					PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "maSanPham")));
		} else {
			pageSanPham = sanPhamService.findByTrangThai(true,
					PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "maSanPham")));
		}
		Map<Integer, Integer> tongSoLuongNhapMap = new HashMap<>();
		Map<Integer, Integer> soLuongTonKhoMap = new HashMap<>();

		for (SanPham sanPham : pageSanPham.getContent()) {
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPham.getMaSanPham());
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPham.getMaSanPham());

			// Tính lại tổng số lượng nhập: nhập - số lượng có trong chi tiết đơn hàng
			int soLuongNhapThucTe = tongSoLuongNhap - soLuongBan;

			// Tính số lượng tồn kho
			int soLuongTonKho = soLuongNhapThucTe - sanPham.getSoLuong();

			tongSoLuongNhapMap.put(sanPham.getMaSanPham(), soLuongNhapThucTe);
			soLuongTonKhoMap.put(sanPham.getMaSanPham(), soLuongTonKho);
		}

		model.addAttribute("danhSachSanPham", pageSanPham.getContent());
		model.addAttribute("tongSoLuongNhapMap", tongSoLuongNhapMap);
		model.addAttribute("soLuongTonKhoMap", soLuongTonKhoMap);
		model.addAttribute("currentPage", pageSanPham.getNumber());
		model.addAttribute("totalPages", pageSanPham.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("keyword", keyword);
		model.addAttribute("trangThai", trangThai);

		return "warehouse/import/ton-kho";
	}

	// cập nhật số lượng trên kệ
	@PostMapping("/cap-nhat-so-luong")
	public String capNhatSoLuong(@RequestParam("maSanPham") Integer maSanPham,
			@RequestParam("soLuongMoi") Integer soLuongMoi, RedirectAttributes redirectAttributes) {

		// Kiểm tra sản phẩm có tồn tại không
		SanPham sanPham = sanPhamService.findById(maSanPham);
		if (sanPham == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
			return "redirect:/warehouse/import/ton-kho";
		}

		// Tính tổng số lượng nhập từ chi tiết đơn nhập hàng
		int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
		int soLuongTonKho = tongSoLuongNhap - sanPham.getSoLuong(); // Số lượng tồn kho thực tế

		// Ràng buộc: không cho nhập số lượng trên kệ vượt quá tổng số lượng nhập
		if (soLuongMoi > tongSoLuongNhap) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Số lượng trên kệ không thể lớn hơn tổng số lượng nhập (" + tongSoLuongNhap + ").");
			return "redirect:/warehouse/import/ton-kho";
		}

		// Ràng buộc: không cho nhập số lượng âm hoặc vượt quá số lượng tồn kho
		if (soLuongMoi < 0 || soLuongMoi > soLuongTonKho + sanPham.getSoLuong()) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Số lượng trên kệ phải nằm trong khoảng từ 0 đến " + (soLuongTonKho + sanPham.getSoLuong()) + ".");
			return "redirect:/warehouse/import/ton-kho";
		}

		// Cập nhật số lượng trên kệ
		sanPham.setSoLuong(soLuongMoi);
		sanPhamService.update(sanPham);

		// Thông báo thành công
		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số lượng trên kệ thành công.");
		return "redirect:/warehouse/import/ton-kho";
	}

	// Hiển thị danh sách nhà cung cấp
	@GetMapping("/nha-cung-cap")
	public String danhSachNhaCungCap(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<NhaCungCap> pageNhaCungCap = (keyword != null && !keyword.isEmpty())
				? nhaCungCapService.searchByName(keyword,
						PageRequest.of(page, size, Sort.by("maNhaCungCap").descending()))
				: nhaCungCapService.findAllActive(PageRequest.of(page, size, Sort.by("maNhaCungCap").descending()));

		model.addAttribute("listNhaCungCap", pageNhaCungCap.getContent());
		model.addAttribute("currentPage", pageNhaCungCap.getNumber());
		model.addAttribute("totalPages", pageNhaCungCap.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/warehouse/import/nha-cung-cap");
		return "warehouse/import/nha-cung-cap/index";
	}

	// Thêm nhà cung cấp
	@GetMapping("/them-nha-cung-cap")
	public String formThemNhaCungCap(Model model) {
		model.addAttribute("nhaCungCap", new NhaCungCap());
		return "warehouse/import/nha-cung-cap/them";
	}

	@PostMapping("/them-nha-cung-cap")
	public String themNhaCungCap(@ModelAttribute("nhaCungCap") NhaCungCap nhaCungCap, Model model) {
		if (nhaCungCapService.create(nhaCungCap)) {
			return "redirect:/warehouse/import/nha-cung-cap";
		} else {
			model.addAttribute("error", "Tên nhà cung cấp đã tồn tại.");
			return "warehouse/import/nha-cung-cap/them";
		}
	}

	// Trang chỉnh sửa nhà cung cấp
	@GetMapping("/nha-cung-cap/edit/{id}")
	public String formChinhSuaNhaCungCap(@PathVariable("id") Integer id, Model model) {
		Optional<NhaCungCap> nhaCungCapOpt = nhaCungCapService.findByIdOptional(id);
		if (!nhaCungCapOpt.isPresent()) {
			return "redirect:/warehouse/import/nha-cung-cap"; // Nếu không tìm thấy, quay lại danh sách nhà cung cấp
		}

		model.addAttribute("nhaCungCap", nhaCungCapOpt.get());
		return "warehouse/import/nha-cung-cap/edit"; // Trả về trang chỉnh sửa nhà cung cấp
	}

	// Xử lý cập nhật nhà cung cấp
	@PostMapping("/nha-cung-cap/edit/{id}")
	public String capNhatNhaCungCap(@PathVariable("id") Integer id,
			@ModelAttribute("nhaCungCap") NhaCungCap nhaCungCap) {
		Optional<NhaCungCap> existingSupplierOpt = nhaCungCapService.findByIdOptional(id);
		if (!existingSupplierOpt.isPresent()) {
			return "redirect:/warehouse/import/nha-cung-cap"; // Nếu không tìm thấy, quay lại danh sách nhà cung cấp
		}

		NhaCungCap existingSupplier = existingSupplierOpt.get();
		existingSupplier.setTenNhaCungCap(nhaCungCap.getTenNhaCungCap());
		existingSupplier.setSdtNhaCungCap(nhaCungCap.getSdtNhaCungCap());
		existingSupplier.setDiaChiNhaCungCap(nhaCungCap.getDiaChiNhaCungCap());
		existingSupplier.setEmailNhaCungCap(nhaCungCap.getEmailNhaCungCap());

		nhaCungCapService.update(existingSupplier);

		return "redirect:/warehouse/import/nha-cung-cap";
	}

	@PostMapping("/nha-cung-cap1/delete/{id}")
	public String xoaNhaCungCap(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		Optional<NhaCungCap> supplierOpt = nhaCungCapService.findByIdOptional(id);
		if (supplierOpt.isPresent()) {
			NhaCungCap supplier = supplierOpt.get();

			// Kiểm tra xem nhà cung cấp có liên quan đến đơn nhập hàng hay không
			if (supplier.getDonNhapHangs() == null || supplier.getDonNhapHangs().isEmpty()) {
				// Nếu không liên quan đến đơn nhập hàng, xóa hoàn toàn
				nhaCungCapService.deleteById(id);
				redirectAttributes.addFlashAttribute("successMessage", "Nhà cung cấp đã được xóa hoàn toàn.");
			} else {
				// Nếu liên quan đến đơn nhập hàng, chỉ chuyển trạng thái
				supplier.setTrangThai(false);
				nhaCungCapService.update(supplier);
				redirectAttributes.addFlashAttribute("successMessage",
						"Nhà cung cấp đã được chuyển trạng thái không hoạt động.");
			}
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Nhà cung cấp không tồn tại.");
		}

		return "redirect:/warehouse/import/nha-cung-cap";
	}

	// Hiển thị trang thêm đơn nhập hàng
	@GetMapping("/add-purchase-order")
	public String showAddPurchaseOrderPage(Model model) {
		model.addAttribute("providers", nhaCungCapService.findAllActive(PageRequest.of(0, 10)).getContent());

		// Lấy thông tin người dùng đăng nhập
		return "warehouse/import/add"; // Trả về trang thêm đơn nhập hàng
	}

	// Lưu đơn nhập hàng (chưa có chi tiết)
	@PostMapping("/save-purchase-order")
	public String createPurchaseOrder(
			@RequestParam("ngayNhapHang") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate ngayNhapHang,
			@RequestParam("maNhaCungCap") Integer maNhaCungCap, RedirectAttributes redirectAttributes) {

		LocalDate today = LocalDate.now();
		if (ngayNhapHang.isAfter(today)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Ngày nhập hàng không được lớn hơn hôm nay.");
			return "redirect:/warehouse/import/add-purchase-order";
		}

		// Kiểm tra nhà cung cấp
		NhaCungCap nhaCungCap = nhaCungCapService.findById(maNhaCungCap);
		if (nhaCungCap == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Nhà cung cấp không tồn tại.");
			return "redirect:/warehouse/import/add-purchase-order";
		}

		// Tạo đơn nhập hàng với tổng giá trị ban đầu là 0
		DonNhapHang donNhapHang = new DonNhapHang();
		donNhapHang.setNgayNhapHang(ngayNhapHang);
		donNhapHang.setNhaCungCap(nhaCungCap);
		donNhapHang.setTongGiaTriNhapHang(BigDecimal.ZERO);
		donNhapHang.setTrangThai(true);

		donNhapHangService.create(donNhapHang);

		redirectAttributes.addFlashAttribute("successMessage", "Đơn nhập hàng đã được lưu.");
		return "redirect:/warehouse/import/add-purchase-order-detail/" + donNhapHang.getMaDonNhapHang();
	}

	// Hiển thị trang thêm chi tiết đơn nhập hàng
	@GetMapping("/add-purchase-order-detail/{maDonNhapHang}")
	public String showAddPurchaseOrderDetailsPage(@PathVariable("maDonNhapHang") Integer maDonNhapHang, Model model) {
		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		if (donNhapHang == null) {
			model.addAttribute("errorMessage", "Không tìm thấy đơn nhập hàng.");
			return "redirect:/warehouse/import";
		}

		model.addAttribute("sanPhams", sanPhamService.findByTrangThai(true));
		model.addAttribute("donNhapHang", donNhapHang);
		return "warehouse/import/add-details";
	}

	@PostMapping("/save-purchase-order-details/{maDonNhapHang}")
	public String savePurchaseOrderDetails(@PathVariable("maDonNhapHang") Integer maDonNhapHang,
			@RequestParam(value = "sanPhamIds", required = false) List<Integer> sanPhamIds,
			@RequestParam(value = "soLuongNhap", required = false) List<Integer> soLuongNhap,
			@RequestParam(value = "donGiaNhap", required = false) List<BigDecimal> donGiaNhap,
			RedirectAttributes redirectAttributes) {

		System.out.println("=== Bắt đầu lưu chi tiết đơn nhập hàng ===");
		System.out.println("Mã đơn nhập hàng: " + maDonNhapHang);

		// Kiểm tra xem đơn nhập hàng có tồn tại không
		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		if (donNhapHang == null) {
			System.out.println("LỖI: Không tìm thấy đơn nhập hàng");
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn nhập hàng.");
			return "redirect:/warehouse/import";
		}

		// Kiểm tra danh sách sản phẩm
		if (sanPhamIds == null || sanPhamIds.isEmpty()) {
			System.out.println("LỖI: Không có sản phẩm nào được chọn");
			redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một sản phẩm.");
			return "redirect:/warehouse/import/add-purchase-order-detail/" + maDonNhapHang;
		}

		BigDecimal tongGiaTriNhap = donNhapHang.getTongGiaTriNhapHang();

		for (int i = 0; i < sanPhamIds.size(); i++) {
			System.out.println("Lưu chi tiết: Sản phẩm ID " + sanPhamIds.get(i) + ", Số lượng: " + soLuongNhap.get(i)
					+ ", Đơn giá: " + donGiaNhap.get(i) + " VND");

			// Lấy sản phẩm từ database
			SanPham sanPham = sanPhamService.findById(sanPhamIds.get(i));
			if (sanPham == null) {
				System.out.println("LỖI: Không tìm thấy sản phẩm có ID " + sanPhamIds.get(i));
				continue;
			}

			// **Khởi tạo khóa chính tổng hợp**
			ChiTietDonNhapHangId chiTietId = new ChiTietDonNhapHangId(maDonNhapHang, sanPham.getMaSanPham());

			// **Tạo đối tượng chi tiết đơn nhập hàng**
			ChiTietDonNhapHang chiTiet = new ChiTietDonNhapHang();
			chiTiet.setId(chiTietId); // Gán ID trước khi lưu
			chiTiet.setDonNhapHang(donNhapHang);
			chiTiet.setSanPham(sanPham);
			chiTiet.setSoLuongNhap(soLuongNhap.get(i));
			chiTiet.setDonGiaNhap(donGiaNhap.get(i));
			chiTiet.setTrangThai(true);

			// **Cập nhật tổng giá trị nhập hàng**
			BigDecimal giaTriNhap = donGiaNhap.get(i).multiply(new BigDecimal(soLuongNhap.get(i)));
			tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);

			System.out.println("Chi tiết đơn nhập hàng sẽ được lưu: " + chiTiet);

			// **Lưu chi tiết đơn nhập hàng**
			chiTietDonNhapHangService.create(chiTiet);
		}

		// Cập nhật tổng giá trị nhập hàng
		donNhapHang.setTongGiaTriNhapHang(tongGiaTriNhap);
		donNhapHangService.update(donNhapHang);

		System.out.println("=== Hoàn tất lưu chi tiết đơn nhập hàng ===");
		redirectAttributes.addFlashAttribute("successMessage", "Chi tiết đơn nhập hàng đã được lưu.");
		return "redirect:/warehouse/import/purchaseorder";
	}

	@GetMapping("/purchaseorder/export/{id}")
	public void exportToPDF(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
		response.setContentType("application/pdf");
		DonNhapHang donNhapHang = donNhapHangService.findById(id);
		// Định dạng ngày
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		String fileName = "DonNhap_" + donNhapHang.getMaDonNhapHang() + "_"
				+ donNhapHang.getNgayNhapHang().format(formatter) + ".pdf";
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		// Tạo tài liệu PDF
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		// Tải font Unicode
		InputStream fontStream = getClass().getResourceAsStream("/fonts/TIMES.TTF");
		PDFont font = PDType0Font.load(document, fontStream);

		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		// Tiêu đề chính giữa
		contentStream.beginText();
		contentStream.setFont(font, 16); // Font lớn hơn cho tiêu đề
		contentStream.newLineAtOffset(220, 750); // Căn giữa
		contentStream.showText("ĐƠN NHẬP HÀNG");
		contentStream.endText();

		// Hiển thị thông tin chi tiết bên trái
		float infoStartY = 700; // Điều chỉnh khoảng cách xuống dưới
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.newLineAtOffset(50, infoStartY);
		contentStream.setLeading(14.5f);

		// DonNhapHang donNhapHang = donNhapHangService.findById(id);
		// Định dạng ngày
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		contentStream.showText("Mã đơn nhập: " + donNhapHang.getMaDonNhapHang());
		contentStream.newLine();
		contentStream.showText("Ngày nhập: " + donNhapHang.getNgayNhapHang().format(formatter));
		contentStream.newLine();
		contentStream.endText();

		// Hiển thị thông tin nhà cung cấp bên phải
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.newLineAtOffset(270, infoStartY); // Căn lề phải
		contentStream.setLeading(14.5f);

		contentStream.showText("Nhà cung cấp: " + donNhapHang.getNhaCungCap().getTenNhaCungCap());
		contentStream.newLine();
		contentStream.showText("Email: " + donNhapHang.getNhaCungCap().getEmailNhaCungCap());
		contentStream.newLine();
		contentStream.endText();

		// Bắt đầu vẽ bảng
		float startX = 50; // Vị trí bắt đầu của bảng
		float startY = infoStartY - 30; // Vị trí dòng đầu tiên
		float[] columnWidths = { 100, 190, 65, 90, 100 }; // Điều chỉnh cột cho cân đối
		float cellHeight = 60; // Tăng chiều cao để hình ảnh không bị méo
		float headerCellHeight = 40;

		// Tiêu đề bảng
		String[] headers = { "HÌNH ẢNH", "SẢN PHẨM", "SỐ LƯỢNG NHẬP", "ĐƠN GIÁ NHẬP", "TỔNG GIÁ TIỀN" };
		drawRow(contentStream, font, startX, startY, headerCellHeight, columnWidths, headers, null, document);
		startY -= headerCellHeight; // Di chuyển xuống dòng tiếp theo

		// Lấy danh sách chi tiết đơn nhập hàng
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
		// Tính tổng giá trị đơn nhập hàng
		BigDecimal tongGiaTriNhap = chiTietList.stream()
				.map(chiTiet -> chiTiet.getDonGiaNhap().multiply(new BigDecimal(chiTiet.getSoLuongNhap())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		for (ChiTietDonNhapHang chiTiet : chiTietList) {
			String imagePath = "src/main/resources/static/upload/" + chiTiet.getSanPham().getHinhAnh();
			String[] data = { "", // Placeholder cho hình ảnh
					chiTiet.getSanPham().getTenSanPham(), String.valueOf(chiTiet.getSoLuongNhap()),
					decimalFormat.format(chiTiet.getDonGiaNhap()) + " VND",
					decimalFormat.format(chiTiet.getDonGiaNhap().multiply(new BigDecimal(chiTiet.getSoLuongNhap())))
							+ " VND" };
			drawRow(contentStream, font, startX, startY, cellHeight, columnWidths, data, imagePath, document);
			startY -= cellHeight;
		}

		// Hiển thị tổng tiền sau bảng, cách bảng 10px
		contentStream.beginText();
		contentStream.setFont(font, 12);

		// Tính toán vị trí hiển thị tổng tiền ở bên phải
		float totalTextX = startX + columnWidths[0] + columnWidths[1] + 10; // Tổng độ rộng cột
		contentStream.newLineAtOffset(totalTextX, startY - 20); // Vị trí ngoài bảng, bên phải
		contentStream.showText("Tổng tiền phải trả: " + decimalFormat.format(tongGiaTriNhap) + " VND");
		contentStream.endText();

		contentStream.close();

		// Lưu tài liệu
		document.save(response.getOutputStream());
		document.close();
	}

	private void drawRow(PDPageContentStream contentStream, PDFont font, float startX, float startY, float cellHeight,
			float[] columnWidths, String[] content, String imagePath, PDDocument document) throws IOException {
		float currentX = startX;

// Vẽ khung của dòng
		contentStream.setLineWidth(0.75f); // Đường viền rõ hơn
		for (float width : columnWidths) {
			contentStream.addRect(currentX, startY - cellHeight, width, cellHeight);
			currentX += width;
		}
		contentStream.stroke();

		currentX = startX;

		for (int i = 0; i < content.length; i++) {
			if (i == 0 && imagePath != null) {
				// Nếu là cột hình ảnh, vẽ hình
				PDImageXObject image = PDImageXObject.createFromFile(imagePath, document);
				float imageWidth = columnWidths[i] - 10;
				float aspectRatio = (float) image.getHeight() / image.getWidth();
				float imageHeight = imageWidth * aspectRatio;
				if (imageHeight > cellHeight - 10) {
					imageHeight = cellHeight - 10;
					imageWidth = imageHeight / aspectRatio;
				}
				float imageX = currentX + (columnWidths[i] - imageWidth) / 2;
				float imageY = startY - cellHeight + (cellHeight - imageHeight) / 2;
				contentStream.drawImage(image, imageX, imageY, imageWidth, imageHeight);
			} else {
				// Nếu là nội dung văn bản
				float textX = currentX + 5; // Padding
				float textY = startY - 15; // Vị trí bắt đầu text
				contentStream.beginText();
				contentStream.setFont(font, 10);
				contentStream.newLineAtOffset(textX, textY);

				// Xử lý nội dung dài
				List<String> lines = splitTextIntoLines(content[i], columnWidths[i] - 10, font, 10);
				for (String line : lines) {
					contentStream.showText(line);
					contentStream.newLineAtOffset(0, -12); // Xuống dòng
				}
				contentStream.endText();
			}
			currentX += columnWidths[i];
		}
	}

	private List<String> splitTextIntoLines(String text, float maxWidth, PDFont font, int fontSize) throws IOException {
		List<String> lines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder();
		for (String word : text.split(" ")) {
			String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
			float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
			if (textWidth > maxWidth) {
				lines.add(currentLine.toString());
				currentLine = new StringBuilder(word);
			} else {
				currentLine.append(word).append(" ");
			}
		}
		lines.add(currentLine.toString());
		return lines;
	}

	@GetMapping("/thong-ke")
	public String getImportStatistics(
	        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	        Model model) {

	    // Nếu fromDate và toDate không có, thì mặc định là 30 ngày gần nhất
	    if (fromDate == null) {
	        fromDate = LocalDate.now().minusDays(30); // 30 ngày trước
	    }
	    if (toDate == null) {
	        toDate = LocalDate.now(); // Ngày hiện tại
	    }

	    List<Object[]> results = chiTietDonNhapHangService.getImportStatistics(fromDate, toDate);
	    
	    List<String> labels = new ArrayList<>();
	    List<Integer> values = new ArrayList<>();
	    
	    for (Object[] row : results) {
	        if (row[0] != null && row[1] != null) {
	            labels.add(row[0].toString());
	            values.add(((Number) row[1]).intValue());
	        }
	    }

	    List<Object[]> danhSachBaoCao = chiTietDonNhapHangService.getBaoCaoChiTiet(fromDate, toDate);
	    List<Object[]> topSuppliers = chiTietDonNhapHangService.getTopSuppliers(fromDate, toDate);

	    model.addAttribute("labels", labels);
	    model.addAttribute("values", values);
	    model.addAttribute("fromDate", fromDate);
	    model.addAttribute("toDate", toDate);
	    model.addAttribute("danhSachBaoCao", danhSachBaoCao);
	    model.addAttribute("topSuppliers", topSuppliers);

	    return "warehouse/import/thong-ke"; // Trả về file thong-ke.html
	}

}
