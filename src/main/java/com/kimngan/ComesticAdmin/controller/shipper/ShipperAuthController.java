package com.kimngan.ComesticAdmin.controller.shipper;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
		System.out.println(" Controller ShipperController đã được gọi!");

		if (principal == null) {
			return "redirect:/shipper/login";
		}

		String username = principal.getName();
		System.out.println(" Username đang đăng nhập: " + username);

		NguoiDung currentShipper = nguoiDungService.findByTenNguoiDung(username);

		if (currentShipper == null) {
			System.out.println(" Không tìm thấy shipper!");
			model.addAttribute("errorMessage", "Không tìm thấy tài khoản shipper!");
			return "shipper/index";
		}

		System.out.println(" Shipper ID: " + currentShipper.getMaNguoiDung());

		List<DonHang> danhSachDonHang = donHangService.findOrdersByShipper(currentShipper);
		System.out.println(" Số đơn hàng tìm thấy: " + (danhSachDonHang != null ? danhSachDonHang.size() : "NULL"));

		model.addAttribute("danhSachDonHang", danhSachDonHang);
		model.addAttribute("shipper", currentShipper);

		return "shipper/index";
	}

	@GetMapping("/orders")
	public String listAllOrders(@RequestParam(value = "status", required = false) String status, Model model,
			Principal principal) {
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

		model.addAttribute("selectedStatus", status);

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
		model.addAttribute("shipper", currentShipper); // Thêm shipper vào model giống listAllOrders

		return "shipper/donhang/detail";
	}

//	@PostMapping("/order/update-status")
//	public String updateOrderStatus(@RequestParam("orderId") Integer orderId, @RequestParam("status") String status,
//			@RequestParam(value = "hinhAnh", required = false) MultipartFile hinhAnh,
//			@RequestParam(value = "lyDo", required = false) String lyDo, Principal principal,
//			RedirectAttributes redirectAttributes) {
//
//		if (principal == null) {
//			return "redirect:/shipper/login";
//		}
//
//		DonHang order = donHangService.getDonHangById(orderId);
//
//		if (order == null) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
//			return "redirect:/shipper/orders";
//		}
//		if ("Đang chuẩn bị hàng".equals(order.getTrangThaiDonHang())) {
//			order.setTrangThaiChoXacNhan("Đang giao hàng"); // Lưu vào trạng thái chờ xác nhận
//			donHangService.updateDonHang(order);
//			redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu xác nhận đang giao hàng.");
//			return "redirect:/shipper/order/" + orderId;
//		}
//
//		if (("Đã xác nhận".equals(order.getTrangThaiDonHang())
//				|| "Đang chuẩn bị hàng".equals(order.getTrangThaiDonHang()))
//				&& "Giao hàng thất bại (Lần 1)".equals(order.getTrangThaiChoXacNhan())
//				&& "Đang giao lại (Lần 2)".equals(status)) {
//
//			order.setTrangThaiDonHang("Đang giao hàng");
//			donHangService.updateDonHang(order);
//			redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận hàng lần 2! Bắt đầu giao lại.");
//			return "redirect:/shipper/order/" + orderId;
//		}
//		if (status.equals("Đã hoàn thành") && hinhAnh != null && !hinhAnh.isEmpty()) {
//			try {
//				String fileName = storageService.storeFile(hinhAnh);
//				order.setHinhAnhGiaoHang(fileName); // Có chắc chỗ này đã chạy không?
//				System.out.println("📷 Ảnh đã lưu: " + fileName);
//			} catch (IOException e) {
//				e.printStackTrace();
//				redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu ảnh.");
//				return "redirect:/shipper/order/" + orderId;
//			}
//		}
//
//		// Kiểm tra nếu shipper đang đăng nhập có phải là shipper của đơn hàng không
//		if (order.getShipper() == null || !order.getShipper().getTenNguoiDung().equals(principal.getName())) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải shipper của đơn hàng này.");
//			return "redirect:/shipper/orders";
//		}
//
//		// Nếu shipper nhận đơn giao lại (Admin đã chọn giao lại)
//		if ("Chờ shipper xác nhận lại".equals(order.getTrangThaiChoXacNhan())) {
//			order.setTrangThaiDonHang("Đang chuẩn bị hàng"); // Quay về trạng thái chuẩn bị hàng
//			order.setTrangThaiChoXacNhan(null); // Xóa trạng thái chờ xác nhận
//			donHangService.updateDonHang(order);
//			redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận đơn hàng để giao lại.");
//			return "redirect:/shipper/order/" + orderId;
//		}
//		// Chỉ cho phép shipper cập nhật trạng thái hợp lệ
//		List<String> allowedStatuses = Arrays.asList("Đang giao hàng", "Đã hoàn thành", "Giao thất bại");
//
//		if (!allowedStatuses.contains(status)) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
//			return "redirect:/shipper/orders";
//		}
//
//		// Nếu trạng thái là "Giao thất bại", kiểm tra số lần giao thất bại
//		if (status.equals("Giao thất bại")) {
//			int soLanGiaoThatBai = order.getSoLanGiaoThatBai(); // Cần có trường này trong DonHang
//
//			if (soLanGiaoThatBai >= 2) {
//				redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã thất bại 2 lần, không thể giao lại.");
//				return "redirect:/shipper/orders";
//			}
//
//			// Cập nhật trạng thái thất bại theo số lần giao
//			if (soLanGiaoThatBai == 0) {
//				order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần 1)");
//			} else if (soLanGiaoThatBai == 1) {
//				order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần 2)");
//			}
//
//			// Lưu cả lịch sử lý do thất bại mà không ghi đè
//			// Đảm bảo lưu cả lịch sử lý do thất bại mà không bị lỗi
//			if (lyDo != null && !lyDo.trim().isEmpty()) {
//				String ghiChuCu = order.getGhiChu() == null ? "" : order.getGhiChu();
//				order.setGhiChu(ghiChuCu + "  Lần " + (soLanGiaoThatBai + 1) + ": " + lyDo.trim());
//			} else {
//				String ghiChuCu = order.getGhiChu() == null ? "" : order.getGhiChu() + "\n";
//				order.setGhiChu(ghiChuCu + "  Lần " + (soLanGiaoThatBai + 1) + ": Không có lý do được cung cấp.");
//			}
//
//			// Cập nhật số lần giao thất bại
//			order.setSoLanGiaoThatBai(soLanGiaoThatBai + 1);
//
//			donHangService.updateDonHang(order);
//
//			System.out.println(" Ghi chú sau khi shipper nhập: " + order.getGhiChu());
//
//			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thất bại. Chờ admin xác nhận.");
//			return "redirect:/shipper/order/" + orderId;
//		}
//
//		// Nếu trạng thái là "Đã hoàn thành", lưu ảnh giao hàng
//
//		System.out.println(" Nhận yêu cầu cập nhật trạng thái:");
//		System.out.println("🔹 Order ID: " + orderId);
//		System.out.println("🔹 Trạng thái mới: " + status);
//		System.out.println("🔹 Lý do thất bại (lyDo): " + lyDo);
//		System.out.println("🔹 Hình ảnh: " + (hinhAnh != null ? hinhAnh.getOriginalFilename() : "Không có hình"));
//
//		order.setTrangThaiChoXacNhan(status);
//		donHangService.updateDonHang(order);
//		System.out.println(" Cập nhật trạng thái thành công.");
//
//		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công. Chờ admin xác nhận.");
//		return "redirect:/shipper/order/" + orderId;
//	}
	@PostMapping("/order/update-status")
	public String updateOrderStatus(@RequestParam("orderId") Integer orderId, @RequestParam("status") String status,
			@RequestParam(value = "hinhAnh", required = false) MultipartFile hinhAnh,
			@RequestParam(value = "lyDo", required = false) String lyDo, Principal principal,
			RedirectAttributes redirectAttributes) {

		if (principal == null)
			return "redirect:/shipper/login";

		DonHang order = donHangService.getDonHangById(orderId);
		if (order == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/shipper/orders";
		}

		// ✅ Kiểm tra shipper có phải là người đang đăng nhập không
		if (order.getShipper() == null || !order.getShipper().getTenNguoiDung().equals(principal.getName())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải shipper của đơn hàng này.");
			return "redirect:/shipper/orders";
		}

		// ✅ Nếu đơn đang ở trạng thái yêu cầu shipper giao lại
		if ("Chờ shipper xác nhận lại".equals(order.getTrangThaiChoXacNhan())) {
			order.setTrangThaiDonHang("Đang chuẩn bị hàng");
			order.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(order);
			redirectAttributes.addFlashAttribute("successMessage", "Bạn đã nhận đơn hàng để giao lại.");
			return "redirect:/shipper/order/" + orderId;
		}

		if ("Giao thất bại".equals(status)) {
			int lan = order.getSoLanGiaoThatBai();

			System.out.println("📦 Giao thất bại lần: " + (lan + 1));

			if (lan >= 2) {
				redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đã thất bại 2 lần.");
				return "redirect:/shipper/orders";
			}

			// Giao thất bại lần 1 → chờ xác nhận
//			if (lan == 0) {
//				order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần 1)");
//				redirectAttributes.addFlashAttribute("successMessage",
//						"Cập nhật trạng thái thất bại. Chờ xác nhận từ kho.");
//			}
//			// Giao thất bại lần 2 → tự động hủy đơn luôn
//			else if (lan == 1) {
//				order.setTrangThaiDonHang("Đã hủy");
//				order.setTrangThaiChoXacNhan(null);
//				redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã thất bại 2 lần và bị hủy.");
//			}
			if (lan == 0) {
				order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần 1)");
			} else if (lan == 1) {
				order.setTrangThaiChoXacNhan("Giao hàng thất bại (Lần 2)");
			}


			order.setSoLanGiaoThatBai(lan + 1);

			// Ghi chú lý do
			String ghiChuCu = order.getGhiChu() == null ? "" : order.getGhiChu();
			String ghiChuMoi = "\n  Lần " + (lan + 1) + ": "
					+ (lyDo == null || lyDo.isBlank() ? "Không có lý do." : lyDo.trim());
			order.setGhiChu(ghiChuCu + ghiChuMoi);

			donHangService.updateDonHang(order);
			return "redirect:/shipper/order/" + orderId;
		}

		// ✅ Nếu trạng thái là "Đã hoàn thành" → cần ảnh và chờ xác nhận
		if ("Đã hoàn thành".equals(status)) {
			if (hinhAnh != null && !hinhAnh.isEmpty()) {
				try {
					String fileName = storageService.storeFile(hinhAnh);
					order.setHinhAnhGiaoHang(fileName);
				} catch (IOException e) {
					e.printStackTrace();
					redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu ảnh.");
					return "redirect:/shipper/order/" + orderId;
				}
			}


			order.setTrangThaiDonHang("Đã hoàn thành");
			order.setTrangThaiChoXacNhan(null);
			donHangService.updateDonHang(order);
			redirectAttributes.addFlashAttribute("successMessage", "Đã gửi xác nhận hoàn thành đơn hàng.");
			return "redirect:/shipper/order/" + orderId;
		}

		// ✅ Các trạng thái khác → cập nhật trực tiếp
		order.setTrangThaiDonHang(status);
		order.setTrangThaiChoXacNhan(null); // xóa trạng thái chờ nếu có
		donHangService.updateDonHang(order);

		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công.");
		return "redirect:/shipper/order/" + orderId;
	}

	@GetMapping("/account")
	public String showAccountPage(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/shipper/login";
		}
		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		model.addAttribute("nguoiDung", currentUser);
		return "shipper/account"; // Giao diện account của shipper
	}

	@PostMapping("/account")
	public String updateAccount(@ModelAttribute("nguoiDung") NguoiDung updatedUser,
			@RequestParam("avatarFile") MultipartFile avatarFile, Principal principal,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/shipper/login";
		}

		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		if (currentUser == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
			return "redirect:/shipper/account";
		}

		currentUser.setHoTen(updatedUser.getHoTen());
		currentUser.setEmail(updatedUser.getEmail());
		currentUser.setSoDienThoai(updatedUser.getSoDienThoai());
		currentUser.setDiaChi(updatedUser.getDiaChi());

		if (avatarFile != null && !avatarFile.isEmpty()) {
			try {
				String fileName = storageService.storeFile(avatarFile);
				currentUser.setAvatar(fileName);
			} catch (IOException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu ảnh đại diện.");
				return "redirect:/shipper/account";
			}
		}

		nguoiDungService.save(currentUser);
		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
		return "redirect:/shipper/account";
	}

}
