package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
	private DonHangService donHangService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@GetMapping("/orders")
	public String getOrders(HttpServletRequest request, Model model,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "status", required = false) String status) {

		page = Math.max(page, 0);
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonHang"));
		Page<DonHang> donHangPage = (status != null && !status.equals("all"))
				? donHangService.getDonHangsByStatus(status, pageRequest)
				: donHangService.getAllDonHangs(pageRequest);

		Map<Integer, String> formattedTongGiaTriMap = formatOrderPrices(donHangPage);
		model.addAttribute("formattedTongGiaTriMap", formattedTongGiaTriMap);
		model.addAttribute("donHangs", donHangPage.getContent());
		model.addAttribute("currentPage", donHangPage.getNumber());
		model.addAttribute("totalPages", donHangPage.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("searchAction", "/admin/orders");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/order/index";
	}

	@GetMapping("/orders/{maDonHang}")
	public String viewOrder(@PathVariable("maDonHang") Integer maDonHang, Model model) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			return "redirect:/admin/orders";
		}

		// ✅ Nếu trạng thái cần admin chọn lại shipper, luôn lấy danh sách shipper
		List<NguoiDung> danhSachShipper = new ArrayList<>();
		if ("Giao hàng thất bại".equals(donHang.getTrangThaiChoXacNhan())
				|| "Đã xác nhận".equals(donHang.getTrangThaiDonHang())) {
			danhSachShipper = nguoiDungService.findByRole("SHIPPER");
		}

		model.addAttribute("danhSachShipper", danhSachShipper);
		model.addAttribute("donHang", donHang);

		Integer soLanGiaoThatBai = (donHang.getSoLanGiaoThatBai() == null) ? 0 : donHang.getSoLanGiaoThatBai();
		List<String> nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang(), false, soLanGiaoThatBai);

		List<String> allStatuses = Arrays.asList("Đang xử lý", "Đã xác nhận", "Đang chuẩn bị hàng", "Đang giao hàng",
				"Đã hoàn thành", "Đã hủy");

		List<String> displayedStatuses = allStatuses.stream().filter(status -> !status.contains("(Chờ admin xác nhận)"))
				.collect(Collectors.toList());

		int currentStatusIndex = displayedStatuses.indexOf(donHang.getTrangThaiDonHang());
		if (currentStatusIndex == -1) {
			currentStatusIndex = 0;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		model.addAttribute("formattedTongGiaTri", decimalFormat.format(donHang.getTongGiaTriDonHang()));
		model.addAttribute("formattedPhiVanChuyen", decimalFormat.format(donHang.getPhiVanChuyen()));
		model.addAttribute("formattedChiTietDonHang", formatOrderDetails(donHang));
		model.addAttribute("nextStatuses", nextStatuses);
		model.addAttribute("allStatuses", displayedStatuses);
		model.addAttribute("displayedStatuses", displayedStatuses);
		model.addAttribute("currentStatusIndex", currentStatusIndex);

		return "admin/order/view";
	}

	private Map<Integer, String> formatOrderPrices(Page<DonHang> donHangPage) {
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		return donHangPage.getContent().stream().collect(Collectors.toMap(DonHang::getMaDonHang,
				donHang -> decimalFormat.format(donHang.getTongGiaTriDonHang())));
	}

	private List<Map<String, String>> formatOrderDetails(DonHang donHang) {
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		return donHang.getChiTietDonHangs().stream().filter(chiTiet -> chiTiet.getSanPham() != null).map(chiTiet -> {
			Map<String, String> chiTietMap = new HashMap<>();
			chiTietMap.put("maSanPham", String.valueOf(chiTiet.getSanPham().getMaSanPham()));
			chiTietMap.put("hinhAnh", chiTiet.getSanPham().getHinhAnh());
			chiTietMap.put("tenSanPham", chiTiet.getSanPham().getTenSanPham());
			chiTietMap.put("soLuong", String.valueOf(chiTiet.getSoLuong()));
			chiTietMap.put("giaTaiThoiDiemDat", decimalFormat.format(chiTiet.getGiaTaiThoiDiemDat()));
			chiTietMap.put("thanhTien", decimalFormat
					.format(chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong()))));
			return chiTietMap;
		}).collect(Collectors.toList());
	}

//	private Map<Integer, String> formatOrderItemPrices(DonHang donHang) {
//		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
//		return donHang.getChiTietDonHangs().stream()
//				.collect(Collectors.toMap(chiTiet -> chiTiet.getSanPham().getMaSanPham(),
//						chiTiet -> decimalFormat.format(chiTiet.getGiaTaiThoiDiemDat())));
//	}
//
//	private Map<Integer, String> formatOrderItemTotals(DonHang donHang) {
//		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
//		return donHang.getChiTietDonHangs().stream()
//				.collect(Collectors.toMap(chiTiet -> chiTiet.getSanPham().getMaSanPham(), chiTiet -> decimalFormat
//						.format(chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong())))));
//	}

//	private NguoiDungDetails getAuthenticatedUser() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		return (NguoiDungDetails) authentication.getPrincipal();
//	}

	@PostMapping("/orders/{maDonHang}/assign-shipper")
	public String assignShipper(@PathVariable("maDonHang") Integer maDonHang,
			@RequestParam("shipperId") Integer shipperId, RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);

		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
			return "redirect:/admin/orders";
		}

		NguoiDung shipper = nguoiDungService.findById(shipperId);
		if (shipper == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy shipper!");
			return "redirect:/admin/orders/" + maDonHang;
		}

		// **Kiểm tra shipper và trạng thái trước khi cập nhật**
		System.out.println("💡 [DEBUG] Gán shipper: " + shipper.getTenNguoiDung() + " cho đơn hàng " + maDonHang);

		// Gán shipper vào đơn hàng và cập nhật trạng thái
		donHang.setShipper(shipper);
		donHang.setTrangThaiDonHang("Đang chuẩn bị hàng");
		donHangService.capNhatTrangThai(donHang, "Đang chuẩn bị hàng");
		donHangService.updateDonHang(donHang);

		// **Kiểm tra trạng thái sau khi cập nhật**
		System.out.println("✅ [DEBUG] Trạng thái đơn hàng sau cập nhật: " + donHang.getTrangThaiDonHang());

		redirectAttributes.addFlashAttribute("successMessage",
				"Đã gán shipper thành công! Đơn hàng chuyển sang trạng thái 'Đang chuẩn bị hàng'.");

		return "redirect:/admin/orders/" + maDonHang;
	}

	@PostMapping("/orders/{maDonHang}/update-status")
	public String updateOrderStatus(@PathVariable("maDonHang") Integer maDonHang,
	                                @RequestParam("status") String action,
	                                @RequestParam(value = "shipperId", required = false) Integer shipperId,
	                                RedirectAttributes redirectAttributes) {

	    DonHang donHang = donHangService.getDonHangById(maDonHang);
	    if (donHang == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
	        return "redirect:/admin/orders";
	    }

	    // ✅ Xác nhận đơn hàng
	    if ("confirm".equals(action)) {
	        donHang.setTrangThaiDonHang("Đã xác nhận");
	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xác nhận.");
	        return "redirect:/admin/orders/" + maDonHang;
	    }
	 // ❌ Hủy đơn hàng
	    else if ("cancel".equals(action)) {
	        donHang.setTrangThaiDonHang("Đã hủy");
	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã bị hủy.");
	    } 
	    // 🚨 Trạng thái không hợp lệ
	    else {
	        redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
	    }

	    // ✅ Hủy đơn hàng
	    if ("cancel".equals(action)) {
	        donHang.setTrangThaiDonHang("Đã hủy");
	        donHang.setTrangThaiChoXacNhan(null); // Xóa trạng thái chờ xác nhận (nếu có)
	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã bị hủy.");
	        return "redirect:/admin/orders/" + maDonHang;
	    }

	    // ✅ Nếu shipper báo "Giao hàng thất bại lần 2"
	    if ("Giao hàng thất bại (Lần 2)".equals(donHang.getTrangThaiChoXacNhan())
	            || "Giao thất bại".equals(action)) {
	        donHang.setTrangThaiDonHang("Giao thất bại");
	        donHang.setTrangThaiChoXacNhan(null); // Xóa trạng thái chờ xác nhận
	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng giao thất bại.");
	        return "redirect:/admin/orders/" + maDonHang;
	    }

	    // ✅ Nếu admin chọn "Giao lại"
	 // ✅ Nếu admin chọn "Giao lại"
	    if ("retry".equals(action)) {
	        if (donHang.getSoLanGiaoThatBai() >= 2) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Không thể giao lại vì đã thất bại 2 lần.");
	            return "redirect:/admin/orders/" + maDonHang;
	        }

	        // 🔥 Kiểm tra shipper có được chọn không
	        if (shipperId == null || shipperId == 0) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn shipper khi giao lại.");
	            return "redirect:/admin/orders/" + maDonHang;
	        }

	        // ✅ Lấy thông tin shipper mới từ DB
	        NguoiDung shipperMoi = nguoiDungService.findById(shipperId);
	        if (shipperMoi == null) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Shipper không hợp lệ.");
	            return "redirect:/admin/orders/" + maDonHang;
	        }

	        // ✅ Cập nhật thông tin đơn hàng
	        NguoiDung shipperCu = donHang.getShipper(); // Lưu shipper cũ
	        donHang.setShipper(shipperMoi);
	        donHang.setTrangThaiDonHang("Đang chuẩn bị hàng"); // Để shipper thấy đơn hàng
	        donHang.setTrangThaiChoXacNhan("Chờ shipper xác nhận lại"); // Để shipper mới xác nhận giao lại
	        donHang.setSoLanGiaoThatBai(donHang.getSoLanGiaoThatBai() + 1); // Tăng số lần giao thất bại

	        // ✅ Nếu shipper cũ khác shipper mới, ghi nhận việc bàn giao đơn hàng
	        if (shipperCu != null && !shipperCu.equals(shipperMoi)) {
	            String lichSu = donHang.getLichSuTrangThai() != null ? donHang.getLichSuTrangThai() : "";
	            String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	            lichSu += "\n🛑 " + thoiGian + " - Đơn hàng được bàn giao từ " + shipperCu.getTenNguoiDung() 
	                    + " sang shipper " + shipperMoi.getTenNguoiDung();
	            donHang.setLichSuTrangThai(lichSu);
	        }

	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đang được giao lại cho " + shipperMoi.getTenNguoiDung());
	        return "redirect:/admin/orders/" + maDonHang;
	    }


	    // ✅ Nếu không khớp với trạng thái nào ở trên
	    redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
	    return "redirect:/admin/orders/" + maDonHang;
	}




//
//	@PostMapping("/orders/{maDonHang}/update-status")
//	public String updateOrderStatus(@PathVariable("maDonHang") Integer maDonHang,
//			@RequestParam("status") String newStatus,
//			@RequestParam(value = "shipperId", required = false) Integer shipperId,
//			RedirectAttributes redirectAttributes) {
//
//		DonHang donHang = donHangService.getDonHangById(maDonHang);
//		if (donHang == null) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
//			return "redirect:/admin/orders";
//		}
//
//		// ✅ Nếu admin xác nhận giao hàng thất bại lần 2
//		if ("Giao hàng thất bại (Lần 2)".equals(donHang.getTrangThaiChoXacNhan())
//				|| "Giao thất bại".equals(newStatus)) {
//			donHangService.capNhatTrangThai(donHang, "Giao thất bại");
//			donHang.setTrangThaiChoXacNhan(null);
//			redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng giao thất bại.");
//			return "redirect:/admin/orders/" + maDonHang;
//		}
//
//		// ✅ Nếu admin chọn "Hủy đơn hàng"
//		if ("cancel".equals(newStatus)) {
//			donHangService.capNhatTrangThai(donHang, "Đã hủy");
//			donHang.setTrangThaiChoXacNhan(null);
//			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã bị hủy.");
//			return "redirect:/admin/orders/" + maDonHang;
//		}
//
//		// ✅ Nếu admin chọn "Giao lại"
//		if ("retry".equals(newStatus)) {
//			if (donHang.getSoLanGiaoThatBai() >= 2) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Không thể giao lại vì đã thất bại 2 lần.");
//				return "redirect:/admin/orders/" + maDonHang;
//			}
//
//			if (shipperId == null || shipperId == 0) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn shipper khi giao lại.");
//				return "redirect:/admin/orders/" + maDonHang;
//			}
//
//			NguoiDung shipper = nguoiDungService.findById(shipperId);
//			if (shipper == null) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Shipper không hợp lệ.");
//				return "redirect:/admin/orders/" + maDonHang;
//			}
//
//			donHang.setShipper(shipper);
//			donHangService.capNhatTrangThai(donHang, "Đang chuẩn bị hàng");
//			donHang.setTrangThaiChoXacNhan("Chờ shipper xác nhận lại");
//			donHang.setSoLanGiaoThatBai(0);
//
//			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đang được giao lại.");
//			return "redirect:/admin/orders/" + maDonHang;
//		}
//
//		// ✅ Cập nhật trạng thái mới mà vẫn giữ lịch sử
//		donHangService.capNhatTrangThai(donHang, newStatus);
//
//		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công.");
//		return "redirect:/admin/orders/" + maDonHang;
//	}

	// Trang xác nhận đơn hàng
//	@GetMapping("/order/confirm/{id}")
//	public String confirmOrder(@PathVariable("id") Integer id, Model model) {
//		DonHang donHang = donHangService.getDonHangById(id);
//		if (donHang == null) {
//			return "redirect:/admin/orders";
//		}
//
//		// Kiểm tra số lần giao thất bại, nếu null thì set về 0 để tránh lỗi
//		int soLanGiaoThatBai = donHang.getSoLanGiaoThatBai() != null ? donHang.getSoLanGiaoThatBai() : 0;
//		System.out.println("🚀 Số lần giao thất bại: " + soLanGiaoThatBai);
//
//		// Lấy danh sách trạng thái tiếp theo
//		List<String> nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang(), false, soLanGiaoThatBai);
//		System.out.println("🚀 nextStatuses: " + nextStatuses); // Debug
//
//		model.addAttribute("nextStatuses", nextStatuses);
//		model.addAttribute("donHang", donHang);
//
//		return "admin/order/confirm_order"; // Điều hướng đúng qua trang xác nhận
//	}
	@GetMapping("/order/confirm/{id}")
	public String confirmOrder(@PathVariable("id") Integer id, 
			Model model, 
			RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(id);

		// Kiểm tra nếu đơn hàng không tồn tại
		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/admin/orders";
		}

		// Debug kiểm tra giá trị donHang
		System.out.println("🚀 Đơn hàng: " + donHang.getMaDonHang());
		System.out.println("🚀 Trạng thái hiện tại: " + donHang.getTrangThaiDonHang());

		// Kiểm tra trạng thái chờ xác nhận (từ shipper)
		String trangThaiChoXacNhan = donHang.getTrangThaiChoXacNhan();
		if (trangThaiChoXacNhan != null) {
			System.out.println("🚀 Trạng thái chờ xác nhận từ shipper: " + trangThaiChoXacNhan);
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
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Thêm thông tin vào model
		// Lấy danh sách shipper
		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER");
		List<String> nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang(), false,
				donHang.getSoLanGiaoThatBai());
		model.addAttribute("nextStatuses", nextStatuses);

		model.addAttribute("danhSachShipper", danhSachShipper);
		model.addAttribute("donHang", donHang);
		model.addAttribute("formattedTongGiaTriDonHang", formattedTongGiaTriDonHang);
		model.addAttribute("formattedPhiVanChuyen", formattedPhiVanChuyen);
		model.addAttribute("formattedGiaSanPhamMap", formattedGiaSanPhamMap);
		model.addAttribute("formattedThanhTienMap", formattedThanhTienMap);

		return "admin/order/confirm_order";
	}

	// Xác nhận đơn hàng
	@PostMapping("/order/confirm/{id}")
	public String confirmOrder(@PathVariable("id") Integer id, 
			@RequestParam("address") String address,
			@RequestParam("phone") String phone, 
			Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		try {
			donHangService.confirmOrder(id, address, phone);
			return "redirect:/admin/orders";
		} catch (Exception e) {
			model.addAttribute("error", "Có lỗi xảy ra khi xác nhận đơn hàng!");
			return "admin/order/confirm_order";
		}
	}

	// Cập nhật trạng thái đơn hàng
	@PostMapping("/order/update")
	public String updateOrder(@ModelAttribute("donHang") DonHang donHang, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		try {
			donHangService.updateDonHang(donHang);
			return "redirect:/admin/orders";
		} catch (RuntimeException e) {
			model.addAttribute("error", e.getMessage());
			return "admin/order/confirm_order";
		}
	}

//	@PostMapping("/orders/{maDonHang}/confirm-status")
//	public String confirmShipperStatus(@PathVariable("maDonHang") Integer maDonHang,
//			RedirectAttributes redirectAttributes) {
//
//		DonHang donHang = donHangService.getDonHangById(maDonHang);
//		if (donHang == null) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
//			return "redirect:/admin/orders";
//		}
//
//		int soLanGiaoThatBai = (donHang.getSoLanGiaoThatBai() == null) ? 0 : donHang.getSoLanGiaoThatBai();
//
//		// Nếu trạng thái cần xác nhận là "Giao thất bại"
//		if ("Giao thất bại".equals(donHang.getTrangThaiChoXacNhan())) {
//			soLanGiaoThatBai++;
//			donHang.setSoLanGiaoThatBai(soLanGiaoThatBai);
//
//			if (soLanGiaoThatBai >= 2) {
//				donHang.setTrangThaiDonHang("Hủy đơn hàng");
//				redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã thất bại 2 lần và bị hủy.");
//			} else {
//				donHang.setTrangThaiDonHang("Đang giao lại lần " + soLanGiaoThatBai);
//				redirectAttributes.addFlashAttribute("successMessage",
//						"Đơn hàng đang được giao lại lần " + soLanGiaoThatBai);
//			}
//
//			donHang.setTrangThaiChoXacNhan(null);
//			donHangService.updateDonHang(donHang);
//			return "redirect:/admin/orders/" + maDonHang;
//		}
//
//		redirectAttributes.addFlashAttribute("errorMessage", "Không có trạng thái nào cần xác nhận.");
//		return "redirect:/admin/orders/" + maDonHang;
//	}
	@PostMapping("/orders/{maDonHang}/confirm-status")
	public String confirmShipperStatus(@PathVariable("maDonHang") Integer maDonHang,
			RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/admin/orders";
		}

		// ✅ Nếu đơn hàng đang xử lý, admin xác nhận đơn hàng
		if ("Đang xử lý".equals(donHang.getTrangThaiDonHang())) {
			donHang.setTrangThaiDonHang("Đã xác nhận");
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xác nhận.");
			return "redirect:/admin/orders/" + maDonHang;
		}

		// ✅ Kiểm tra trạng thái chờ xác nhận từ shipper
		String trangThaiMoi = donHang.getTrangThaiChoXacNhan();
		if (trangThaiMoi == null || trangThaiMoi.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không có trạng thái nào cần xác nhận.");
			return "redirect:/admin/orders/" + maDonHang;
		}
		// ✅ Nếu trạng thái chờ xác nhận là "Giao lại đơn hàng"
		if ("Giao lại đơn hàng".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đang chuẩn bị hàng"); // Cập nhật trạng thái thành "Đang chuẩn bị hàng"
			donHang.setTrangThaiChoXacNhan(null); // Xóa trạng thái chờ xác nhận
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã xác nhận giao lại.");
		}

		// ✅ Nếu shipper báo "Đang giao hàng", admin xác nhận đơn hàng đang giao
		if ("Đang giao hàng".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đang giao hàng");
			donHangService.capNhatTrangThai(donHang, "Đang giao hàng");
			donHang.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage",
					"Đã xác nhận trạng thái 'Đang giao hàng' từ shipper.");
		}
		// ✅ Nếu shipper báo "Giao hàng thành công", admin xác nhận hoàn thành
		else if ("Đã hoàn thành".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đã hoàn thành");
			donHang.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được giao thành công.");
		}
		// ✅ Nếu shipper báo "Giao thất bại"
		else if (trangThaiMoi.startsWith("Giao hàng thất bại")) {
			int soLanGiaoThatBai = (donHang.getSoLanGiaoThatBai() == null) ? 0 : donHang.getSoLanGiaoThatBai();
			soLanGiaoThatBai++;
			donHang.setSoLanGiaoThatBai(soLanGiaoThatBai);

			if (soLanGiaoThatBai >= 2) {
				donHang.setTrangThaiDonHang("Đã hủy");
				donHang.setTrangThaiChoXacNhan(null);
				redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã thất bại 2 lần và bị hủy.");
			} else {
				donHang.setTrangThaiDonHang("Chờ shipper xác nhận lại");
				donHang.setTrangThaiChoXacNhan("Giao lại đơn hàng");
				redirectAttributes.addFlashAttribute("successMessage",
						"Đơn hàng đang chờ shipper nhận lại lần " + soLanGiaoThatBai);
			}

			donHangService.updateDonHang(donHang);
		}
		// ✅ Nếu trạng thái là "Chờ shipper xác nhận lại"
		else if ("Chờ shipper xác nhận lại".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đang chuẩn bị hàng"); // Cho phép shipper nhận đơn
			donHang.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được giao lại cho shipper.");
		}
		// ✅ Nếu trạng thái không hợp lệ
		else {
			redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
		}

		return "redirect:/admin/orders/" + maDonHang;
	}

//	@PostMapping("/orders/{maDonHang}/retry-delivery")
//	public String retryDelivery(@PathVariable("maDonHang") Integer maDonHang,
//	        RedirectAttributes redirectAttributes) {
//	    DonHang donHang = donHangService.getDonHangById(maDonHang);
//	    if (donHang == null) {
//	        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
//	        return "redirect:/admin/orders";
//	    }
//
//	    // ✅ Reset trạng thái và quay lại bước chọn shipper
//	    donHang.setTrangThaiDonHang("Đã xác nhận");
//	    donHang.setSoLanGiaoThatBai(0);
//	    donHangService.updateDonHang(donHang);
//	    redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đang được giao lại. Hãy chọn shipper.");
//	    return "redirect:/admin/orders/" + maDonHang;
//	}
	@PostMapping("/orders/{maDonHang}/cancel-order")
	public String cancelOrder(@PathVariable("maDonHang") Integer maDonHang, RedirectAttributes redirectAttributes) {
	    DonHang donHang = donHangService.getDonHangById(maDonHang);
	    if (donHang == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
	        return "redirect:/admin/orders";
	    }

	    // Nếu đơn hàng đang xử lý, cho phép hủy
	    if ("Đang xử lý".equals(donHang.getTrangThaiDonHang())) {
	        donHang.setTrangThaiDonHang("Đã hủy");
	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công.");
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "Chỉ có thể hủy đơn hàng ở trạng thái 'Đang xử lý'.");
	    }

	    return "redirect:/admin/orders";
	}


	// Kiểm tra trạng thái tiếp theo có hợp lệ không

	@ModelAttribute("getNextStatuses")
	public List<String> getNextStatuses(String currentStatus, boolean isShipperConfirmed, Integer soLanGiaoThatBai) {
		if (currentStatus == null || currentStatus.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> nextStatuses = new ArrayList<>();

		switch (currentStatus) {
		case "Đang xử lý":
			nextStatuses.add("Đã xác nhận");
			nextStatuses.add("Đã hủy");
			break;
		case "Đã xác nhận":
			nextStatuses.add("Đang chuẩn bị hàng");
			break;
		case "Đang chuẩn bị hàng":
			nextStatuses.add("Đang giao hàng");
			break;
		case "Đang giao hàng":
			if (isShipperConfirmed) {
				nextStatuses.add("Đã hoàn thành");
			} else {
				nextStatuses.add("Chờ xác nhận từ shipper");
			}
			break;
		case "Giao thất bại":
			if (soLanGiaoThatBai < 2) {
				nextStatuses.add("Đang giao lại lần " + (soLanGiaoThatBai + 1));
			} else {
				nextStatuses.add("Hủy đơn hàng");
			}
			break;
		case "Đã hoàn thành":
		case "Đã hủy":
			break;
		}

		System.out.println("🚀 nextStatuses: " + nextStatuses);
		return nextStatuses;
	}
	
	@GetMapping("/orders/{maDonHang}/confirm-status")
	public String confirmOrderPage(
			  @PathVariable("maDonHang") Integer maDonHang,
			Model model, 
			RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);

		// Kiểm tra nếu đơn hàng không tồn tại
		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/admin/orders";
		}

		// Debug kiểm tra giá trị donHang
		System.out.println("🚀 Đơn hàng: " + donHang.getMaDonHang());
		System.out.println("🚀 Trạng thái hiện tại: " + donHang.getTrangThaiDonHang());

		// Kiểm tra trạng thái chờ xác nhận (từ shipper)
		String trangThaiChoXacNhan = donHang.getTrangThaiChoXacNhan();
		if (trangThaiChoXacNhan != null) {
			System.out.println("🚀 Trạng thái chờ xác nhận từ shipper: " + trangThaiChoXacNhan);
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
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Thêm thông tin vào model
		// Lấy danh sách shipper
		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER");
		List<String> nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang(), false,
				donHang.getSoLanGiaoThatBai());
		model.addAttribute("nextStatuses", nextStatuses);

		model.addAttribute("danhSachShipper", danhSachShipper);
		model.addAttribute("donHang", donHang);
		model.addAttribute("formattedTongGiaTriDonHang", formattedTongGiaTriDonHang);
		model.addAttribute("formattedPhiVanChuyen", formattedPhiVanChuyen);
		model.addAttribute("formattedGiaSanPhamMap", formattedGiaSanPhamMap);
		model.addAttribute("formattedThanhTienMap", formattedThanhTienMap);

	    return "admin/order/confirm_order";

	}

}