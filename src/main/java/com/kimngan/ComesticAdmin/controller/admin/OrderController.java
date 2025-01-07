package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

	// Hiển thị danh sách đơn hàng
	@GetMapping("/orders")
	public String getOrders(HttpServletRequest request, Model model,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "status", required = false) String status
			) {
		// Nếu status là null hoặc rỗng, chuyển hướng về /admin/orders mà không kèm tham số nào
//	    if (status == null || status.isEmpty()) {
//	        return "redirect:/admin/orders";
//	    }
		
		 
//		 if ((status == null || status.isEmpty()) && request.getQueryString() != null) {
//		        return "redirect:/admin/orders";
//		    }
		// model.addAttribute("requestUri", request.getRequestURI());
		// Kiểm tra nếu page nhỏ hơn 0 thì đặt lại giá trị page về 0
		if (page < 0) {
			page = 0;
		}

		 // Thực hiện logic xử lý tìm kiếm và phân trang như bình thường
		 Page<DonHang> donHangPage = (status != null && !status.equals("all"))
		            ? donHangService.getDonHangsByStatus(status, PageRequest.of(page, size))
		            : donHangService.getAllDonHangs(PageRequest.of(page, size));


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
		List<String> nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang());

		model.addAttribute("donHang", donHang);
		model.addAttribute("chiTietDonHangList", donHang.getChiTietDonHangs());
		model.addAttribute("nextStatuses", nextStatuses); // Thêm thông tin trạng thái tiếp theo vào model

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/order/view"; // Trả về trang view để hiển thị chi tiết đơn hàng và cập nhật trạng thái
	}

	// Cập nhật trạng thái đơn hàng
	@PostMapping("/orders/{maDonHang}/update-status")
	public String updateOrderStatus(
			@PathVariable("maDonHang") Integer maDonHang,
			@RequestParam("status") String newStatus, 
			RedirectAttributes redirectAttributes) {
		try {
			DonHang donHang = donHangService.getDonHangById(maDonHang);
			if (donHang != null) {

				String currentStatus = donHang.getTrangThaiDonHang();
				System.out.println("Trạng thái hiện tại: " + currentStatus);
	            System.out.println("Trạng thái muốn cập nhật: " + newStatus);
				if (!isNextStatusValid(currentStatus, newStatus)) {
					redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
					return "redirect:/admin/orders/" + maDonHang; // Chuyển hướng lại trang chi tiết đơn hàng
				}

	            System.out.println("Đang kiểm tra kho hàng trước khi cập nhật trạng thái...");

				// Kiểm tra số lượng tồn kho trước khi xác nhận đơn hàng
				if ("Đã xác nhận".equals(newStatus)) {
					for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
						SanPham sanPham = chiTiet.getSanPham();
						int soLuongYeuCau = chiTiet.getSoLuong();

						// Nếu số lượng yêu cầu lớn hơn số lượng tồn kho
						if (soLuongYeuCau > sanPham.getSoLuong()) {
							// Đơn hàng phải hủy vì không đủ tồn kho
							redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm '" + sanPham.getTenSanPham()
									+ "' không đủ số lượng tồn kho để xác nhận đơn hàng. Đơn hàng cần phải hủy.");
							// Lưu trạng thái đơn hàng là "Bị hủy do không đủ hàng" để không cộng lại số
							// lượng vào kho
							donHang.setTrangThaiDonHang("Bị hủy do không đủ hàng");
							donHangService.updateDonHang(donHang);
							return "redirect:/admin/orders/" + maDonHang; // Quay lại trang chi tiết đơn hàng
						}
					}

					// Sau khi kiểm tra đủ tồn kho, tiến hành cập nhật lại số lượng tồn kho
					for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
						SanPham sanPham = chiTiet.getSanPham();
						int soLuongYeuCau = chiTiet.getSoLuong();

						// Trừ số lượng đã bán ra khỏi tồn kho
						int soLuongTonHienTai = sanPham.getSoLuong();
						sanPham.setSoLuong(soLuongTonHienTai - soLuongYeuCau);
						sanPhamService.update(sanPham); // Lưu lại sản phẩm với số lượng mới
					}
				}

				// Xử lý khi đơn hàng bị hủy
				if ("Đã hủy".equals(newStatus)) {
					// Kiểm tra xem đơn hàng có bị hủy do không đủ tồn kho hay không
					if (!"Bị hủy do không đủ hàng".equals(donHang.getTrangThaiDonHang())) {
						// Nếu đơn hàng không bị hủy do thiếu tồn kho, tiến hành cộng lại số lượng tồn
						// kho
						for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
							SanPham sanPham = chiTiet.getSanPham();
							int soLuongHienTai = sanPham.getSoLuong();
							int soLuongTraLai = chiTiet.getSoLuong();
							sanPham.setSoLuong(soLuongHienTai + soLuongTraLai);
							sanPhamService.update(sanPham); // Lưu lại sản phẩm sau khi cộng số lượng
						}
					}
				}

				// Tạo hóa đơn khi đơn hàng chuyển sang "Đã hoàn thành"
				// Tạo hóa đơn khi đơn hàng chuyển sang "Đã hoàn thành"
				if ("Đã hoàn thành".equals(newStatus)) {
					System.out.println("Tạo hóa đơn cho đơn hàng: " + maDonHang);
				    HoaDon hoaDon = new HoaDon();
				    hoaDon.setDonHang(donHang);
				    hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
				    hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
				    hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
				    hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
				    hoaDon.setSoDienThoaiNhanHang(donHang.getSdtNhanHang());
				    hoaDon.setTrangThaiThanhToan("Chưa xác nhận"); // Gán giá trị mặc định cho trạng thái thanh toán

				    // Lưu hóa đơn vào cơ sở dữ liệu
				    hoaDonService.saveHoaDon(hoaDon);
				}


				donHang.setTrangThaiDonHang(newStatus);
				donHangService.updateDonHang(donHang); // Lưu lại đơn hàng với trạng thái mới
				System.out.println("Trạng thái đơn hàng sau khi cập nhật: " + donHang.getTrangThaiDonHang());

				
				redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công.");

			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng để cập nhật.");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Có lỗi xảy ra khi cập nhật trạng thái đơn hàng: " + e.getMessage());
		}
		return "redirect:/admin/orders/" + maDonHang;  // Chuyển hướng về danh sách đơn hàng sau khi cập nhật
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
		// Thêm thông tin người dùng vào model

		model.addAttribute("donHang", donHang);
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
