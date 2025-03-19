//package com.kimngan.ComesticAdmin.controller.warehouseExport;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
//import com.kimngan.ComesticAdmin.entity.DonHang;
//import com.kimngan.ComesticAdmin.entity.NguoiDung;
//import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
//import com.kimngan.ComesticAdmin.entity.SanPham;
//import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
//import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
//import com.kimngan.ComesticAdmin.services.DonHangService;
//import com.kimngan.ComesticAdmin.services.NguoiDungService;
//import com.kimngan.ComesticAdmin.services.SanPhamService;
//import java.util.Map;
//import java.security.Principal;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.data.domain.*;
//import org.springframework.security.core.Authentication;
//
//@Controller
//@RequestMapping("/warehouse/export") // Định nghĩa đường dẫn riêng cho nhân viên xuất kho
//public class WarehouseExportController {
//	@Autowired
//	private SanPhamService sanPhamService;
//	@Autowired
//	private DonHangService donHangService;
//
//	@Autowired
//	private NguoiDungService nguoiDungService;
//
//	@Autowired
//	private ChiTietDonHangService chiTietDonHangService;
//	@Autowired
//	private ChiTietDonNhapHangService chiTietDonNhapHangService;
//
//	// Lấy thông tin nhân viên xuất kho hiện tại
//	@ModelAttribute("user")
//	public NguoiDung getCurrentUser(Principal principal) {
//		if (principal != null) {
//			return nguoiDungService.findByTenNguoiDung(principal.getName());
//		}
//		return null;
//	}
//
//	@GetMapping("/login")
//	public String showLoginForm() {
//		return "warehouse/export/login";
//	}
//
//	@GetMapping("/logout")
//	public String logout() {
//		return "redirect:/warehouse/export/login";
//	}
//
//	@GetMapping("/orders")
//	public String viewWarehouseOrders(Model model, Authentication authentication) {
//		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
//		NguoiDung nhanVienXuatKho = userDetails.getNguoiDung(); // Nếu có
//
//		List<DonHang> danhSachDonHang = donHangService.getOrdersByStatusAndExportStaff("Chờ xuất kho",
//				nhanVienXuatKho.getMaNguoiDung());
//		model.addAttribute("danhSachDonHang", danhSachDonHang);
//
//		return "warehouse/export/index";
//	}
//
//	// Xem chi tiết đơn hàng cần xuất kho
//	@GetMapping("/orders/{maDonHang}")
//	public String viewOrderDetails(@PathVariable("maDonHang") Integer maDonHang, Model model) {
//		DonHang donHang = donHangService.getDonHangById(maDonHang);
//		if (donHang == null) {
//			return "redirect:/warehouse/export/orders";
//		}
//
//		Map<Integer, Integer> soLuongTrenKeMap = new HashMap<>();
//		Map<Integer, Integer> soLuongTonKhoMap = new HashMap<>();
//
//		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//			SanPham sanPham = chiTiet.getSanPham();
//
//			if (sanPham == null) {
//				System.out.println("⚠ LỖI: Không tìm thấy sản phẩm cho đơn hàng " + donHang.getMaDonHang());
//				continue;
//			}
//
//			int soLuongTrenKe = sanPham.getSoLuong();
//
//			Integer tongSoLuongNhap = chiTietDonNhapHangService
//					.getTotalImportedQuantityBySanPhamId(sanPham.getMaSanPham());
//			Integer soLuongDaBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPham.getMaSanPham());
//
//			tongSoLuongNhap = (tongSoLuongNhap == null) ? 0 : tongSoLuongNhap;
//			soLuongDaBan = (soLuongDaBan == null) ? 0 : soLuongDaBan;
//
//			int soLuongTonKho = tongSoLuongNhap - soLuongDaBan - soLuongTrenKe;
//
//			// Debug log để kiểm tra dữ liệu trên console
//			System.out.println("🛒 Sản phẩm: " + sanPham.getTenSanPham());
//			System.out.println("🔹 Số lượng đặt: " + chiTiet.getSoLuong());
//			System.out.println("📦 Số lượng trên kệ: " + soLuongTrenKe);
//			System.out.println("🏢 Số lượng trong kho: " + soLuongTonKho);
//			System.out.println("------------------------------");
//
//			soLuongTrenKeMap.put(sanPham.getMaSanPham(), soLuongTrenKe);
//			soLuongTonKhoMap.put(sanPham.getMaSanPham(), soLuongTonKho);
//		}
//
//		model.addAttribute("donHang", donHang);
//		model.addAttribute("soLuongTrenKeMap", soLuongTrenKeMap);
//		model.addAttribute("soLuongTonKhoMap", soLuongTonKhoMap);
//
//		return "warehouse/export/view-order";
//	}
//
//	// Xác nhận xuất kho
//	@PostMapping("/orders/{maDonHang}/confirm-export")
//	public String confirmExport(@PathVariable("maDonHang") Integer maDonHang, RedirectAttributes redirectAttributes) {
//		DonHang donHang = donHangService.getDonHangById(maDonHang);
//		if (donHang == null || !"Chờ xuất kho".equals(donHang.getTrangThaiDonHang())) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không hợp lệ hoặc đã xử lý.");
//			return "redirect:/warehouse/export/orders";
//		}
//
//		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//	        SanPham sanPham = chiTiet.getSanPham();
//	        int soLuongDat = chiTiet.getSoLuong();
//
//	        Integer tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPham.getMaSanPham());
//	        Integer soLuongDaBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPham.getMaSanPham());
//
//	        tongSoLuongNhap = (tongSoLuongNhap == null) ? 0 : tongSoLuongNhap;
//	        soLuongDaBan = (soLuongDaBan == null) ? 0 : soLuongDaBan;
//
//	        // **Tồn kho hiện tại** = Tổng nhập - Đã bán - Số lượng trên kệ
//	        int tonKhoHienTai = tongSoLuongNhap - soLuongDaBan - sanPham.getSoLuong();
//
//	        if (tonKhoHienTai < soLuongDat) {
//	            redirectAttributes.addFlashAttribute("errorMessage",
//	                    "Không đủ tồn kho cho sản phẩm: " + sanPham.getTenSanPham());
//	            return "redirect:/warehouse/export/orders";
//	        }
//
//	        // ✅ **Trừ số lượng trong kho và cộng lại vào kệ**
//	        sanPham.setSoLuongTonKho(tonKhoHienTai - soLuongDat);
//	        sanPham.setSoLuong(sanPham.getSoLuong() + soLuongDat);
//	        sanPhamService.update(sanPham);
//	    }
//
//		// Đổi trạng thái đơn hàng thành "Đã xác nhận"
//		donHang.setTrangThaiDonHang("Chờ xác nhận");
//		donHangService.updateDonHang(donHang);
//
//		redirectAttributes.addFlashAttribute("successMessage", "Xác nhận xuất kho thành công.");
//		return "redirect:/warehouse/export/orders";
//	}
//
//	@GetMapping("/orders-confirmed")
//	public String viewConfirmedOrders(Model model, Authentication authentication) {
//	    NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
//	    NguoiDung nhanVienXuatKho = userDetails.getNguoiDung(); // Nếu có
//
//	    // ✅ Lấy cả đơn hàng "Chờ xác nhận" và "Đã xác nhận"
//	    List<DonHang> danhSachDonHang = donHangService.getOrdersByStatusesAndExportStaff(
//	            Arrays.asList("Chờ xác nhận", "Đã xác nhận"), nhanVienXuatKho.getMaNguoiDung());
//	    danhSachDonHang.sort(Comparator.comparing(DonHang::getNgayDat).reversed());
//	    model.addAttribute("danhSachDonHang", danhSachDonHang);
//	    return "warehouse/export/confirmed-orders";
//	}
//
//
//	@GetMapping("/orders-confirmed/{maDonHang}")
//	public String viewConfirmedOrderDetails(@PathVariable("maDonHang") Integer maDonHang, Model model) {
//		DonHang donHang = donHangService.getDonHangById(maDonHang);
//
//		if (donHang == null || (!"Chờ xuất kho".equals(donHang.getTrangThaiDonHang())
//				&& !"Đã xác nhận".equals(donHang.getTrangThaiDonHang()))) {
//			return "redirect:/warehouse/export/orders-confirmed";
//		}
//
//		Map<Integer, Integer> soLuongTrenKeMap = new HashMap<>();
//		Map<Integer, Integer> soLuongTonKhoMap = new HashMap<>();
//
//		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//			SanPham sanPham = chiTiet.getSanPham();
//			if (sanPham == null)
//				continue;
//
//			int soLuongTrenKe = sanPham.getSoLuong();
//			Integer tongSoLuongNhap = chiTietDonNhapHangService
//					.getTotalImportedQuantityBySanPhamId(sanPham.getMaSanPham());
//			Integer soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPham.getMaSanPham());
//
//			tongSoLuongNhap = (tongSoLuongNhap == null) ? 0 : tongSoLuongNhap;
//			soLuongBan = (soLuongBan == null) ? 0 : soLuongBan;
//
//			int soLuongTonKho = tongSoLuongNhap - soLuongBan - soLuongTrenKe;
//
//			soLuongTrenKeMap.put(sanPham.getMaSanPham(), soLuongTrenKe);
//			soLuongTonKhoMap.put(sanPham.getMaSanPham(), soLuongTonKho);
//		}
//
//		model.addAttribute("donHang", donHang);
//		model.addAttribute("soLuongTrenKeMap", soLuongTrenKeMap);
//		model.addAttribute("soLuongTonKhoMap", soLuongTonKhoMap);
//
//		return "warehouse/export/view-confirmed-order";
//	}
//
//}
