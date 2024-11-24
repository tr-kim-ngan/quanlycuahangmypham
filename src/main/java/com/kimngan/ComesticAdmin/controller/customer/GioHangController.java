package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietGioHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.GioHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

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
	private ChiTietGioHangService chiTietGioHangService;
	@Autowired
	private DonHangService donHangService;

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

		// Lấy danh sách sản phẩm trong giỏ hàng
		List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(nguoiDung);

		// Tính tổng giá trị và phần trăm giảm giá
		BigDecimal totalPrice = BigDecimal.ZERO;
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, BigDecimal> phanTramGiamMap = new HashMap<>(); // Map để lưu % giảm giá

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

			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);
			totalPrice = totalPrice.add(giaSauGiam.multiply(BigDecimal.valueOf(item.getSoLuong())));
		}

		// Đưa dữ liệu vào model để hiển thị trong cart.html
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("selectedProducts", selectedProducts);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("phanTramGiamMap", phanTramGiamMap); // Thêm % giảm giá vào model

		return "customer/cart";
	}

	@PostMapping("/add")
	public String addToCart(@RequestParam("productId") Integer productId, @RequestParam("quantity") Integer quantity,
			Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();

		// Kiểm tra người dùng đã đăng nhập hay chưa
		if (principal == null) {
			redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm vào giỏ hàng.");
			return "redirect:/login"; // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
		}

		try {
			// Log để kiểm tra giá trị nhận từ form
			System.out.println("ProductId nhận được: " + productId);
			System.out.println("Số lượng nhận được: " + quantity);

			if (quantity < 1) {
				redirectAttributes.addFlashAttribute("error", "Số lượng không hợp lệ.");
				return "redirect:" + request.getHeader("Referer"); // Trở lại trang hiện tại
			}

			// Lấy người dùng hiện tại
			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
			Optional<SanPham> optionalSanPham = sanPhamService.findByIdOptional(productId);

			if (!optionalSanPham.isPresent()) {
				redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại.");
				return "redirect:" + request.getHeader("Referer");
			}

			SanPham sanPham = optionalSanPham.get();
			gioHangService.addToCart(currentUser, sanPham, quantity);

			// Thông báo thành công và quay lại trang hiện tại
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
	public String checkout(Principal principal, Model model, RedirectAttributes redirectAttributes) {
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

		if (cartItems.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
			return "redirect:/customer/cart";
		}

		// Tính tổng giá trị giỏ hàng
		BigDecimal totalPrice = BigDecimal.ZERO;
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, BigDecimal> phanTramGiamMap = new HashMap<>(); // Map để lưu % giảm giá

		LocalDate today = LocalDate.now();
		for (ChiTietGioHang item : cartItems) {
			SanPham sanPham = item.getSanPham();
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

		// Truyền dữ liệu vào model
		model.addAttribute("currentUser", currentUser); // Thêm thông tin người dùng
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("phanTramGiamMap", phanTramGiamMap); // Thêm map phần trăm giảm giá vào model

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

		// Trả về số loại sản phẩm trong giỏ hàng
		return cartItems.size();
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

	// Phương thức tiện ích để lấy người dùng hiện tại
	private NguoiDung getCurrentUser(Principal principal) {
		return nguoiDungService.findByTenNguoiDung(principal.getName());
	}
}
