package com.kimngan.ComesticAdmin.controller.seller;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
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

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.NguoiDungRepository;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seller")
public class SellerController {

	@Autowired
	private DonHangService donHangService;

	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;
	@Autowired
	private NguoiDungRepository nguoiDungRepository;
	@Autowired
	private SanPhamRepository sanPhamRepository;

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
	public String getOrdersForSeller(Model model,

			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "status", required = false, defaultValue = "all") String status) {

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
			List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER").stream()
					.filter(NguoiDung::isTrangThai) 
					.collect(Collectors.toList());
			model.addAttribute("danhSachShipper", danhSachShipper);
		}
		System.out.println("📌 Ghi chú hiện tại:\n" + donHang.getGhiChu());

		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER").stream().filter(NguoiDung::isTrangThai) 
				.collect(Collectors.toList());
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
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		NguoiDung seller = nguoiDungService.findByTenNguoiDung(userDetails.getUsername());
		donHang.setSeller(seller); // 👈 GÁN NGƯỜI BÁN Ở ĐÂY

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
		List<NguoiDung> danhSachShipper = nguoiDungService.findByRole("SHIPPER").stream().filter(NguoiDung::isTrangThai) 
				.collect(Collectors.toList());

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

	@PostMapping("/orders/{maDonHang}/update-status")
	public String updateOrderStatusFromSeller(@PathVariable("maDonHang") Integer maDonHang,
			@RequestParam(value = "cancelReason", required = false) String cancelReason,
			@RequestParam("status") String action,
			@RequestParam(value = "shipperId", required = false) Integer shipperId,
			RedirectAttributes redirectAttributes, HttpServletRequest request) {

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
					lyDo = request.getParameter("customCancelReason");
				} else {
					lyDo = cancelReason;
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

	@GetMapping("/offline-orders")
	public String showOfflineOrderFormForSeller(
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
			selectedProductIds = Arrays.stream(selectedProductIdsStr.split(",")).map(Integer::parseInt)
					.collect(Collectors.toList());
		}

		if (selectedQuantitiesStr != null && !selectedQuantitiesStr.isEmpty()) {
			selectedQuantities = Arrays.stream(selectedQuantitiesStr.split(",")).map(q -> {
				String[] parts = q.split(":");
				return parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
			}).collect(Collectors.toList());
		}

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

			Optional<KhuyenMai> highestKhuyenMai = sp.getKhuyenMais().stream().filter(KhuyenMai::getTrangThai)
					.filter(km -> km.getNgayBatDau() != null && km.getNgayKetThuc() != null
							&& !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			if (highestKhuyenMai.isPresent() && highestKhuyenMai.get().getPhanTramGiamGia() != null) {
				BigDecimal phanTramGiam = highestKhuyenMai.get().getPhanTramGiamGia();
				BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));

				formattedDiscountPrices.put(sp.getMaSanPham(),
						"<del style='color:grey; font-size:14px;'>" + decimalFormat.format(giaGoc) + " VND</del> "
								+ "<span class='text-danger fw-bold'>" + decimalFormat.format(giaSauGiam)
								+ " VND</span>");
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

		return "seller/order/offline-order";
	}

	@GetMapping("/offline-orders/confirm")
	public String confirmOfflineOrderForSeller(Model model,
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
			return "seller/order/offline-order-confirm";
		}

		LocalDate today = LocalDate.now();
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

		BigDecimal totalPrice = BigDecimal.ZERO;
		Map<Integer, String> formattedDiscountPrices = new HashMap<>();

		for (ChiTietDonHang chiTiet : orderItems) {
			SanPham sp = chiTiet.getSanPham();

			// Load lại danh sách khuyến mãi mới nhất
			sp.setKhuyenMais(new HashSet<>(
					sanPhamRepository.findByIdInWithKhuyenMai(List.of(sp.getMaSanPham())).get(0).getKhuyenMais()));

			Optional<KhuyenMai> highestKhuyenMai = sp.getKhuyenMais().stream().filter(KhuyenMai::getTrangThai)
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

		NguoiDung khachHang = nguoiDungRepository.findBySoDienThoai(soDienThoai).orElse(null);
		if (khachHang != null) {
			model.addAttribute("tenKhachHang", khachHang.getHoTen());
			model.addAttribute("soDienThoai", khachHang.getSoDienThoai());
		} else {
			model.addAttribute("tenKhachHang", "Khách vãng lai");
			model.addAttribute("soDienThoai", "0000000000");
		}

		model.addAttribute("diaChiGiaoHang", "Mua tại quầy KN");
		model.addAttribute("orderItems", orderItems);
		model.addAttribute("totalPrice", decimalFormat.format(totalPrice) + " VND");
		model.addAttribute("formattedDiscountPrices", formattedDiscountPrices);

		return "seller/order/offline-order-confirm";
	}

	@PostMapping("/offline-orders/remove")
	public String removeFromOfflineOrderForSeller(@RequestParam("sanPhamId") Integer sanPhamId,
			RedirectAttributes redirectAttributes) {
		donHangService.removeFromOfflineOrder(sanPhamId);
		redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa khỏi đơn hàng!");
		return "redirect:/seller/offline-orders/confirm";
	}

	@PostMapping("/offline-orders/checkout")
	public String checkoutOfflineOrderForSeller(RedirectAttributes redirectAttributes,
			@RequestParam(value = "soDienThoai", required = false) String soDienThoai) {
		System.out.println("🔵 Số điện thoại nhận được khi checkout (SELLER): " + soDienThoai);

		if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
			soDienThoai = "0000000000"; // Khách vãng lai
		}

		boolean isConfirmed = donHangService.processAndGenerateInvoiceForOfflineOrder(soDienThoai);

		if (isConfirmed) {
			redirectAttributes.addFlashAttribute("successMessage", "Hóa đơn đã được tạo thành công!");
			return "redirect:/seller/orders";
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo hóa đơn. Vui lòng thử lại.");
			return "redirect:/seller/offline-orders/confirm";
		}
	}

	@PostMapping("/offline-orders/check-phone")
	public String checkPhoneForSeller(@RequestParam(value = "soDienThoai", required = false) String soDienThoai,
			RedirectAttributes redirectAttributes) {
		System.out.println("📞 [SELLER] Kiểm tra số điện thoại: " + soDienThoai);

		if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Vui lòng nhập số điện thoại hoặc để trống nếu là khách vãng lai.");
			return "redirect:/seller/offline-orders/confirm";
		}

		Optional<NguoiDung> optionalKhachHang = nguoiDungRepository.findBySoDienThoai(soDienThoai);
		if (optionalKhachHang.isPresent()) {
			NguoiDung khachHang = optionalKhachHang.get();
			redirectAttributes.addAttribute("tenKhachHang", khachHang.getHoTen());
			redirectAttributes.addAttribute("soDienThoai", khachHang.getSoDienThoai());
			System.out.println("✅ [SELLER] Tìm thấy khách hàng: " + khachHang.getTenNguoiDung());
		} else {
			redirectAttributes.addAttribute("tenKhachHang", "Khách vãng lai");
			redirectAttributes.addAttribute("soDienThoai", "0000000000");
			redirectAttributes.addFlashAttribute("errorMessage",
					"Không tìm thấy khách hàng. Tiếp tục với khách vãng lai.");
			System.out.println("❌ [SELLER] Không tìm thấy khách hàng -> Khách vãng lai");
		}

		return "redirect:/seller/offline-orders/confirm";
	}

	@PostMapping("/offline-orders/add")
	public String addProductToOrderForSeller(@RequestParam("sanPhamId") Integer sanPhamId,
			@RequestParam("soLuong") Integer soLuong, RedirectAttributes redirectAttributes) {

		Optional<SanPham> optionalSanPham = sanPhamRepository.findById(sanPhamId);
		if (optionalSanPham.isPresent()) {
			donHangService.addToOfflineOrder(optionalSanPham.get(), soLuong);
			redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được thêm vào đơn hàng!");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm.");
		}

		return "redirect:/seller/offline-orders"; // Giữ nguyên trang seller
	}

}