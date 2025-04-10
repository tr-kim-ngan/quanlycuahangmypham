package com.kimngan.ComesticAdmin.controller.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.YeuThichService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/favorites")
public class YeuThichController {

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private YeuThichService yeuThichService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	@Autowired
	private KiemKeKhoService kiemKeKhoService;
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

	@GetMapping("/index")
	public String viewIndex(Model model, Principal principal) {
		List<Integer> favoriteProductIds = new ArrayList<>();
		if (principal != null) {
			NguoiDung nguoiDung = getCurrentUser(principal);
			List<SanPham> yeuThichList = yeuThichService.getFavoritesByUser(nguoiDung);
			if (yeuThichList != null) {
				favoriteProductIds = yeuThichList.stream().map(SanPham::getMaSanPham).collect(Collectors.toList());
			}
		}
		model.addAttribute("favoriteProductIds", favoriteProductIds);
		return "index";
	}

	@GetMapping
	public String viewFavorites(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		NguoiDung nguoiDung = getCurrentUser(principal);
		List<SanPham> allFavorites = yeuThichService.getFavoritesByUser(nguoiDung);

		Set<Integer> favoriteProductIds = allFavorites != null
				? allFavorites.stream().map(SanPham::getMaSanPham).collect(Collectors.toSet())
				: new HashSet<>();

		LocalDate today = LocalDate.now();
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, Integer> sanPhamSoLuongTonKhoMap = new HashMap<>();

		List<SanPham> yeuThichList = new ArrayList<>();
		Map<Integer, String> sanPhamThuongHieuMap = new HashMap<>();

		// Trong vòng lặp for (SanPham sanPham : allFavorites)
		
		for (SanPham sanPham : allFavorites) {
			Integer maSanPham = sanPham.getMaSanPham();
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);
			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			if (soLuongTonKho > 0) {
				yeuThichList.add(sanPham);
				sanPhamSoLuongTonKhoMap.put(maSanPham, soLuongTonKho);
				if (sanPham.getThuongHieu() != null) {
					sanPhamThuongHieuMap.put(maSanPham, sanPham.getThuongHieu().getTenThuongHieu());
				} else {
					sanPhamThuongHieuMap.put(maSanPham, "Không có thương hiệu");
				}

				Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
						.filter(km -> km.getTrangThai())
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				BigDecimal giaSauGiam = sanPham.getDonGiaBan();
				if (highestCurrentKhuyenMai.isPresent()) {
					BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
					sanPhamKhuyenMaiMap.put(maSanPham, highestCurrentKhuyenMai.get());
				} else {
					sanPhamKhuyenMaiMap.put(maSanPham, null);
				}

				sanPhamGiaSauGiamMap.put(maSanPham, giaSauGiam);
			}
		}

		model.addAttribute("yeuThichList", yeuThichList);
		model.addAttribute("favoriteProductIds", favoriteProductIds);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("sanPhamSoLuongTonKhoMap", sanPhamSoLuongTonKhoMap);
		model.addAttribute("sanPhamThuongHieuMap", sanPhamThuongHieuMap);
		return "customer/favorites";
	}

	@PostMapping("/add")
	@ResponseBody
	public ResponseEntity<?> addFavorite(@RequestParam("sanPhamId") Integer sanPhamId, Principal principal) {
		System.out.println("Request to add favorite received."); // Kiểm tra xem phương thức có được gọi không

		if (principal == null) {
			System.out.println("User not logged in."); // Kiểm tra nếu người dùng chưa đăng nhập
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
		}

		NguoiDung nguoiDung = getCurrentUser(principal);
		SanPham sanPham = getSanPhamById(sanPhamId);

		if (nguoiDung != null && sanPham != null) {
			yeuThichService.addYeuThich(nguoiDung, sanPham);
			System.out.println("Favorite added to database."); // Kiểm tra xem có vào đến đoạn này không
			return ResponseEntity.ok("Added to favorites");
		}

		System.out.println("Failed to add favorite.");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add favorite");
	}

	@PostMapping("/remove")
	public String removeFavorite(@RequestParam("sanPhamId") Integer sanPhamId, Principal principal) {
		if (principal == null) {
			return "redirect:/customer/login";
		}
		System.out.println("xóa favorite for product: " + sanPhamId);
		NguoiDung nguoiDung = getCurrentUser(principal);
		SanPham sanPham = getSanPhamById(sanPhamId);

		if (nguoiDung != null && sanPham != null) {
			yeuThichService.removeYeuThich(nguoiDung, sanPham);
		}

		return "redirect:/product/" + sanPhamId;
	}

	private NguoiDung getCurrentUser(Principal principal) {
		String username = principal.getName();
		return nguoiDungService.findByTenNguoiDung(username);
	}

	private SanPham getSanPhamById(Integer sanPhamId) {
		return sanPhamService.findById(sanPhamId);
	}

}
