package com.kimngan.ComesticAdmin.controller.customer;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.GioHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.ShippingFeeConfigService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Controller
@RequestMapping("/customer/order")
public class DonHangController {
	@Autowired
	private HoaDonService hoaDonService;

	@Autowired
	private DonHangService donHangService;

	@Autowired
	private GioHangService gioHangService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;
	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private SanPhamService sanPhamService;
	@Autowired
	private ShippingFeeConfigService shippingFeeConfigService;
	
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
	@GetMapping("/confirm")
	public String confirmOrder(@RequestParam("orderId") Integer orderId, Model model, Principal principal) {
	    if (principal == null) {
	        return "redirect:/customer/login";
	    }
	    
	    // 🔍 Debug kiểm tra orderId
	    System.out.println("🔍 Debug confirmOrder - Order ID nhận vào: " + orderId);

	    DonHang donHang = donHangService.getDonHangById(orderId);
	    
	    // 🔍 Debug kiểm tra donHang
	    System.out.println("🔍 Debug confirmOrder - DonHang từ DB: " + donHang);

	    if (donHang == null) {
	        model.addAttribute("errorMessage", "Không tìm thấy đơn hàng.");
	        return "redirect:/customer/order";
	    }

	    // ✅ Thêm donHang vào model để Thymeleaf có thể sử dụng
	    model.addAttribute("donHang", donHang);
	    
	    return "customer/confirmOrder"; // ✅ Trả về giao diện xác nhận đơn hàng
	}



	@GetMapping
	public String viewOrders(@RequestParam(value = "status", required = false, defaultValue = "all") String status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "2") int size, Principal principal, Model model) {
		if (page < 0) {
			page = 0; // Đảm bảo không để số âm gây lỗi
		}

		if (principal == null) {
			return "redirect:/customer/login";
		}

		String username = principal.getName();
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayDat"));

		Page<DonHang> donHangPage;
		if ("all".equals(status)) {
			// Lấy tất cả đơn hàng
			donHangPage = donHangService.getOrdersByUser(username, pageRequest);
		} else if ("Mới đặt".equals(status)) {
			// Chỉ lấy các đơn hàng có ngày đặt mới nhất
			donHangPage = donHangService.getLatestOrdersByUser(username, pageRequest);
		} else {
			// Lọc theo trạng thái khác
			donHangPage = donHangService.getOrdersByUserAndStatus(username, status, pageRequest);
		}

		model.addAttribute("donHangs", donHangPage.getContent());
		model.addAttribute("currentPage", donHangPage.getNumber());
		model.addAttribute("totalPages", donHangPage.getTotalPages());
		model.addAttribute("selectedStatus", status);
		model.addAttribute("size", size);

		return "customer/order";
	}

	// Phương thức hiển thị chi tiết đơn hàng
	@GetMapping("/{maDonHang}")
	public String viewOrderDetail(@PathVariable Integer maDonHang, Model model) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);

		// Tính giá trị thành tiền cho từng sản phẩm trong đơn hàng
		Map<ChiTietDonHang, BigDecimal> thanhTienMap = new HashMap<>();
		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
			BigDecimal giaSauKhuyenMai = chiTiet.getGiaTaiThoiDiemDat(); // Lấy giá tại thời điểm đặt từ chi tiết đơn
																			// hàng

			// Tính thành tiền cho sản phẩm này
			BigDecimal thanhTien = giaSauKhuyenMai.multiply(BigDecimal.valueOf(chiTiet.getSoLuong()));
			thanhTienMap.put(chiTiet, thanhTien);
		}

		// Đưa `thanhTienMap` vào model để sử dụng trong view
		model.addAttribute("donHang", donHang);
		model.addAttribute("thanhTienMap", thanhTienMap);

		return "customer/order_detail"; // Trả về trang order_detail.html
	}
	// Phương thức hiển thị chi tiết đơn hàng

	@PostMapping("/create")
	public String createOrder(Principal principal, @RequestParam("address") String address,
			@RequestParam("phone") String phone, @RequestParam("phuongThucThanhToan") String phuongThucThanhToan,
			HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
	
		System.out.println("🔍 Debug: BẮT ĐẦU XỬ LÝ TẠO ĐƠN HÀNG");
		if (principal == null) {
			return "redirect:/customer/login";
		}

		try {
			String username = principal.getName();
			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(username);
			List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);
			LocalDate today = LocalDate.now();

			// Kiểm tra giỏ hàng có trống không
			if (cartItems.isEmpty()) {
				model.addAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
				return "redirect:/customer/cart";
			}
			// 🔍 Debug kiểm tra trước khi tạo đơn hàng
	        System.out.println("🔍 Debug: Bắt đầu tạo đơn hàng");

			// Tạo đối tượng DonHang và thiết lập các thông tin ban đầu
			DonHang donHang = new DonHang();
			donHang.setNguoiDung(currentUser);
			donHang.setDiaChiGiaoHang(address);
			donHang.setSdtNhanHang(phone);
			donHang.setNgayDat(LocalDateTime.now());
		//	donHang.setTrangThaiDonHang("Đang xử lý");
			
			
			// 🔍 Kiểm tra khách hàng chọn phương thức nào
	        if ("COD".equals(phuongThucThanhToan)) {
	            donHang.setTrangThaiDonHang("Đang xử lý"); // Trạng thái xử lý ngay khi đặt hàng
	        } else if ("VNPay".equals(phuongThucThanhToan)) {
	            donHang.setTrangThaiDonHang("Chờ thanh toán"); // Chờ khách hàng thanh toán online
	        } else {
	            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ.");
	            return "redirect:/customer/order";
	        }
			  // Lưu đơn hàng vào DB
	   
			 // 🔍 Debug kiểm tra trước khi lưu
	     
	        
			BigDecimal tongGiaTriDonHang = BigDecimal.ZERO;
			//BigDecimal phiVanChuyen = BigDecimal.valueOf(0); // Giá trị phí vận chuyển mặc định

			// Duyệt qua các sản phẩm trong giỏ hàng để tính tổng giá trị đơn hàng trước
			for (ChiTietGioHang cartItem : cartItems) {
				// Tính giá tại thời điểm đặt (bao gồm khuyến mãi nếu có)
				BigDecimal giaTaiThoiDiemDat = cartItem.getSanPham().getDonGiaBan();
				Optional<KhuyenMai> highestCurrentKhuyenMai = cartItem.getSanPham().getKhuyenMais().stream()
						.filter(KhuyenMai::getTrangThai)
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				if (highestCurrentKhuyenMai.isPresent()) {
					BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					giaTaiThoiDiemDat = giaTaiThoiDiemDat
							.subtract(giaTaiThoiDiemDat.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				}

				// Tính thành tiền cho sản phẩm này và cộng vào tổng giá trị đơn hàng
				BigDecimal thanhTien = giaTaiThoiDiemDat.multiply(BigDecimal.valueOf(cartItem.getSoLuong()));
				tongGiaTriDonHang = tongGiaTriDonHang.add(thanhTien);
			}

			   // 🔥 **Tính phí vận chuyển từ bảng cấu hình**
	        BigDecimal phiVanChuyen = shippingFeeConfigService.getShippingFeeForOrder(tongGiaTriDonHang);

			// Đặt tổng giá trị đơn hàng và phí vận chuyển
			tongGiaTriDonHang = tongGiaTriDonHang.add(phiVanChuyen);
			donHang.setTongGiaTriDonHang(tongGiaTriDonHang);
			donHang.setPhiVanChuyen(phiVanChuyen);

			// Lưu đơn hàng lần đầu vào cơ sở dữ liệu
			donHangService.save(donHang);

	        
			// Duyệt qua các sản phẩm trong giỏ hàng và lưu chi tiết đơn hàng
			for (ChiTietGioHang cartItem : cartItems) {
				// Tạo ID tổng hợp cho chi tiết đơn hàng
				ChiTietDonHangId chiTietDonHangId = new ChiTietDonHangId(donHang.getMaDonHang(),
						cartItem.getSanPham().getMaSanPham());

				// Tính giá tại thời điểm đặt (bao gồm khuyến mãi nếu có)
				BigDecimal giaTaiThoiDiemDat = cartItem.getSanPham().getDonGiaBan();
				Optional<KhuyenMai> highestCurrentKhuyenMai = cartItem.getSanPham().getKhuyenMais().stream()
						.filter(KhuyenMai::getTrangThai)
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				BigDecimal phanTramGiam = BigDecimal.ZERO;
				if (highestCurrentKhuyenMai.isPresent()) {
					phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					giaTaiThoiDiemDat = giaTaiThoiDiemDat
							.subtract(giaTaiThoiDiemDat.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				}

				// Tạo chi tiết đơn hàng
				ChiTietDonHang chiTietDonHang = new ChiTietDonHang(chiTietDonHangId, donHang, cartItem.getSanPham(),
						cartItem.getSoLuong(), giaTaiThoiDiemDat, phanTramGiam);

				// Lưu chi tiết đơn hàng vào cơ sở dữ liệu
				chiTietDonHangService.save(chiTietDonHang);
				// Trừ số lượng sản phẩm
				SanPham sanPham = cartItem.getSanPham();
				int soLuongConLai = sanPham.getSoLuong() - cartItem.getSoLuong();
				if (soLuongConLai < 0) {
					throw new IllegalStateException("Số lượng sản phẩm không đủ để đặt hàng.");
				}
				sanPham.setSoLuong(soLuongConLai);
				sanPhamService.update(sanPham);
			}

			// Xóa giỏ hàng sau khi tạo đơn hàng
			gioHangService.clearCart(currentUser);

			
			
	        System.out.println("✅ Đơn hàng đã tạo! Mã đơn hàng: " + donHang.getMaDonHang());

			// 🔹 Nếu chọn COD, xử lý bình thường
	        if ("COD".equals(phuongThucThanhToan)) {
	            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được tạo thành công!");
	            System.out.println("✅ Đơn hàng COD đã tạo! Mã đơn hàng: " + donHang.getMaDonHang());
	            return "redirect:/customer/order";
	        }

	        return "redirect:/customer/order";
		//	return "redirect:/customer/order"; // Chuyển đến danh sách đơn hàng
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "Không thể tạo đơn hàng: " + e.getMessage());
			return "redirect:/customer/cart";
		}
	}
	
	



	// Hủy đơn hàng
	@PostMapping("/cancel")
	public String cancelOrder(@RequestParam("maDonHang") Integer maDonHang, RedirectAttributes redirectAttributes) {
		DonHang donHang = donHangService.getDonHangById(maDonHang);

		if (donHang == null || !"Đang xử lý".equals(donHang.getTrangThaiDonHang())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng này.");
			return "redirect:/customer/order";
		}

		// Cập nhật trạng thái đơn hàng thành "Đã hủy"
		donHang.setTrangThaiDonHang("Đã hủy");
		donHangService.updateDonHang(donHang);

		// Cộng lại số lượng sản phẩm
		for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
			SanPham sanPham = chiTiet.getSanPham();
			int soLuongConLai = sanPham.getSoLuong() + chiTiet.getSoLuong();
			sanPham.setSoLuong(soLuongConLai);
			sanPhamService.update(sanPham);
		}

		redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công.");
		return "redirect:/customer/order";
	}

}
