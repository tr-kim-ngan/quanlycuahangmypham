package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class OrderController {
	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private DonHangService donHangService;
	@Autowired
	private HoaDonService hoaDonService;
	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;
	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	// Hiển thị danh sách đơn hàng
	@GetMapping("/orders")
	public String getOrders(HttpServletRequest request, Model model,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "status", required = false) String status) {

		if (page < 0) {
			page = 0;
		}
		// Sắp xếp theo mã đơn hàng giảm dần
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonHang"));

		// Thực hiện logic xử lý tìm kiếm và phân trang như bình thường
		// Thực hiện logic xử lý tìm kiếm và phân trang như bình thường
		Page<DonHang> donHangPage = (status != null && !status.equals("all"))
				? donHangService.getDonHangsByStatus(status, pageRequest)
				: donHangService.getAllDonHangs(pageRequest);

		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		Map<Integer, String> formattedTongGiaTriMap = new HashMap<>();
		for (DonHang donHang : donHangPage.getContent()) {
			formattedTongGiaTriMap.put(donHang.getMaDonHang(), decimalFormat.format(donHang.getTongGiaTriDonHang()));
		}

		model.addAttribute("formattedTongGiaTriMap", formattedTongGiaTriMap);

		model.addAttribute("donHangs", donHangPage.getContent());
		model.addAttribute("currentPage", donHangPage.getNumber());
		model.addAttribute("totalPages", donHangPage.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("searchAction", "/admin/orders");

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Thêm requestUri vào model để sử dụng trong header
		model.addAttribute("requestUri", request.getRequestURI());

		return "admin/order/index";
	}

	// Xem chi tiết đơn hàng (cho phép admin sửa trạng thái đơn hàng)
	@GetMapping("/orders/{maDonHang}")
	public String viewOrder(@PathVariable("maDonHang") Integer maDonHang, Model model) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			return "redirect:/admin/orders"; // Nếu đơn hàng không tồn tại, chuyển về danh sách đơn hàng
		}

		// Định dạng số tiền
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

		// Định dạng tổng giá trị và phí vận chuyển
		String formattedTongGiaTri = decimalFormat.format(donHang.getTongGiaTriDonHang());
		String formattedPhiVanChuyen = decimalFormat.format(donHang.getPhiVanChuyen());

		// Định dạng các giá trị trong chi tiết đơn hàng
		List<Map<String, String>> formattedChiTietDonHang = new ArrayList<>();
		// Định dạng các giá trị trong chi tiết đơn hàng

		// Kiểm tra nếu không có chi tiết đơn hàng
		if (donHang.getChiTietDonHangs() == null || donHang.getChiTietDonHangs().isEmpty()) {
			model.addAttribute("error", "Đơn hàng không có chi tiết sản phẩm.");
		} else {
			for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
				if (chiTiet.getSanPham() == null) {
					continue; // Bỏ qua nếu sản phẩm bị null
				}
				Map<String, String> chiTietMap = new HashMap<>();

				chiTietMap.put("maSanPham", String.valueOf(chiTiet.getSanPham().getMaSanPham()));
				chiTietMap.put("hinhAnh", chiTiet.getSanPham().getHinhAnh());
				chiTietMap.put("tenSanPham", chiTiet.getSanPham().getTenSanPham());
				chiTietMap.put("soLuong", String.valueOf(chiTiet.getSoLuong()));
				chiTietMap.put("giaTaiThoiDiemDat", decimalFormat.format(chiTiet.getGiaTaiThoiDiemDat()));

				BigDecimal thanhTien = chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong()));
				chiTietMap.put("thanhTien", decimalFormat.format(thanhTien));

				formattedChiTietDonHang.add(chiTietMap);
			}
		}

		// Gắn danh sách chi tiết đơn hàng vào model
		model.addAttribute("formattedChiTietDonHang", formattedChiTietDonHang);

		// Danh sách trạng thái cố định
		List<String> allStatuses = Arrays.asList("Đang xử lý", "Đã xác nhận", "Đang giao hàng", "Đã hoàn thành",
				"Đã hủy");
		int currentStatusIndex = allStatuses.indexOf(donHang.getTrangThaiDonHang());

		// Kiểm tra trạng thái hợp lệ
		if (currentStatusIndex == -1) {
			model.addAttribute("error", "Trạng thái đơn hàng không hợp lệ.");
			return "admin/order/view"; // Trả về view với thông báo lỗi
		}
		// Danh sách trạng thái tiếp theo
		List<String> nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang());

		// Gắn dữ liệu vào model
		model.addAttribute("donHang", donHang);
		model.addAttribute("formattedTongGiaTri", formattedTongGiaTri);
		model.addAttribute("formattedPhiVanChuyen", formattedPhiVanChuyen);
		model.addAttribute("formattedChiTietDonHang", formattedChiTietDonHang);
		model.addAttribute("nextStatuses", nextStatuses);

		// Lịch sử trạng thái
		model.addAttribute("allStatuses", allStatuses);
		model.addAttribute("currentStatusIndex", currentStatusIndex);
		System.out.println("Danh sách trạng thái: " + allStatuses);
		System.out.println("Chỉ số trạng thái hiện tại: " + currentStatusIndex);

		// Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/order/view"; // Trả về trang view để hiển thị chi tiết đơn hàng và cập nhật trạng thái
	}

	// Cập nhật trạng thái đơn hàng
//	@PostMapping("/orders/{maDonHang}/update-status")
//	public String updateOrderStatus(@PathVariable("maDonHang") Integer maDonHang,
//			@RequestParam("status") String newStatus, RedirectAttributes redirectAttributes) {
//		try {
//			DonHang donHang = donHangService.getDonHangById(maDonHang);
//			if (donHang != null) {
//
//				String currentStatus = donHang.getTrangThaiDonHang();
//				System.out.println("Trạng thái hiện tại: " + currentStatus);
//				System.out.println("Trạng thái muốn cập nhật: " + newStatus);
//				if (!isNextStatusValid(currentStatus, newStatus)) {
//					redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
//					return "redirect:/admin/orders/" + maDonHang; // Chuyển hướng lại trang chi tiết đơn hàng
//				}
//
//				System.out.println("Đang kiểm tra kho hàng trước khi cập nhật trạng thái...");
//
//				// Kiểm tra số lượng tồn kho trước khi xác nhận đơn hàng
//				if ("Đã xác nhận".equals(newStatus)) {
//					for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//						SanPham sanPham = chiTiet.getSanPham();
//						int soLuongYeuCau = chiTiet.getSoLuong();
//
//						// Nếu số lượng yêu cầu lớn hơn số lượng tồn kho
//						if (soLuongYeuCau > sanPham.getSoLuong()) {
//							// Đơn hàng phải hủy vì không đủ tồn kho
//							redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm '" + sanPham.getTenSanPham()
//									+ "' không đủ số lượng tồn kho để xác nhận đơn hàng. Đơn hàng cần phải hủy.");
//							// Lưu trạng thái đơn hàng là "Bị hủy do không đủ hàng" để không cộng lại số
//							// lượng vào kho
//							donHang.setTrangThaiDonHang("Bị hủy do không đủ hàng");
//							donHangService.updateDonHang(donHang);
//							return "redirect:/admin/orders/" + maDonHang; // Quay lại trang chi tiết đơn hàng
//						}
//					}
//
//					// Sau khi kiểm tra đủ tồn kho, tiến hành cập nhật lại số lượng tồn kho
//					for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//						SanPham sanPham = chiTiet.getSanPham();
//						int soLuongYeuCau = chiTiet.getSoLuong();
//
//						// Trừ số lượng đã bán ra khỏi tồn kho
//						int soLuongTonHienTai = sanPham.getSoLuong();
//						sanPham.setSoLuong(soLuongTonHienTai - soLuongYeuCau);
//						sanPhamService.update(sanPham); // Lưu lại sản phẩm với số lượng mới
//					}
//					
//				}
//				if ("Đã xác nhận".equals(newStatus)) {
//				    boolean isOutOfStock = false;
//				    List<String> outOfStockProducts = new ArrayList<>();
//
//				    for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//				        SanPham sanPham = chiTiet.getSanPham();
//				        int soLuongYeuCau = chiTiet.getSoLuong();
//
//				        // Kiểm tra nếu số lượng yêu cầu lớn hơn số lượng còn lại cho phép bán
//				        if (soLuongYeuCau > sanPham.getSoLuong()) {
//				            isOutOfStock = true;
//				            outOfStockProducts.add(sanPham.getTenSanPham());
//				        }
//				    }
//
//				    if (isOutOfStock) {
//				        // Hiển thị thông báo nhưng KHÔNG hủy đơn hàng
//				        String errorMsg = "Không đủ hàng để xác nhận đơn hàng cho sản phẩm: " + String.join(", ", outOfStockProducts);
//				        redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
//				        return "redirect:/admin/orders/" + maDonHang;
//				    } else {
//				        // Nếu đủ hàng, tiến hành trừ số lượng tồn kho
//				        for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//				            SanPham sanPham = chiTiet.getSanPham();
//				            sanPham.setSoLuong(sanPham.getSoLuong() - chiTiet.getSoLuong());
//				            sanPhamService.update(sanPham);
//				        }
//				    }
//				}
//
//
//				// Xử lý khi đơn hàng bị hủy
//				if ("Đã hủy".equals(newStatus)) {
//					// Kiểm tra xem đơn hàng có bị hủy do không đủ tồn kho hay không
//					if (!"Bị hủy do không đủ hàng".equals(donHang.getTrangThaiDonHang())) {
//						// Nếu đơn hàng không bị hủy do thiếu tồn kho, tiến hành cộng lại số lượng tồn
//						// kho
//						for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//							SanPham sanPham = chiTiet.getSanPham();
//							int soLuongHienTai = sanPham.getSoLuong();
//							int soLuongTraLai = chiTiet.getSoLuong();
//							sanPham.setSoLuong(soLuongHienTai + soLuongTraLai);
//							sanPhamService.update(sanPham); // Lưu lại sản phẩm sau khi cộng số lượng
//							 // In ra log kiểm tra sau khi cập nhật số lượng tồn kho
//					        System.out.println("Sản phẩm: " + sanPham.getTenSanPham() + 
//					            " | Số lượng tồn sau khi trừ: " + sanPham.getSoLuong());
//						}
//					}
//				}
//				
//
//				// Tạo hóa đơn khi đơn hàng chuyển sang "Đã hoàn thành"
//				// Tạo hóa đơn khi đơn hàng chuyển sang "Đã hoàn thành"
//				if ("Đã hoàn thành".equals(newStatus)) {
//					System.out.println("Tạo hóa đơn cho đơn hàng: " + maDonHang);
//
//					HoaDon hoaDon = new HoaDon();
//					hoaDon.setDonHang(donHang);
//					hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
//
//					// Sử dụng giá trị gốc BigDecimal để lưu vào cơ sở dữ liệu
//					hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
//
//					// Định dạng số tiền để log hoặc hiển thị
//					DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
//					String formattedTongTien = decimalFormat.format(donHang.getTongGiaTriDonHang());
//					System.out.println("Tổng tiền (định dạng): " + formattedTongTien);
//
//					hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
//					hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
//					hoaDon.setSoDienThoaiNhanHang(donHang.getSdtNhanHang());
//					hoaDon.setTrangThaiThanhToan("Chưa xác nhận"); // Gán giá trị mặc định cho trạng thái thanh toán
//
//					// Lưu hóa đơn vào cơ sở dữ liệu
//					hoaDonService.saveHoaDon(hoaDon);
//				}
//
//				donHang.setTrangThaiDonHang(newStatus);
//				donHangService.updateDonHang(donHang); // Lưu lại đơn hàng với trạng thái mới
//				System.out.println("Trạng thái đơn hàng sau khi cập nhật: " + donHang.getTrangThaiDonHang());
//
//				redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công.");
//
//			} else {
//				redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng để cập nhật.");
//			}
//		} catch (Exception e) {
//			redirectAttributes.addFlashAttribute("errorMessage",
//					"Có lỗi xảy ra khi cập nhật trạng thái đơn hàng: " + e.getMessage());
//		}
//		return "redirect:/admin/orders/" + maDonHang; // Chuyển hướng về danh sách đơn hàng sau khi cập nhật
//	}

//	@PostMapping("/orders/{maDonHang}/update-status")
//	public String updateOrderStatus(@PathVariable("maDonHang") Integer maDonHang,
//	        @RequestParam("status") String newStatus, RedirectAttributes redirectAttributes) {
//	    try {
//	        DonHang donHang = donHangService.getDonHangById(maDonHang);
//	        if (donHang != null) {
//	            System.out.println("Trạng thái hiện tại: " + donHang.getTrangThaiDonHang());
//	            System.out.println("Trạng thái muốn cập nhật: " + newStatus);
//
//	            // Kiểm tra trạng thái hợp lệ
//	            if (!isNextStatusValid(donHang.getTrangThaiDonHang(), newStatus)) {
//	                redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
//	                return "redirect:/admin/orders/" + maDonHang;
//	            }
//
//	            // Chỉ giảm số lượng tồn kho khi đơn hàng được xác nhận thành công
//	            if ("Đã xác nhận".equals(newStatus)) {
//	                for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//	                    SanPham sanPham = chiTiet.getSanPham();
//	                    int soLuongDaBan = chiTiet.getSoLuong();
//	                    
//	                    // Cập nhật số lượng tồn kho theo công thức: tồn kho = nhập vào - số lượng bán ra
//	                    int soLuongTonKhoMoi = sanPham.getSoLuong() - soLuongDaBan;
//	                    sanPham.setSoLuong(soLuongTonKhoMoi);
//
//	                    sanPhamService.update(sanPham);
//	                }
//	            }
//
//	            // Tạo hóa đơn khi đơn hàng được hoàn thành
//	            if ("Đã hoàn thành".equals(newStatus)) {
//	                System.out.println("Tạo hóa đơn cho đơn hàng: " + maDonHang);
//
//	                HoaDon hoaDon = new HoaDon();
//	                hoaDon.setDonHang(donHang);
//	                hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
//	                hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
//	                hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
//	                hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
//	                hoaDon.setSoDienThoaiNhanHang(donHang.getSdtNhanHang());
//	                hoaDon.setTrangThaiThanhToan("Chưa xác nhận");
//
//	                hoaDonService.saveHoaDon(hoaDon);
//	            }
//
//	            // Cập nhật trạng thái đơn hàng
//	            donHang.setTrangThaiDonHang(newStatus);
//	            donHangService.updateDonHang(donHang);
//	            System.out.println("Trạng thái đơn hàng sau khi cập nhật: " + donHang.getTrangThaiDonHang());
//
//	            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công.");
//	        } else {
//	            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng để cập nhật.");
//	        }
//	    } catch (Exception e) {
//	        redirectAttributes.addFlashAttribute("errorMessage",
//	                "Có lỗi xảy ra khi cập nhật trạng thái đơn hàng: " + e.getMessage());
//	    }
//	    return "redirect:/admin/orders/" + maDonHang;
//	}

	@PostMapping("/orders/{maDonHang}/update-status")
	public String updateOrderStatus(@PathVariable("maDonHang") Integer maDonHang,
			@RequestParam("status") String newStatus, RedirectAttributes redirectAttributes) {
		try {
			DonHang donHang = donHangService.getDonHangById(maDonHang);
			if (donHang != null) {
				System.out.println("Trạng thái hiện tại: " + donHang.getTrangThaiDonHang());
				System.out.println("Trạng thái muốn cập nhật: " + newStatus);

				// Kiểm tra trạng thái hợp lệ
				if (!isNextStatusValid(donHang.getTrangThaiDonHang(), newStatus)) {
					redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
					return "redirect:/admin/orders/" + maDonHang;
				}

				// Chỉ giảm số lượng tồn kho khi đơn hàng được xác nhận thành công
				// Chỉ giảm số lượng tồn kho khi đơn hàng được xác nhận thành công
//				if ("Đã xác nhận".equals(newStatus)) {
//				    for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
//				        SanPham sanPham = chiTiet.getSanPham();
//
//				        // Lấy tổng số lượng nhập thực tế từ chi tiết đơn nhập hàng
//				        Integer totalImportedQuantity = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPham.getMaSanPham());
//
//				        // Lấy tổng số lượng đã bán từ chi tiết đơn hàng
//				        Integer totalSoldQuantity = chiTietDonHangService.getSoldQuantityBySanPhamId(sanPham.getMaSanPham());
//
//				        // Kiểm tra null và gán giá trị mặc định
//				        totalImportedQuantity = (totalImportedQuantity != null) ? totalImportedQuantity : 0;
//				        totalSoldQuantity = (totalSoldQuantity != null) ? totalSoldQuantity : 0;
//
//				        // Cập nhật lại số lượng tồn kho theo tổng nhập - tổng bán (Không để bị âm)
//				        int soLuongTonKhoMoi = Math.max(totalImportedQuantity - totalSoldQuantity, 0);
//
//				        // Chỉ cập nhật nếu tồn kho thay đổi
//				        if (sanPham.getSoLuong() != soLuongTonKhoMoi) {
//				            sanPham.setSoLuong(soLuongTonKhoMoi);
//				            sanPhamService.update(sanPham);
//				        }
//				    }
//				}
				// Chỉ giảm số lượng tồn kho khi đơn hàng được xác nhận thành công
				if ("Đã xác nhận".equals(newStatus)) {
				    for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
				        SanPham sanPham = chiTiet.getSanPham();
				        int soLuongDaBan = chiTiet.getSoLuong();

				        // Lấy số lượng tồn hiện tại từ sản phẩm
				        int soLuongTonHienTai = sanPham.getSoLuong();

				        // Cập nhật lại số lượng tồn kho: tồn kho mới = tồn kho hiện tại - số lượng đã bán
				        int soLuongTonKhoMoi = Math.max(soLuongTonHienTai - soLuongDaBan, 0);

				        // Chỉ cập nhật nếu tồn kho thay đổi để tránh update không cần thiết
				        if (sanPham.getSoLuong() != soLuongTonKhoMoi) {
				            sanPham.setSoLuong(soLuongTonKhoMoi);
				            sanPhamService.update(sanPham);
				        }
				    }
				}
				// Nếu admin hủy đơn hàng, số lượng sản phẩm phải được cộng lại
				// Nếu admin hủy đơn hàng, số lượng sản phẩm phải được cộng lại vào kho
				if ("Đã hủy".equals(newStatus)) {
				    for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
				        SanPham sanPham = chiTiet.getSanPham();
				        int soLuongDaBan = chiTiet.getSoLuong();

				        // Lấy số lượng tồn hiện tại từ sản phẩm
				        int soLuongTonHienTai = sanPham.getSoLuong();

				        // Cộng lại số lượng đã bán vào kho khi hủy đơn
				        int soLuongTonKhoMoi = soLuongTonHienTai + soLuongDaBan;

				        // **In ra kiểm tra**
				        System.out.println("Hủy đơn hàng - Cập nhật số lượng tồn:");
				        System.out.println("Sản phẩm: " + sanPham.getTenSanPham());
				        System.out.println("Số lượng trước khi cập nhật: " + soLuongTonHienTai);
				        System.out.println("Số lượng trả lại: " + soLuongDaBan);
				        System.out.println("Số lượng sau khi cập nhật: " + soLuongTonKhoMoi);

				        // Chỉ cập nhật nếu tồn kho thay đổi để tránh update không cần thiết
				        if (sanPham.getSoLuong() != soLuongTonKhoMoi) {
				            sanPham.setSoLuong(soLuongTonKhoMoi);
				            sanPhamService.update(sanPham);
				        }
				    }
				}






				// **Tạo hóa đơn khi đơn hàng được hoàn thành**
				if ("Đã hoàn thành".equals(newStatus)) {
					System.out.println("Tạo hóa đơn cho đơn hàng: " + maDonHang);

					HoaDon hoaDon = new HoaDon();
					hoaDon.setDonHang(donHang);
					hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
					hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
					hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
					hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
					hoaDon.setSoDienThoaiNhanHang(donHang.getSdtNhanHang());
					hoaDon.setTrangThaiThanhToan("Chưa xác nhận");

					hoaDonService.saveHoaDon(hoaDon);
				}

				// Cập nhật trạng thái đơn hàng
				donHang.setTrangThaiDonHang(newStatus);
				donHangService.updateDonHang(donHang);
				System.out.println("Trạng thái đơn hàng sau khi cập nhật: " + donHang.getTrangThaiDonHang());

				redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng để cập nhật.");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Có lỗi xảy ra khi cập nhật trạng thái đơn hàng: " + e.getMessage());
		}
		return "redirect:/admin/orders/" + maDonHang;
	}

	// Trang xác nhận đơn hàng
	@GetMapping("/order/confirm/{id}")
	public String confirmOrder(@PathVariable("id") Integer id, Model model) {
		DonHang donHang = donHangService.getDonHangById(id);
		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		if (donHang == null) {
			return "redirect:/admin/orders"; // Nếu đơn hàng không tồn tại, chuyển về trang danh sách
		}
		// Định dạng số tiền
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		String formattedTongGiaTriDonHang = decimalFormat.format(donHang.getTongGiaTriDonHang());
		String formattedPhiVanChuyen = decimalFormat.format(donHang.getPhiVanChuyen());

		Map<Integer, String> formattedGiaSanPhamMap = new HashMap<>();
		Map<Integer, String> formattedThanhTienMap = new HashMap<>();
		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
			formattedGiaSanPhamMap.put(chiTiet.getSanPham().getMaSanPham(),
					decimalFormat.format(chiTiet.getGiaTaiThoiDiemDat()));
			BigDecimal thanhTien = chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong()));
			formattedThanhTienMap.put(chiTiet.getSanPham().getMaSanPham(), decimalFormat.format(thanhTien));
		}
		// Thêm thông tin người dùng vào model

		model.addAttribute("donHang", donHang);
		model.addAttribute("formattedTongGiaTriDonHang", formattedTongGiaTriDonHang);
		model.addAttribute("formattedPhiVanChuyen", formattedPhiVanChuyen);
		model.addAttribute("formattedGiaSanPhamMap", formattedGiaSanPhamMap);
		model.addAttribute("formattedThanhTienMap", formattedThanhTienMap);

		return "admin/order/confirm";
	}

	// Xác nhận đơn hàng
	@PostMapping("/order/confirm/{id}")
	public String confirmOrder(@PathVariable("id") Integer id, @RequestParam("address") String address,
			@RequestParam("phone") String phone, Model model) {

		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		try {
			donHangService.confirmOrder(id, address, phone);
			return "redirect:/admin/orders";
		} catch (Exception e) {
			model.addAttribute("error", "Có lỗi xảy ra khi xác nhận đơn hàng!");

			return "admin/order/view";
		}
	}

	// Cập nhật trạng thái đơn hàng
	@PostMapping("/order/update")
	public String updateOrder(@ModelAttribute("donHang") DonHang donHang, Model model) {
		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
		try {
			donHangService.updateDonHang(donHang);
			return "redirect:/admin/orders";
		} catch (RuntimeException e) {
			model.addAttribute("error", e.getMessage());

			return "admin/order/view";
		}
	}

	// Kiểm tra trạng thái tiếp theo có hợp lệ không
	// Kiểm tra trạng thái tiếp theo có hợp lệ không
	private boolean isNextStatusValid(String currentStatus, String newStatus) {
		List<String> validNextStatuses = getNextStatuses(currentStatus);
		return validNextStatuses.contains(newStatus);
	}

	@ModelAttribute("getNextStatuses")
	public List<String> getNextStatuses(String currentStatus) {
		if (currentStatus == null) {
			return Collections.emptyList();
		}

		List<String> nextStatuses = new ArrayList<>();
		switch (currentStatus) {
		case "Đang xử lý":
			nextStatuses.add("Đã xác nhận");
			nextStatuses.add("Đã hủy");
			break;
		case "Đã xác nhận":
			nextStatuses.add("Đang giao hàng");
			nextStatuses.add("Đã hủy");
			break;
		case "Đang giao hàng":
			nextStatuses.add("Đã hoàn thành");
			break;
		case "Đã hoàn thành":
		case "Đã hủy":
			break;
		}
		return nextStatuses;
	}

}