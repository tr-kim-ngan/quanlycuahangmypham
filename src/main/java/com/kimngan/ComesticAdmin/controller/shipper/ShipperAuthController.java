package com.kimngan.ComesticAdmin.controller.shipper;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.StorageService;

@Controller
@RequestMapping("/shipper")
public class ShipperAuthController {

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private DonHangService donHangService;

	@Autowired
	private StorageService storageService;

	@ModelAttribute("user")
	public NguoiDung getCurrentUser(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	// Hiển thị trang đăng nhập
	@GetMapping("/login")
	public String showLoginForm() {
		return "shipper/login"; // Điều hướng đến trang login.html của Shipper
	}

	// Đăng xuất Shipper
	@GetMapping("/logout")
	public String logout() {
		return "redirect:/shipper/login"; // Điều hướng về trang đăng nhập sau khi đăng xuất
	}

	// Thêm thông tin Shipper hiện tại vào model (giống cách làm của Customer)
	@ModelAttribute("currentShipper")
	public NguoiDung getCurrentShipper(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	// Trang chính của shipper, hiển thị thông tin cá nhân + danh sách đơn hàng
	@GetMapping({ "", "/", "/index" })
	public String shipperIndex(Model model, Principal principal) {
		System.out.println("🔥🔥🔥 Controller ShipperController đã được gọi!");

		if (principal == null) {
			return "redirect:/shipper/login";
		}

		String username = principal.getName();
		System.out.println("🚀 Username đang đăng nhập: " + username);

		NguoiDung currentShipper = nguoiDungService.findByTenNguoiDung(username);

		if (currentShipper == null) {
			System.out.println("❌ Không tìm thấy shipper!");
			model.addAttribute("errorMessage", "Không tìm thấy tài khoản shipper!");
			return "shipper/index";
		}

		System.out.println("✅ Shipper ID: " + currentShipper.getMaNguoiDung());

		List<DonHang> danhSachDonHang = donHangService.findOrdersByShipper(currentShipper);
		System.out.println("📦 Số đơn hàng tìm thấy: " + (danhSachDonHang != null ? danhSachDonHang.size() : "NULL"));

		model.addAttribute("danhSachDonHang", danhSachDonHang);
		model.addAttribute("shipper", currentShipper);

		return "shipper/index";
	}

	@GetMapping("/orders")
	public String listAllOrders(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/shipper/login";
		}

		String username = principal.getName();
		NguoiDung currentShipper = nguoiDungService.findByTenNguoiDung(username);

		if (currentShipper == null) {
			model.addAttribute("errorMessage", "Không tìm thấy tài khoản shipper!");
			return "shipper/donhang/index";
		}

		// Chỉ lấy đơn hàng có trạng thái "Đã duyệt" hoặc "Đang giao"
		List<DonHang> danhSachDonHang = donHangService.findOrdersByShipper(currentShipper);

		model.addAttribute("danhSachDonHang", danhSachDonHang);
		model.addAttribute("shipper", currentShipper);

		return "shipper/donhang/index";
	}

	@GetMapping("/order/{id}")
	public String orderDetail(@PathVariable("id") Integer id, Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/shipper/login";
		}

		String username = principal.getName();
		NguoiDung currentShipper = nguoiDungService.findByTenNguoiDung(username);

		if (currentShipper == null) {
			model.addAttribute("errorMessage", "Không tìm thấy tài khoản shipper!");
			return "shipper/donhang/index";
		}

		DonHang order = donHangService.getDonHangById(id);
		if (order == null) {
			return "redirect:/shipper/orders";
		}

		model.addAttribute("order", order);
		model.addAttribute("shipper", currentShipper); // ✅ Thêm shipper vào model giống listAllOrders

		return "shipper/donhang/detail";
	}

	@PostMapping("/order/update-status")
	public String updateOrderStatus(
			@RequestParam("orderId") Integer orderId, 
			@RequestParam("status") String status,
			@RequestParam(value = "hinhAnh", required = false) MultipartFile hinhAnh,
			@RequestParam(value = "lyDo", required = false) String lyDo, 
			Principal principal,
			RedirectAttributes redirectAttributes) {

		if (principal == null) {
			return "redirect:/shipper/login";
		}

		DonHang order = donHangService.getDonHangById(orderId);
		
		if (order == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/shipper/orders";
		}
		// Nếu đơn hàng đang chuẩn bị -> chuyển sang trạng thái Đang giao hàng
		// Nếu đơn hàng đang chuẩn bị hàng -> Cập nhật trạng thái chờ xác nhận, KHÔNG cập nhật trực tiếp
		if ("Đang chuẩn bị hàng".equals(order.getTrangThaiDonHang())) {
		    order.setTrangThaiChoXacNhan("Đang giao hàng");  // ✅ Lưu vào trạng thái chờ xác nhận
		    donHangService.updateDonHang(order);
		    redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu xác nhận đang giao hàng.");
		    return "redirect:/shipper/order/" + orderId;
		}

//		// Nếu đơn hàng đang giao hàng -> cho phép chọn hoàn thành hoặc thất bại
//		if ("Đang giao hàng".equals(order.getTrangThaiDonHang())) {
//		    if ("Giao thất bại".equals(status)) {
//		        order.setTrangThaiChoXacNhan("Giao hàng thất bại");
//		    } else {
//		        order.setTrangThaiChoXacNhan(status);
//		    }
//		    donHangService.updateDonHang(order);
//		    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công. Chờ admin xác nhận.");
//		    return "redirect:/shipper/order/" + orderId;
//		}

		// ✅ Nếu trạng thái là "Chờ shipper lấy hàng" và trước đó đã giao thất bại (Lần
		// 1)
		// Nếu admin đã giao lại đơn hàng, shipper nhận đơn lại và chuyển sang "Đang
		// giao hàng"
		if (("Đã xác nhận".equals(order.getTrangThaiDonHang())
				|| "Đang chuẩn bị hàng".equals(order.getTrangThaiDonHang()))
				&& "Giao hàng thất bại (Lần 1)".equals(order.getTrangThaiChoXacNhan())
				&& "Đang giao lại (Lần 2)".equals(status)) {

			order.setTrangThaiDonHang("Đang giao hàng");
			donHangService.updateDonHang(order);
			redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận hàng lần 2! Bắt đầu giao lại.");
			return "redirect:/shipper/order/" + orderId;
		}

		// ✅ Nếu trạng thái là "Đang giao hàng", shipper có thể chọn "Giao hàng thành
		// công" hoặc "Giao thất bại"
		  // ✅ Nếu trạng thái là "Đang giao hàng", shipper có thể chọn "Giao hàng thành công" hoặc "Giao thất bại"
	    if ("Đang giao hàng".equals(order.getTrangThaiDonHang())) {
	        if ("Giao thất bại".equals(status)) {
	            int soLanGiaoThatBai = order.getSoLanGiaoThatBai();
	            order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần " + (soLanGiaoThatBai + 1) + ")");
	            order.setSoLanGiaoThatBai(soLanGiaoThatBai + 1);
	        } else {
	            order.setTrangThaiChoXacNhan(status); // Lưu trạng thái chờ xác nhận từ admin
	        }

	        donHangService.updateDonHang(order);
	        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công. Chờ admin xác nhận.");
	        return "redirect:/shipper/order/" + orderId;
	    }

		// ✅ Kiểm tra nếu shipper đang đăng nhập có phải là shipper của đơn hàng không
		if (order.getShipper() == null || !order.getShipper().getTenNguoiDung().equals(principal.getName())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải shipper của đơn hàng này.");
			return "redirect:/shipper/orders";
		}

		 // ✅ Nếu shipper nhận đơn giao lại (Admin đã chọn giao lại)
	    if ("Chờ shipper xác nhận lại".equals(order.getTrangThaiChoXacNhan())) {
	        order.setTrangThaiDonHang("Đang chuẩn bị hàng"); // Quay về trạng thái chuẩn bị hàng
	        order.setTrangThaiChoXacNhan(null); // Xóa trạng thái chờ xác nhận
	        donHangService.updateDonHang(order);
	        redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận đơn hàng để giao lại.");
	        return "redirect:/shipper/order/" + orderId;
	    }
		// ✅ Chỉ cho phép shipper cập nhật trạng thái hợp lệ
		List<String> allowedStatuses = Arrays.asList("Đang giao hàng", "Đã hoàn thành", "Giao thất bại");

		if (!allowedStatuses.contains(status)) {
			redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
			return "redirect:/shipper/orders";
		}

		// ✅ Nếu trạng thái là "Giao thất bại", kiểm tra số lần giao thất bại
		if (status.equals("Giao thất bại")) {
			int soLanGiaoThatBai = order.getSoLanGiaoThatBai(); // Cần có trường này trong DonHang

			if (soLanGiaoThatBai >= 2) {
				redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã thất bại 2 lần, không thể giao lại.");
				return "redirect:/shipper/orders";
			}

			// ✅ Lưu trạng thái theo số lần thất bại
			if (soLanGiaoThatBai == 0) {
				order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần 1)");
			} else if (soLanGiaoThatBai == 1) {
				order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần 2)");
			}

			order.setGhiChu(lyDo); // Lưu lý do thất bại
			order.setSoLanGiaoThatBai(soLanGiaoThatBai + 1);
			donHangService.updateDonHang(order);

			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thất bại. Chờ admin xác nhận.");
			return "redirect:/shipper/order/" + orderId;
		}

		// ✅ Nếu trạng thái là "Đã hoàn thành", lưu ảnh giao hàng
		if (status.equals("Đã hoàn thành") && hinhAnh != null && !hinhAnh.isEmpty()) {
			try {
				String fileName = storageService.storeFile(hinhAnh);
				order.setHinhAnhGiaoHang(fileName);
			} catch (IOException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu ảnh.");
				return "redirect:/shipper/order/" + orderId;
			}
		}

		order.setTrangThaiChoXacNhan(status);
		donHangService.updateDonHang(order);

		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công. Chờ admin xác nhận.");
		return "redirect:/shipper/order/" + orderId;
	}

}
