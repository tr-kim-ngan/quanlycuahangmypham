package com.kimngan.ComesticAdmin.controller.admin;

import org.springframework.data.domain.Pageable;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.NguoiDungRepository;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
	private NguoiDungService nguoiDungService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;
	
	@Autowired
	private NguoiDungRepository nguoiDungRepository;
	@Autowired
	private SanPhamRepository sanPhamRepository;

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
	public String updateOrderStatus(@PathVariable("maDonHang") Integer maDonHang, @RequestParam("status") String action,
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
		if ("Giao hàng thất bại (Lần 2)".equals(donHang.getTrangThaiChoXacNhan()) || "Giao thất bại".equals(action)) {
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
			redirectAttributes.addFlashAttribute("successMessage",
					"Đơn hàng đang được giao lại cho " + shipperMoi.getTenNguoiDung());
			return "redirect:/admin/orders/" + maDonHang;
		}

		// ✅ Nếu không khớp với trạng thái nào ở trên
		redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
		return "redirect:/admin/orders/" + maDonHang;
	}

	@GetMapping("/order/confirm/{id}")
	public String confirmOrder(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
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
	public String confirmOrder(@PathVariable("id") Integer id, @RequestParam("address") String address,
			@RequestParam("phone") String phone, Model model) {
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
	public String confirmOrderPage(@PathVariable("maDonHang") Integer maDonHang, Model model,
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

	@GetMapping("/offline-orders")
	public String showOfflineOrderForm(
	    @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
	    @RequestParam(value = "size", defaultValue = "5") int size,
	    @RequestParam(value = "keyword", required = false) String keyword,
	    @RequestParam(value = "selectedProductIds", required = false) String selectedProductIdsStr,
	    @RequestParam(value = "selectedQuantities", required = false) String selectedQuantitiesStr,
	    HttpServletRequest request, Model model) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
	        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
	        model.addAttribute("user", userDetails);
	    } else {
	        model.addAttribute("user", null);
	    }
	    
	    model.addAttribute("requestUri", request.getRequestURI());

	    List<Integer> selectedProductIds = new ArrayList<>();
	    List<Integer> selectedQuantities = new ArrayList<>();

	    if (selectedProductIdsStr != null && !selectedProductIdsStr.isEmpty()) {
	        selectedProductIds = Arrays.stream(selectedProductIdsStr.split(","))
	                                   .map(Integer::parseInt)
	                                   .collect(Collectors.toList());
	    }

	    if (selectedQuantitiesStr != null && !selectedQuantitiesStr.isEmpty()) {
	        selectedQuantities = Arrays.stream(selectedQuantitiesStr.split(","))
	            .map(q -> {
	                String[] parts = q.split(":");
	                return parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
	            })
	            .collect(Collectors.toList());
	    }

	    Pageable pageable = PageRequest.of(page, size);
	    Page<SanPham> sanPhamPage;
	    if (keyword != null && !keyword.isEmpty()) {
	        sanPhamPage = sanPhamService.searchActiveByName(keyword, PageRequest.of(page, size));
	    } else {
	    	sanPhamPage = sanPhamService.findAllActiveWithStock(PageRequest.of(page, size));
	    }

	    if (sanPhamPage.isEmpty()) {
	        model.addAttribute("noProductsMessage", "Không có sản phẩm nào phù hợp.");
	    }

	    LocalDate today = LocalDate.now();
	    Map<Integer, String> formattedPrices = new HashMap<>();
	    Map<Integer, String> formattedDiscountPrices = new HashMap<>();
	    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

	    for (SanPham sp : sanPhamPage.getContent()) {
	        BigDecimal giaGoc = sp.getDonGiaBan();
	        formattedPrices.put(sp.getMaSanPham(), decimalFormat.format(giaGoc) + " VND");

	        Optional<KhuyenMai> highestKhuyenMai = sp.getKhuyenMais().stream()
	                .filter(KhuyenMai::getTrangThai)
	                .filter(km -> km.getNgayBatDau() != null && km.getNgayKetThuc() != null &&
	                        !km.getNgayBatDau().toLocalDate().isAfter(today) &&
	                        !km.getNgayKetThuc().toLocalDate().isBefore(today))
	                .max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

	        if (highestKhuyenMai.isPresent() && highestKhuyenMai.get().getPhanTramGiamGia() != null) {
	            BigDecimal phanTramGiam = highestKhuyenMai.get().getPhanTramGiamGia();
	            BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));

	            formattedDiscountPrices.put(sp.getMaSanPham(),
	                    "<del style='color:grey; font-size:14px;'>" + decimalFormat.format(giaGoc) + " VND</del> " +
	                            "<span class='text-danger fw-bold'>" + decimalFormat.format(giaSauGiam) + " VND</span>");
	        } else {
	            formattedDiscountPrices.put(sp.getMaSanPham(), decimalFormat.format(giaGoc) + " VND");
	        }
	    }

	    model.addAttribute("selectedProductIds", selectedProductIds);
	    model.addAttribute("selectedQuantities", selectedQuantities);
	    model.addAttribute("sanPhamList", sanPhamPage.getContent());
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", sanPhamPage.getTotalPages());
	    model.addAttribute("formattedDiscountPrices", formattedDiscountPrices);

	    return "admin/order/offline-order";
	}


	@GetMapping("/offline-orders/confirm")
	public String confirmOfflineOrder(Model model,
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai) {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
	        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
	        model.addAttribute("user", userDetails);
	    }

	    List<ChiTietDonHang> orderItems = donHangService.getCurrentOfflineOrder();

	    if (orderItems.isEmpty()) {
	        model.addAttribute("orderItems", Collections.emptyList());
	        model.addAttribute("totalPrice", "0 VND");
	        return "admin/order/offline-order-confirm";
	    }

	    LocalDate today = LocalDate.now();
	    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

	    BigDecimal totalPrice = BigDecimal.ZERO;
	    Map<Integer, String> formattedDiscountPrices = new HashMap<>();

	    for (ChiTietDonHang chiTiet : orderItems) {
	        SanPham sp = chiTiet.getSanPham();

	        // ✅ Load danh sách khuyến mãi trước khi xử lý
	        sp.setKhuyenMais(new HashSet<>(sanPhamRepository.findByIdInWithKhuyenMai(List.of(sp.getMaSanPham())).get(0).getKhuyenMais()));

	        Optional<KhuyenMai> highestKhuyenMai = sp.getKhuyenMais().stream()
	                .filter(KhuyenMai::getTrangThai)
	                .filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
	                        && !km.getNgayKetThuc().toLocalDate().isBefore(today))
	                .max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

	        BigDecimal giaSauGiam = sp.getDonGiaBan();
	        if (highestKhuyenMai.isPresent()) {
	            BigDecimal phanTramGiam = highestKhuyenMai.get().getPhanTramGiamGia();
	            giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
	        }

	        formattedDiscountPrices.put(sp.getMaSanPham(), decimalFormat.format(giaSauGiam) + " VND");
	        totalPrice = totalPrice.add(giaSauGiam.multiply(BigDecimal.valueOf(chiTiet.getSoLuong())));
	    }

	 //  // ✅ Kiểm tra số điện thoại, nếu tìm thấy khách hàng thì lấy thông tin
	    NguoiDung khachHang = nguoiDungRepository.findBySoDienThoai(soDienThoai).orElse(null);
	    if (khachHang != null) {
	        model.addAttribute("tenKhachHang", khachHang.getTenNguoiDung());
	        model.addAttribute("soDienThoai", khachHang.getSoDienThoai());
	    } else {
	        model.addAttribute("tenKhachHang", "Khách vãng lai");
	        model.addAttribute("soDienThoai", "0000000000");
	    }

	    // ✅ Địa chỉ luôn là "Mua tại quầy KN" dù khách có tài khoản hay không
	    model.addAttribute("diaChiGiaoHang", "Mua tại quầy KN");
	    
	    
	    model.addAttribute("orderItems", orderItems);
	    model.addAttribute("totalPrice", decimalFormat.format(totalPrice) + " VND");
	    model.addAttribute("formattedDiscountPrices", formattedDiscountPrices);

	    return "admin/order/offline-order-confirm";
	}




	@PostMapping("/offline-orders/confirm")
	public String confirmSelectedProducts(
	        @RequestParam(value = "selectedProducts", required = false) List<Integer> selectedProductIds,
	        @RequestParam Map<String, String> allParams,
	        RedirectAttributes redirectAttributes) { 

	    // ✅ Kiểm tra danh sách sản phẩm được chọn
	    if (selectedProductIds == null || selectedProductIds.isEmpty()) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một sản phẩm.");
	        return "redirect:/admin/offline-orders";
	    }

	    // ✅ Xử lý danh sách sản phẩm và số lượng
	    List<Integer> selectedQuantities = new ArrayList<>();
	    for (Integer productId : selectedProductIds) {
	        String quantityStr = allParams.get("quantities[" + productId + "]");
	        int quantity = (quantityStr != null && !quantityStr.isEmpty()) ? Integer.parseInt(quantityStr) : 1;
	        selectedQuantities.add(quantity);
	    }

	    // ✅ Gọi `donHangService` để xử lý đơn hàng offline
	    donHangService.processOfflineOrder(selectedProductIds, selectedQuantities);

	    return "redirect:/admin/offline-orders/confirm";
	}


	
	@PostMapping("/offline-orders/remove")
	public String removeFromOfflineOrder(@RequestParam("sanPhamId") Integer sanPhamId,
	                                     RedirectAttributes redirectAttributes) {
	    donHangService.removeFromOfflineOrder(sanPhamId);
	    redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa khỏi đơn hàng!");
	    return "redirect:/admin/offline-orders/confirm"; 
	}





	@PostMapping("/offline-orders/checkout")
	public String checkoutOfflineOrder(RedirectAttributes redirectAttributes,
	                                   @RequestParam(value = "soDienThoai", required = false) String soDienThoai) {
	    System.out.println("🔵 Số điện thoại nhận được khi checkout: " + soDienThoai);

	    if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
	        soDienThoai = "0000000000"; // Khách vãng lai
	    }

	    boolean isConfirmed = donHangService.processAndGenerateInvoiceForOfflineOrder(soDienThoai);

	    if (isConfirmed) {
	        redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được tạo thành công!");
	        return "redirect:/admin/orders"; 
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo hóa đơn. Vui lòng thử lại.");
	        return "redirect:/admin/offline-orders/confirm";
	    }
	}



	@PostMapping("/offline-orders/check-phone")
	public String checkPhone(@RequestParam(value = "soDienThoai", required = false) String soDienThoai, 
	                         RedirectAttributes redirectAttributes) {
	    System.out.println("🔵 Kiểm tra số điện thoại: " + soDienThoai);

	    if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập số điện thoại hoặc để trống nếu là khách vãng lai.");
	        return "redirect:/admin/offline-orders/confirm";
	    }

	    Optional<NguoiDung> optionalKhachHang = nguoiDungRepository.findBySoDienThoai(soDienThoai);
	    if (optionalKhachHang.isPresent()) {
	        NguoiDung khachHang = optionalKhachHang.get();
	        redirectAttributes.addAttribute("tenKhachHang", khachHang.getTenNguoiDung());
	        redirectAttributes.addAttribute("soDienThoai", khachHang.getSoDienThoai());
	        System.out.println("🟢 Tìm thấy khách hàng: " + khachHang.getTenNguoiDung());
	    } else {
	        redirectAttributes.addAttribute("tenKhachHang", "Khách vãng lai");
	        redirectAttributes.addAttribute("soDienThoai", "0000000000");
	        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng. Tiếp tục với khách vãng lai.");
	        System.out.println("🔴 Không tìm thấy khách hàng -> Khách vãng lai");
	    }
	    return "redirect:/admin/offline-orders/confirm";
	}




	@PostMapping("/offline-orders/add")
	public String addProductToOrder(@RequestParam("sanPhamId") Integer sanPhamId, 
	                                @RequestParam("soLuong") Integer soLuong, 
	                                RedirectAttributes redirectAttributes) {

	    Optional<SanPham> optionalSanPham = sanPhamRepository.findById(sanPhamId);
	    if (optionalSanPham.isPresent()) {
	        donHangService.addToOfflineOrder(optionalSanPham.get(), soLuong);
	        redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được thêm vào đơn hàng!");
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm.");
	    }

	    return "redirect:/admin/offline-orders"; // Giữ nguyên trang
	}








}