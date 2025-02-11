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
import java.util.stream.Collectors;
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
	private DonHangService donHangService;



	@Autowired
	private NguoiDungService nguoiDungService;


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

		// ✅ Lấy danh sách shipper
		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER");

		// 🔥 Kiểm tra trạng thái đơn hàng khi Admin mở trang chi tiết
		System.out.println("✅ Trạng thái đơn hàng: " + donHang.getTrangThaiDonHang());
		System.out.println("✅ Trạng thái chờ xác nhận: " + donHang.getTrangThaiChoXacNhan());

		List<String> nextStatuses;
		if (donHang.getTrangThaiChoXacNhan() != null && !donHang.getTrangThaiChoXacNhan().isEmpty()) {
			nextStatuses = Arrays.asList(donHang.getTrangThaiChoXacNhan());
		} else {
			nextStatuses = getNextStatuses(donHang.getTrangThaiDonHang(), false);
		}
		// ✅ Nếu trạng thái chờ xác nhận != null thì dùng nó, ngược lại lấy trạng thái
		// hiện tại

		// ✅ Xác định trạng thái hiện tại
		String currentStatus = (donHang.getTrangThaiChoXacNhan() != null && !donHang.getTrangThaiChoXacNhan().isEmpty())
				? donHang.getTrangThaiChoXacNhan()
				: donHang.getTrangThaiDonHang();

		if (currentStatus == null || currentStatus.isEmpty()) {
			currentStatus = "Đang xử lý"; // Trạng thái mặc định
		}

		// ✅ Danh sách trạng thái hiển thị (không có "Chờ admin xác nhận")
		List<String> allStatuses = Arrays.asList("Đang xử lý", "Đã xác nhận", "Đang chuẩn bị hàng", "Đang giao hàng",
				"Đã hoàn thành", "Đã hủy");

		// ✅ Xóa trạng thái có chữ "Chờ admin xác nhận"
		List<String> displayedStatuses = allStatuses.stream().filter(status -> !status.contains("(Chờ admin xác nhận)"))
				.collect(Collectors.toList());

		// ✅ Xác định trạng thái hiện tại
		int currentStatusIndex = displayedStatuses.indexOf(currentStatus);
		if (currentStatusIndex == -1) {
			currentStatusIndex = 0; // Tránh lỗi nếu trạng thái không hợp lệ
		}

		// ✅ Định dạng số tiền
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		String formattedTongGiaTri = decimalFormat.format(donHang.getTongGiaTriDonHang());
		String formattedPhiVanChuyen = decimalFormat.format(donHang.getPhiVanChuyen());

		// ✅ Định dạng các giá trị trong chi tiết đơn hàng
		List<Map<String, String>> formattedChiTietDonHang = new ArrayList<>();
		if (donHang.getChiTietDonHangs() == null || donHang.getChiTietDonHangs().isEmpty()) {
			model.addAttribute("error", "Đơn hàng không có chi tiết sản phẩm.");
		} else {
			for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
				if (chiTiet.getSanPham() == null) {
					continue;
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
		System.out.println("🚀 nextStatuses: " + nextStatuses);
		model.addAttribute("nextStatuses", nextStatuses);
		// ✅ Gắn dữ liệu vào model
		model.addAttribute("allStatuses", displayedStatuses);

		model.addAttribute("danhSachShipper", danhSachShipper);
		model.addAttribute("donHang", donHang);
		model.addAttribute("formattedTongGiaTri", formattedTongGiaTri);
		model.addAttribute("formattedPhiVanChuyen", formattedPhiVanChuyen);
		model.addAttribute("formattedChiTietDonHang", formattedChiTietDonHang);
		model.addAttribute("displayedStatuses", displayedStatuses);
		model.addAttribute("currentStatusIndex", currentStatusIndex);

		// ✅ Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/order/view"; // Trả về trang view để hiển thị chi tiết đơn hàng và cập nhật trạng thái
	}
	@PostMapping("/orders/{maDonHang}/assign-shipper")
	public String assignShipper(@PathVariable("maDonHang") Integer maDonHang,
			@RequestParam("shipperId") Integer shipperId, RedirectAttributes redirectAttributes) {
		try {
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

			// ✅ Gán shipper vào đơn hàng
			donHang.setShipper(shipper);
			donHang.setTrangThaiDonHang("Đang chuẩn bị hàng"); // Admin chỉ chuẩn bị hàng, chưa giao

			donHangService.updateDonHang(donHang);
			System.out.println("✅ Đơn hàng " + maDonHang + " đã gán cho shipper " + shipperId);

			redirectAttributes.addFlashAttribute("successMessage", "Đã gán shipper thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gán shipper: " + e.getMessage());
		}

		return "redirect:/admin/orders/" + maDonHang;
	}


	@PostMapping("/orders/{maDonHang}/update-status")
	public String updateOrderStatus(@PathVariable("maDonHang") Integer maDonHang,
	                                @RequestParam("status") String newStatus,
	                                @RequestParam(value = "shipperId", required = false) Integer shipperId,
	                                RedirectAttributes redirectAttributes) {

	    DonHang donHang = donHangService.getDonHangById(maDonHang);
	    if (donHang == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
	        return "redirect:/admin/orders";
	    }

	    System.out.println("✅ Đang cập nhật trạng thái cho đơn hàng: " + maDonHang);
	    System.out.println("Trạng thái hiện tại: " + donHang.getTrangThaiDonHang());
	    System.out.println("Trạng thái mới: " + newStatus);

	    // ✅ Bắt buộc chọn shipper trước khi chuyển sang "Đang chuẩn bị hàng"
	    if ("Đang chuẩn bị hàng".equals(newStatus) && (shipperId == null || shipperId <= 0)) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn shipper trước khi chuẩn bị hàng.");
	        return "redirect:/admin/orders/" + maDonHang;
	    }

	    // ✅ Nếu có shipper, gán shipper vào đơn hàng
	    if (shipperId != null && shipperId > 0) {
	        NguoiDung shipper = nguoiDungService.findById(shipperId);
	        if (shipper != null) {
	            donHang.setShipper(shipper);
	        }
	    }
	 // 🔥 Kiểm tra nếu đơn hàng đã hoàn thành thì tạo hóa đơn
	   
	    // ✅ Cập nhật trạng thái đơn hàng
	    donHang.setTrangThaiDonHang(newStatus);
	    donHangService.updateDonHang(donHang);
	    System.out.println("🔍 Giá trị newStatus nhận được từ request: '" + newStatus + "'");
	    System.out.println("🚀 Gọi updateOrderStatus() với maDonHang: " + maDonHang);

	    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công.");
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

		// Lấy danh sách shipper để admin chọn nếu cần
		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER");
		model.addAttribute("danhSachShipper", danhSachShipper);
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

	@PostMapping("/orders/{maDonHang}/confirm-status")
	public String confirmShipperStatus(@PathVariable("maDonHang") Integer maDonHang,
			RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
			return "redirect:/admin/orders";
		}

		// ✅ Kiểm tra nếu shipper đã gửi trạng thái mới
		if (donHang.getTrangThaiChoXacNhan() != null) {
			donHang.setTrangThaiDonHang(donHang.getTrangThaiChoXacNhan());
			donHang.setTrangThaiChoXacNhan(null); // Xóa trạng thái chờ xác nhận

			donHangService.updateDonHang(donHang);
			redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận trạng thái đơn hàng.");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Không có trạng thái nào cần xác nhận.");
		}

		return "redirect:/admin/orders/" + maDonHang;
	}
	// Kiểm tra trạng thái tiếp theo có hợp lệ không
	

	@ModelAttribute("getNextStatuses")
	public List<String> getNextStatuses(String currentStatus, boolean isShipperConfirmed) {
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
			}
			break;

		case "Đã hoàn thành":
		case "Đã hủy":
			break;
		}

		System.out.println("✅ Trạng thái hợp lệ tiếp theo: " + nextStatuses);
		return nextStatuses;
	}

}