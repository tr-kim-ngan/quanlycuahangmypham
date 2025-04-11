package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.ThuongHieu;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.ThuongHieuService;

import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping("/brands")
public class CustomerBrandController {

	@Autowired
	private ThuongHieuService thuongHieuService;

	@Autowired
	private DanhGiaService danhGiaService;

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	@Autowired
	private KiemKeKhoService kiemKeKhoService;
	@Autowired
	private DonHangService donHangService;

	@GetMapping("/all")
	public String getAllBrands(Model model) {
		// Danh sách các chữ cái (A-Z và 0-9)
		List<String> alphabet = new ArrayList<>();
		alphabet.add("0-9");
		for (char c = 'A'; c <= 'Z'; c++) {
			alphabet.add(String.valueOf(c));
		}

		// Lấy danh sách tất cả các thương hiệu từ cơ sở dữ liệu
		List<ThuongHieu> allBrands = thuongHieuService.getAllBrands();

		// Nhóm thương hiệu theo chữ cái đầu tiên
		Map<String, List<ThuongHieu>> groupedBrands = allBrands.stream().collect(Collectors.groupingBy(brand -> {
			String firstChar = brand.getTenThuongHieu().substring(0, 1).toUpperCase();
			return firstChar.matches("[A-Z]") ? firstChar : "0-9";
		}));

		// Sắp xếp nhóm theo thứ tự bảng chữ cái
		Map<String, List<ThuongHieu>> sortedGroupedBrands = new TreeMap<>(groupedBrands);

		// Gửi dữ liệu sang view
		model.addAttribute("alphabet", alphabet);
		model.addAttribute("groupedBrands", sortedGroupedBrands);

		return "customer/allbrands";
	}

	@GetMapping("/{brandId}")
	public String productsByBrand(@PathVariable("brandId") Integer brandId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(value = "minPrice", defaultValue = "0", required = false) BigDecimal minPrice,
			@RequestParam(value = "maxPrice", defaultValue = "999999999", required = false) BigDecimal maxPrice,
			@RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder, Model model,
			Authentication authentication) {

		// Lấy thông tin người dùng hiện tại nếu đăng nhập
		NguoiDung currentUser = null;
		if (authentication != null && authentication.isAuthenticated()) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof NguoiDungDetails) {
				NguoiDungDetails userDetails = (NguoiDungDetails) principal;
				currentUser = userDetails.getNguoiDung();
			}
		}

		if (currentUser != null) {
			model.addAttribute("currentUser", currentUser);
		}
		model.addAttribute("timestamp", System.currentTimeMillis());

		// Lấy thương hiệu theo ID
		ThuongHieu brand = thuongHieuService.findById(brandId);
		if (brand == null) {
			model.addAttribute("errorMessage", "Thương hiệu không tồn tại.");
			return "error";
		}
		model.addAttribute("brand", brand);

		// Lấy danh sách sản phẩm theo thương hiệu
		Page<SanPham> productPage = sanPhamService.findActiveProductsByBrand(brandId, PageRequest.of(page, 1000));
		List<SanPham> products = productPage.getContent();

		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();
		Map<Integer, Integer> sanPhamSoLuongTonKhoMap = new HashMap<>();
		LocalDate today = LocalDate.now();

		List<SanPham> filteredProducts = products.stream().filter(sp -> {
			Integer maSanPham = sp.getMaSanPham();
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);
			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			sanPhamSoLuongTonKhoMap.put(maSanPham, soLuongTonKho);
			
//			return soLuongTonKho > 0 && sp.getDonGiaBan().compareTo(minPrice) >= 0
//					&& sp.getDonGiaBan().compareTo(maxPrice) <= 0;
			return sp.getDonGiaBan().compareTo(minPrice) >= 0
				    && sp.getDonGiaBan().compareTo(maxPrice) <= 0;
		}).peek(sp -> {
			Optional<KhuyenMai> highestCurrentKhuyenMai = sp.getKhuyenMais().stream().filter(km -> km.getTrangThai())
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			BigDecimal giaSauGiam = sp.getDonGiaBan();
			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				sanPhamKhuyenMaiMap.put(sp.getMaSanPham(), highestCurrentKhuyenMai.get());
			}
			sanPhamGiaSauGiamMap.put(sp.getMaSanPham(), giaSauGiam);

			List<DanhGia> danhGias = danhGiaService.findBySanPham(sp);
			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
			sanPhamAverageRatingMap.put(sp.getMaSanPham(), averageRating);
		}).sorted((p1, p2) -> {
			BigDecimal gia1 = sanPhamGiaSauGiamMap.getOrDefault(p1.getMaSanPham(), p1.getDonGiaBan());
			BigDecimal gia2 = sanPhamGiaSauGiamMap.getOrDefault(p2.getMaSanPham(), p2.getDonGiaBan());
			return sortOrder.equals("asc") ? gia1.compareTo(gia2) : gia2.compareTo(gia1);
		}).collect(Collectors.toList());

		int pageSize = 15;
		int start = Math.min(page * pageSize, filteredProducts.size());
		int end = Math.min((page + 1) * pageSize, filteredProducts.size());
		List<SanPham> paginatedProducts = filteredProducts.subList(start, end);

		model.addAttribute("products", paginatedProducts);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);
		model.addAttribute("sanPhamSoLuongTonKhoMap", sanPhamSoLuongTonKhoMap);
		model.addAttribute("page", page);
		model.addAttribute("totalPages", (filteredProducts.size() + pageSize - 1) / pageSize);
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);
		model.addAttribute("sortOrder", sortOrder);

		return "customer/brandProducts";
	}

}
