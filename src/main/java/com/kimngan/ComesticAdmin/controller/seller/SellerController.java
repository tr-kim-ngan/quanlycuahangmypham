package com.kimngan.ComesticAdmin.controller.seller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seller")
public class SellerController {

	@Autowired
	private DonHangService donHangService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@GetMapping("/login")
	public String loginPage() {
		return "seller/login"; // file login.html trong thư mục templates/seller/
	}

	@ModelAttribute("user")
	public NguoiDung getCurrentUser(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	@GetMapping("/orders")
	public String getOrdersForSeller(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "status", required = false) String status) {

		page = Math.max(page, 0);
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonHang"));

		Page<DonHang> donHangPage = (status != null && !status.equals("all"))
				? donHangService.getDonHangsByStatus(status, pageRequest)
				: donHangService.getAllDonHangs(pageRequest);

		model.addAttribute("donHangs", donHangPage.getContent());
		model.addAttribute("currentPage", donHangPage.getNumber());
		model.addAttribute("totalPages", donHangPage.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("searchAction", "/seller/orders");

		// Lấy thông tin người dùng đăng nhập

		return "seller/order/index";
	}

	@GetMapping("/orders/{maDonHang}")
	public String viewOrderForSeller(@PathVariable("maDonHang") Integer maDonHang, Model model) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			return "redirect:/seller/orders";
		}

		// Lấy người dùng đang đăng nhập
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Truyền đơn hàng
		model.addAttribute("donHang", donHang);

		// ✅ Thêm danh sách shipper vào nếu đơn đang chờ gán shipper
		if ("Đã xác nhận".equals(donHang.getTrangThaiDonHang())) {
			List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER");
			model.addAttribute("danhSachShipper", danhSachShipper);
		}
		System.out.println("📌 Ghi chú hiện tại:\n" + donHang.getGhiChu());

		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER");
		model.addAttribute("danhSachShipper", danhSachShipper);
		return "seller/order/view";
	}

	@PostMapping("/orders/{maDonHang}/assign-shipper")
	public String assignShipperForSeller(@PathVariable("maDonHang") Integer maDonHang,
			@RequestParam("shipperId") Integer shipperId, RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);

		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
			return "redirect:/seller/orders";
		}

		NguoiDung shipper = nguoiDungService.findById(shipperId);
		if (shipper == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy shipper!");
			return "redirect:/seller/orders/" + maDonHang;
		}

		// Gán shipper và cập nhật trạng thái
		donHang.setShipper(shipper);
		donHang.setTrangThaiDonHang("Đang chuẩn bị hàng");
		donHangService.capNhatTrangThai(donHang, "Đang chuẩn bị hàng");
		donHangService.updateDonHang(donHang);

		redirectAttributes.addFlashAttribute("successMessage",
				"Đã gán shipper thành công! Đơn hàng chuyển sang trạng thái 'Đang chuẩn bị hàng'.");

		return "redirect:/seller/orders/" + maDonHang;
	}

	@GetMapping("/orders/confirm/{id}")
	public String confirmOrderForSeller(@PathVariable("id") Integer id, Model model,
			RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(id);

		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/seller/orders";
		}

		// Debug
		System.out.println("✅ Đơn hàng cần xác nhận: " + donHang.getMaDonHang());
		System.out.println("📦 Trạng thái hiện tại: " + donHang.getTrangThaiDonHang());

		String trangThaiChoXacNhan = donHang.getTrangThaiChoXacNhan();
		if (trangThaiChoXacNhan != null) {
			System.out.println("⏳ Trạng thái chờ xác nhận từ shipper: " + trangThaiChoXacNhan);
		}

		// Người dùng đang đăng nhập
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Danh sách shipper
		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER");

		// Trạng thái tiếp theo có thể chọn
		List<String> nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang(), false,
				donHang.getSoLanGiaoThatBai());
		String trangThaiMoi = donHang.getTrangThaiChoXacNhan();
		System.out.println("🚚 Trạng thái chờ xác nhận tiếp theo từ shipper: " + trangThaiMoi);

		if (trangThaiMoi == null || trangThaiMoi.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không có trạng thái nào cần xác nhận.");
			return "redirect:/seller/orders/" + id;
		}

		// Đưa dữ liệu ra giao diện
		model.addAttribute("donHang", donHang);
		model.addAttribute("danhSachShipper", danhSachShipper);
		model.addAttribute("nextStatuses", nextStatuses);

		return "redirect:/seller/orders/" + id;// nhớ tạo file này
	}

//	@PostMapping("/orders/{maDonHang}/update-status")
//	public String updateOrderStatusFromSeller(@PathVariable("maDonHang") Integer maDonHang,
//			@RequestParam("status") String action,
//			@RequestParam(value = "cancelReason", required = false) String cancelReason,
//			@RequestParam(value = "shipperId", required = false) Integer shipperId,
//			RedirectAttributes redirectAttributes) {
//
//		DonHang donHang = donHangService.getDonHangById(maDonHang);
//		if (donHang == null) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
//			return "redirect:/seller/orders";
//		}
//
//		// ✅ Xác nhận đơn
//		if ("confirm".equals(action)) {
//			donHang.setTrangThaiDonHang("Đã xác nhận");
//			donHangService.updateDonHang(donHang);
//			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xác nhận.");
//			return "redirect:/seller/orders/" + maDonHang;
//		}
//		if ("Giao hàng thất bại (Lần 2)".equals(donHang.getTrangThaiChoXacNhan()) || "Giao thất bại".equals(action)) {
//			donHang.setTrangThaiDonHang("Giao thất bại");
//			donHang.setTrangThaiChoXacNhan(null); // Xóa trạng thái chờ xác nhận
//			donHangService.updateDonHang(donHang);
//			redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng giao thất bại.");
//			return "redirect:/admin/orders/" + maDonHang;
//		}
//
//		// ❌ Hủy đơn hàng
//		if ("cancel".equals(action)) {
//			if (cancelReason == null || cancelReason.trim().isEmpty()) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do hủy đơn hàng.");
//				return "redirect:/seller/orders/" + maDonHang;
//			}
//			donHang.setTrangThaiDonHang("Đã hủy");
//			donHang.setTrangThaiChoXacNhan(null);
//			donHang.setGhiChu(cancelReason);
//			donHangService.updateDonHang(donHang);
//			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã bị hủy.");
//			return "redirect:/seller/orders/" + maDonHang;
//		}
//
//		// ❗ Giao thất bại
//		if ("Giao thất bại".equals(action)) {
//			donHang.setTrangThaiDonHang("Giao thất bại");
//			donHang.setTrangThaiChoXacNhan(null);
//			donHangService.updateDonHang(donHang);
//			redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái giao thất bại.");
//			return "redirect:/seller/orders/" + maDonHang;
//		}
//
//		// 🔁 Giao lại đơn hàng
//		if ("retry".equals(action)) {
//			if (donHang.getSoLanGiaoThatBai() >= 2) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Không thể giao lại vì đã thất bại 2 lần.");
//				return "redirect:/seller/orders/" + maDonHang;
//			}
//
//			if (shipperId == null || shipperId == 0) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn shipper khi giao lại.");
//				return "redirect:/seller/orders/" + maDonHang;
//			}
//
//			NguoiDung shipperMoi = nguoiDungService.findById(shipperId);
//			if (shipperMoi == null) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Shipper không hợp lệ.");
//				return "redirect:/seller/orders/" + maDonHang;
//			}
//
//			// Lịch sử + tăng lần giao thất bại
//			NguoiDung shipperCu = donHang.getShipper();
//			donHang.setShipper(shipperMoi);
//			donHang.setTrangThaiDonHang("Đang chuẩn bị hàng");
//			donHang.setTrangThaiChoXacNhan("Chờ shipper xác nhận lại");
//			donHang.setSoLanGiaoThatBai(donHang.getSoLanGiaoThatBai() + 1);
//
//			if (shipperCu != null && !shipperCu.equals(shipperMoi)) {
//				String lichSu = donHang.getLichSuTrangThai() != null ? donHang.getLichSuTrangThai() : "";
//				String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
//				lichSu += "\n🛑 " + thoiGian + " - Bàn giao từ " + shipperCu.getTenNguoiDung() + " → "
//						+ shipperMoi.getTenNguoiDung();
//				donHang.setLichSuTrangThai(lichSu);
//			}
//
//			donHangService.updateDonHang(donHang);
//			redirectAttributes.addFlashAttribute("successMessage",
//					"Đơn hàng đang được giao lại cho " + shipperMoi.getTenNguoiDung());
//			return "redirect:/seller/orders/" + maDonHang;
//		}
//		String trangThaiMoi = donHang.getTrangThaiChoXacNhan();
//		System.out.println("🚚 Trạng thái chờ xác nhận tiếp theo từ shipper: " + trangThaiMoi);
//
//		if (trangThaiMoi == null || trangThaiMoi.isEmpty()) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không có trạng thái nào cần xác nhận.");
//			return "redirect:/seller/orders/" + maDonHang;
//		}
//
//		// ❓ Nếu là chọn trạng thái mới (không thuộc các trường hợp đặc biệt)
//		donHang.setTrangThaiDonHang(action);
//		donHang.setTrangThaiChoXacNhan(null);
//		donHangService.updateDonHang(donHang);
//		redirectAttributes.addFlashAttribute("successMessage", "Trạng thái đơn hàng đã cập nhật: " + action);
//		return "redirect:/seller/orders/" + maDonHang;
//	}

	
	@PostMapping("/orders/{maDonHang}/update-status")
	public String updateOrderStatusFromSeller(@PathVariable("maDonHang") Integer maDonHang,
	        @RequestParam(value = "cancelReason", required = false) String cancelReason,
	        @RequestParam("status") String action,
	        @RequestParam(value = "shipperId", required = false) Integer shipperId,
	        RedirectAttributes redirectAttributes,
	        HttpServletRequest request) {

	    DonHang donHang = donHangService.getDonHangById(maDonHang);
	    if (donHang == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
	        return "redirect:/seller/orders";
	    }

	    // Xác nhận đơn hàng
	    if ("confirm".equals(action)) {
	        donHang.setTrangThaiDonHang("Đã xác nhận");
	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xác nhận.");
	        return "redirect:/seller/orders/" + maDonHang;
	    }

	    // Hủy do giao thất bại lần 2
	    if ("Giao hàng thất bại (Lần 2)".equals(donHang.getTrangThaiChoXacNhan()) || "Giao thất bại".equals(action)) {
	        donHang.setTrangThaiDonHang("Đã hủy");
	        donHang.setTrangThaiChoXacNhan(null);
	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng giao thất bại.");
	        return "redirect:/seller/orders/" + maDonHang;
	    }

	    // Hủy đơn hàng thủ công
	    if ("cancel".equals(action)) {
	        String trangThaiChoXacNhan = donHang.getTrangThaiChoXacNhan();
	        String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	        String ghiChuCu = donHang.getGhiChu() != null ? donHang.getGhiChu() : "";

	        String lyDo;

	        if ("Giao hàng thất bại (Lần 1)".equals(trangThaiChoXacNhan)) {
	            lyDo = "";
	        } else {
	            if (cancelReason == null || cancelReason.trim().isEmpty()) {
	                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do hủy đơn hàng.");
	                return "redirect:/seller/orders/" + maDonHang;
	            }

	            // Xử lý rõ ràng khi người dùng chọn "Khác"
	            if ("Khác".equals(cancelReason)) {
	                lyDo =  request.getParameter("customCancelReason");
	            } else {
	                lyDo =  cancelReason;
	            }
	        }

	        donHang.setTrangThaiDonHang("Đã hủy");
	        donHang.setTrangThaiChoXacNhan(null);
	        donHang.setGhiChu((ghiChuCu + "\n" + lyDo).trim());
	        donHangService.updateDonHang(donHang);

	        redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã bị hủy.");
	        return "redirect:/seller/orders/" + maDonHang;
	    }


	    // Giao lại đơn hàng
	    if ("retry".equals(action)) {
	        if (donHang.getSoLanGiaoThatBai() >= 2) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Không thể giao lại vì đã thất bại 2 lần.");
	            return "redirect:/seller/orders/" + maDonHang;
	        }

	        if (shipperId == null || shipperId == 0) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn shipper khi giao lại.");
	            return "redirect:/seller/orders/" + maDonHang;
	        }

	        NguoiDung shipperMoi = nguoiDungService.findById(shipperId);
	        if (shipperMoi == null) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Shipper không hợp lệ.");
	            return "redirect:/seller/orders/" + maDonHang;
	        }

	        NguoiDung shipperCu = donHang.getShipper();
	        donHang.setShipper(shipperMoi);
	        donHang.setTrangThaiDonHang("Đang chuẩn bị hàng");
	        donHang.setTrangThaiChoXacNhan("Chờ shipper xác nhận lại");
	        donHang.setSoLanGiaoThatBai(donHang.getSoLanGiaoThatBai() + 1);

	        if (shipperCu != null && !shipperCu.equals(shipperMoi)) {
	            String lichSu = donHang.getLichSuTrangThai() != null ? donHang.getLichSuTrangThai() : "";
	            String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	            lichSu += " " + thoiGian + " - Đơn hàng được bàn giao từ " + shipperCu.getTenNguoiDung()
	                    + " sang shipper " + shipperMoi.getTenNguoiDung();
	            donHang.setLichSuTrangThai(lichSu);
	        }

	        donHangService.updateDonHang(donHang);
	        redirectAttributes.addFlashAttribute("successMessage",
	                "Đơn hàng đang được giao lại cho " + shipperMoi.getTenNguoiDung());
	        return "redirect:/seller/orders/" + maDonHang;
	    }

	    redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
	    return "redirect:/seller/orders/" + maDonHang;
	}

	@PostMapping("/order/update")
	public String updateOrderFromSeller(@ModelAttribute("donHang") DonHang donHang, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		try {
			donHangService.updateDonHang(donHang);
			return "redirect:/seller/orders/" + donHang.getMaDonHang();
		} catch (RuntimeException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "seller/order/view";
		}
	}

	@PostMapping("/orders/{maDonHang}/confirm-status")
	public String confirmShipperStatusFromSeller(@PathVariable("maDonHang") Integer maDonHang,
			RedirectAttributes redirectAttributes) {

		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/seller/orders";
		}

		if ("Chờ xác nhận".equals(donHang.getTrangThaiDonHang())) {
			donHang.setTrangThaiDonHang("Đang xử lý");
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã chuyển sang trạng thái 'Đang xử lý'.");
			return "redirect:/seller/orders";
		}

		if ("Đang xử lý".equals(donHang.getTrangThaiDonHang())) {
			donHang.setTrangThaiDonHang("Đã xác nhận");
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xác nhận.");
			return "redirect:/seller/orders/" + maDonHang;
		}

		String trangThaiMoi = donHang.getTrangThaiChoXacNhan();
		if (trangThaiMoi == null || trangThaiMoi.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không có trạng thái nào cần xác nhận.");
			return "redirect:/seller/orders/" + maDonHang;
		}

		if ("Giao lại đơn hàng".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đang chuẩn bị hàng");
			donHang.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã xác nhận giao lại.");
		} else if ("Đang giao hàng".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đang giao hàng");
			donHangService.capNhatTrangThai(donHang, "Đang giao hàng");
			donHang.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận trạng thái 'Đang giao hàng'.");
		} else if ("Đã hoàn thành".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đã hoàn thành");
			donHang.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được giao thành công.");
		} else if (trangThaiMoi.startsWith("Giao hàng thất bại")) {
			int soLanGiaoThatBai = donHang.getSoLanGiaoThatBai() != null ? donHang.getSoLanGiaoThatBai() : 0;
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
		} else if ("Chờ shipper xác nhận lại".equals(trangThaiMoi)) {
			donHang.setTrangThaiDonHang("Đang chuẩn bị hàng");
			donHang.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được giao lại cho shipper.");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
		}
		// String trangThaiMoi = donHang.getTrangThaiChoXacNhan();
		System.out.println("🚚 Trạng thái chờ xác nhận tiếp theo từ shipper: " + trangThaiMoi);

		if (trangThaiMoi == null || trangThaiMoi.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không có trạng thái nào cần xác nhận.");
			return "redirect:/seller/orders/" + maDonHang;
		}

		return "redirect:/seller/orders/" + maDonHang;
	}

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

		System.out.println(" nextStatuses: " + nextStatuses);
		return nextStatuses;
	}

}