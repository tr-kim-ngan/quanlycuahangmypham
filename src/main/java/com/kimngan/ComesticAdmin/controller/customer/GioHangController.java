package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.GioHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.ShippingFeeConfigService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/customer/cart")
public class GioHangController {

	@Autowired
	private GioHangService gioHangService;

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private ShippingFeeConfigService shippingFeeConfigService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;
	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private KiemKeKhoService kiemKeKhoService;

	@Autowired
	private SanPhamRepository sanPhamRepository;
	@Autowired
	private DonHangService donHangService;

	@ModelAttribute
	public void addAttributes(Model model, Principal principal) {
		if (principal != null) {
			// Lấy tên đăng nhập từ Principal
			String username = principal.getName();

			// Tìm thông tin người dùng
			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(username);

			// Thêm thông tin người dùng và timestamp vào Model
			model.addAttribute("currentUser", currentUser);
			model.addAttribute("timestamp", System.currentTimeMillis()); // Timestamp luôn được cập nhật
		}
	}

	@GetMapping
	public String viewCart(Model model, Principal principal,
			@RequestParam(value = "selectedProducts", required = false) List<Integer> selectedProducts) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		// Nếu không có sản phẩm được chọn, khởi tạo danh sách trống
		if (selectedProducts == null) {
			selectedProducts = new ArrayList<>();
		}

		// Lấy thông tin người dùng hiện tại
		NguoiDung nguoiDung = getCurrentUser(principal);


//		List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(nguoiDung).stream()
//				.filter(item -> tinhSoLuongTonKho(item.getSanPham().getMaSanPham()) > 0).collect(Collectors.toList());
//		
		List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(nguoiDung);

		
		// Tính tổng giá trị và phần trăm giảm giá
		BigDecimal totalPrice = BigDecimal.ZERO;
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, BigDecimal> phanTramGiamMap = new HashMap<>(); // Map để lưu % giảm giá
		Map<Integer, Integer> sanPhamSoLuongTonKhoMap = new HashMap<>();

		LocalDate today = LocalDate.now();
		for (ChiTietGioHang item : cartItems) {
			SanPham sanPham = item.getSanPham();
			BigDecimal giaSauGiam = sanPham.getDonGiaBan();

			Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
					.filter(km -> km.getTrangThai())
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				phanTramGiamMap.put(sanPham.getMaSanPham(), phanTramGiam); // Lưu % giảm giá
				giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.get());
			} else {
				phanTramGiamMap.put(sanPham.getMaSanPham(), BigDecimal.ZERO); // Không có giảm giá
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), null);
			}

			// int soLuongTonKho =
			// sanPhamService.getSoLuongTonKho(item.getSanPham().getMaSanPham());
			// int soLuongTonKho = tinhSoLuongTonKho(item.getSanPham().getMaSanPham());

			// sanPhamSoLuongTonKhoMap.put(item.getSanPham().getMaSanPham(), soLuongTonKho);
			Integer sanPhamId = sanPham.getMaSanPham();
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPhamId);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPhamId);
			int soLuongTrenKe = sanPhamRepository.getSoLuongTrenKe(sanPhamId);
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(sanPhamId);
			int soLuongTraHang = donHangService.getSoLuongTraHang(sanPhamId);

			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(sanPhamId);
			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			sanPhamSoLuongTonKhoMap.put(sanPhamId, soLuongTonKho);
			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);
			totalPrice = totalPrice.add(giaSauGiam.multiply(BigDecimal.valueOf(item.getSoLuong())));
		}
		// Kiểm tra xem số lượng tồn kho có đúng không trước khi đưa vào model
		System.out.println("📦 [DEBUG] Số lượng tồn kho thực tế:");
		for (Map.Entry<Integer, Integer> entry : sanPhamSoLuongTonKhoMap.entrySet()) {
		    System.out.println("🛒 Sản phẩm ID: " + entry.getKey() + " | Tồn kho: " + entry.getValue());
		}

		// Đưa dữ liệu vào model để hiển thị trong cart.html
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("selectedProducts", selectedProducts);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("phanTramGiamMap", phanTramGiamMap); // Thêm % giảm giá vào model
		model.addAttribute("sanPhamSoLuongTonKhoMap", sanPhamSoLuongTonKhoMap);

		return "customer/cart";
	}

	@PostMapping("/add")
	public String addToCart(@RequestParam("productId") Integer productId,
			@RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
			Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request) {

		// Kiểm tra nếu quantity null hoặc nhỏ hơn 1 thì đặt lại thành 1
		if (quantity == null || quantity < 1) {
			quantity = 1;
		}

		// Kiểm tra người dùng đã đăng nhập hay chưa
		if (principal == null) {
			redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm vào giỏ hàng.");
			return "redirect:/login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
		}

		try {
			System.out.println("ProductId nhận được: " + productId);
			System.out.println("Số lượng nhận được: " + quantity);

			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
			Optional<SanPham> optionalSanPham = sanPhamService.findByIdOptional(productId);

			if (!optionalSanPham.isPresent()) {
				redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại.");
				return "redirect:" + request.getHeader("Referer");
			}

			SanPham sanPham = optionalSanPham.get();
			Integer sanPhamId = sanPham.getMaSanPham();
			// Lấy số lượng tồn kho thực tế
			// int soLuongTonKho = sanPhamService.getSoLuongTonKho(sanPham.getMaSanPham());
			// int soLuongTonKho = tinhSoLuongTonKho(sanPham.getMaSanPham());
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPhamId);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPhamId);
			int soLuongTrenKe = sanPhamRepository.getSoLuongTrenKe(sanPhamId);
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(sanPhamId);
			int soLuongTraHang = donHangService.getSoLuongTraHang(sanPhamId);

			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(sanPhamId);
			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			System.out.println("📦 DEBUG - Số lượng tồn kho thực tế: " + soLuongTonKho);

			// Kiểm tra số lượng đặt hàng có vượt quá số lượng tồn kho không
			if (quantity > soLuongTonKho) {
				System.out.println("⚠️ Không thể thêm vào giỏ hàng! Số lượng tồn kho không đủ.");
				quantity = soLuongTonKho; // Giới hạn số lượng đặt hàng theo số lượng tồn kho
			}

			gioHangService.addToCart(currentUser, sanPham, quantity);

			redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được thêm vào giỏ hàng!");
			return "redirect:" + request.getHeader("Referer");

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Lỗi trong quá trình thêm sản phẩm vào giỏ hàng.");
			return "redirect:" + request.getHeader("Referer");
		}
	}

	@PostMapping("/remove")
	public String removeFromCart(@RequestParam("sanPhamId") Integer sanPhamId, Principal principal,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		SanPham sanPham = sanPhamService.findById(sanPhamId);

		gioHangService.removeFromCart(currentUser, sanPham);
		redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa khỏi giỏ hàng!");

		return "redirect:/customer/cart";
	}

	@GetMapping("/count")
	@ResponseBody
	public Integer getCartItemCount(Principal principal) {
		if (principal != null) {
			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
			List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);
			return cartItems.size();
		}
		return 0;
	}

	@PostMapping("/select-all")
	public String selectAllItems(Principal principal, RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);

		List<Integer> selectedProductIds = cartItems.stream().map(item -> item.getSanPham().getMaSanPham())
				.collect(Collectors.toList());

		redirectAttributes.addFlashAttribute("selectedProducts", selectedProductIds);
		return "redirect:/customer/cart";
	}

	@PostMapping("/checkout")
	public String checkout(Principal principal, Model model,

			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		// Lấy thông tin người dùng hiện tại
		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		if (currentUser == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể tìm thấy thông tin người dùng.");
			return "redirect:/customer/cart";
		}

		// Lấy giỏ hàng
		List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);
		System.out.println("🛒 [Debug] Số lượng sản phẩm trong giỏ: " + cartItems.size());

		if (cartItems.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
			return "redirect:/customer/cart";
		} else {
			System.out.println("Cart items found: " + cartItems.size());
		}

		// Kiểm tra số lượng còn lại và tính tổng giá trị giỏ hàng
		BigDecimal totalPrice = BigDecimal.ZERO;
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, BigDecimal> phanTramGiamMap = new HashMap<>(); // Map để lưu % giảm giá

		LocalDate today = LocalDate.now();
		for (ChiTietGioHang item : cartItems) {
			SanPham sanPham = item.getSanPham();
			Integer maSanPham = sanPham.getMaSanPham();

			// ✅ Tính số lượng tồn kho trực tiếp
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sanPhamRepository.getSoLuongTrenKe(maSanPham);
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

//	        int soLuongTonKho = tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe;
			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)

					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			// int soLuongTonKho = sanPhamService.getSoLuongTonKho(sanPham.getMaSanPham());

			// Kiểm tra số lượng tồn kho
			System.out.println("📦 [Debug] Kiểm tra sản phẩm: " + sanPham.getTenSanPham());
			System.out.println("🔢 [Debug] Số lượng tồn kho: " + soLuongTonKho);
			System.out.println("🛒 [Debug] Số lượng cần đặt: " + item.getSoLuong());
			System.out.println("🛒 [Debug] KHÁCH HÀNG CHECKOUT - Thời gian: " + LocalDateTime.now());
			System.out.println("📦 [Debug] Sản phẩm: " + sanPham.getTenSanPham());
			System.out.println("🔢 [Debug] Tồn kho tại thời điểm checkout: " + soLuongTonKho);

			// Kiểm tra số lượng tồn kho
			if (item.getSoLuong() > soLuongTonKho) {
				redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm '" + sanPham.getTenSanPham()
						+ "' không đủ số lượng tồn kho. Vui lòng điều chỉnh lại số lượng trong giỏ hàng.");
				return "redirect:/customer/cart";
			}

			BigDecimal giaSauGiam = sanPham.getDonGiaBan();

			// Kiểm tra khuyến mãi
			Optional<KhuyenMai> khuyenMaiOptional = sanPham.getKhuyenMais().stream().filter(KhuyenMai::getTrangThai)
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			if (khuyenMaiOptional.isPresent()) {
				BigDecimal phanTramGiam = khuyenMaiOptional.get().getPhanTramGiamGia();
				phanTramGiamMap.put(sanPham.getMaSanPham(), phanTramGiam); // Lưu % giảm giá
				giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
			} else {
				phanTramGiamMap.put(sanPham.getMaSanPham(), BigDecimal.ZERO); // Không có giảm giá
			}

			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);
			totalPrice = totalPrice.add(giaSauGiam.multiply(BigDecimal.valueOf(item.getSoLuong())));
		}

		System.out.println("💰 [Debug] Tổng tiền sản phẩm: " + totalPrice);

		List<ShippingFeeConfig> shippingConfigs = shippingFeeConfigService.getAllShippingConfigs();
		BigDecimal shippingFee = BigDecimal.ZERO;
		for (ShippingFeeConfig config : shippingConfigs) {
			if (config.getMinOrderValue().compareTo(totalPrice) <= 0
					&& (config.getMaxOrderValue() == null || config.getMaxOrderValue().compareTo(totalPrice) >= 0)) {
				shippingFee = config.getShippingFee();
				break;
			}
		}

		System.out.println("🚚 [Debug] Phí vận chuyển áp dụng từ CSDL: " + shippingFee);
		// Tính tổng tiền đơn hàng sau khi cộng phí vận chuyển
		BigDecimal finalTotal = totalPrice.add(shippingFee);
		System.out.println("🛒 [Debug] Tổng tiền đơn hàng (bao gồm phí vận chuyển): " + finalTotal);

		// Định dạng số tiền trước khi hiển thị
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

		model.addAttribute("formattedTotalPrice", decimalFormat.format(totalPrice));
		model.addAttribute("formattedShippingFee", decimalFormat.format(shippingFee));
		model.addAttribute("formattedFinalTotal", decimalFormat.format(finalTotal));
		// Truyền dữ liệu vào model
		model.addAttribute("currentUser", currentUser); // Thêm thông tin người dùng
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("phanTramGiamMap", phanTramGiamMap); // Thêm map phần trăm giảm giá vào model
		model.addAttribute("finalTotal", finalTotal);
		model.addAttribute("shippingFee", shippingFee);

		System.out.println("✅ [Debug] Hoàn tất checkout. Chuyển đến confirmOrder.html");

		return "customer/confirmOrder"; // Chuyển đến trang confirmOrder
	}

	@PostMapping("/update-quantity")
	public String updateCartItemQuantity(@RequestParam("sanPhamId") Integer sanPhamId,
			@RequestParam("quantity") Integer newQuantity, Principal principal, RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		SanPham sanPham = sanPhamService.findById(sanPhamId);
		int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPhamId);
		int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPhamId);
		int soLuongTrenKe = sanPhamRepository.getSoLuongTrenKe(sanPhamId);
		int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(sanPhamId);
		int soLuongTraHang = donHangService.getSoLuongTraHang(sanPhamId);

		Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(sanPhamId);

		int soLuongTonKho = (tonKhoDaDuyet != null)
				? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
				: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

		// Kiểm tra nếu số lượng mới vượt quá số lượng tồn kho
		if (newQuantity > soLuongTonKho) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Số lượng sản phẩm trong giỏ hàng không được vượt quá số lượng tồn kho (" + soLuongTonKho + ").");
			return "redirect:/customer/cart";
		}
		// Gọi service để cập nhật số lượng sản phẩm
		gioHangService.updateCartItemQuantity(currentUser, sanPham, newQuantity);

		redirectAttributes.addFlashAttribute("successMessage", "Số lượng đã được cập nhật thành công!");
		return "redirect:/customer/cart";
	}

	@PostMapping("/calculate-total")
	@ResponseBody
	public BigDecimal calculateTotal(@RequestBody List<Integer> selectedProducts) {
		if (selectedProducts == null || selectedProducts.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal total = BigDecimal.ZERO;
		for (Integer productId : selectedProducts) {
			Optional<SanPham> sanPhamOptional = sanPhamService.findByIdOptional(productId);
			if (sanPhamOptional.isPresent()) {
				SanPham sanPham = sanPhamOptional.get();
				BigDecimal giaSauGiam = sanPham.getDonGiaBan();

				Optional<KhuyenMai> khuyenMaiOptional = sanPham.getKhuyenMais().stream().filter(KhuyenMai::getTrangThai)
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				if (khuyenMaiOptional.isPresent()) {
					BigDecimal discount = giaSauGiam.multiply(khuyenMaiOptional.get().getPhanTramGiamGia())
							.divide(BigDecimal.valueOf(100));
					giaSauGiam = giaSauGiam.subtract(discount);
				}
				total = total.add(giaSauGiam.multiply(BigDecimal.valueOf(1))); // Thay 1 bằng số lượng thực tế
			}
		}

		return total;
	}

	@GetMapping("/total-items")
	@ResponseBody
	public Integer getTotalItemsInCart(Principal principal) {
		if (principal == null) {
			return 0; // Nếu người dùng chưa đăng nhập, trả về 0
		}

		NguoiDung currentUser = getCurrentUser(principal);
		List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);
		// Tính tổng số lượng tất cả sản phẩm trong giỏ hàng
		// Đếm số loại sản phẩm trong giỏ hàng (không tính tổng số lượng)
		int totalItems = cartItems.size();

		// Debug kiểm tra
		System.out.println("🛒 Số loại sản phẩm trong giỏ hàng: " + totalItems);

		return totalItems; // ✅ Trả về tổng số lượng

	}

	@ModelAttribute("cartItems")
	public List<ChiTietGioHang> getCartItems(Principal principal) {
		if (principal != null) {
			NguoiDung currentUser = getCurrentUser(principal);
			List<ChiTietGioHang> items = gioHangService.viewCartItems(currentUser);
			System.out.println("Cart items: " + items);
			return items;
		}
		return new ArrayList<>();
	}

	private NguoiDung getCurrentUser(Principal principal) {
		return nguoiDungService.findByTenNguoiDung(principal.getName());
	}

	private int tinhSoLuongTonKho(Integer maSanPham) {
		int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
		int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
		int soLuongTrenKe = sanPhamRepository.getSoLuongTrenKe(maSanPham);
		int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
		int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

		Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

		int soLuongTonKho = (tonKhoDaDuyet != null)
				? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)

				: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

		return soLuongTonKho;
		// return tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe;
	}

}
