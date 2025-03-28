package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.DanhMucService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import com.kimngan.ComesticAdmin.services.YeuThichService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

@Controller
@RequestMapping("/category")
public class CustomerCategoryController {

	@Autowired
	private SanPhamService sanPhamService;
	@Autowired
	private DanhGiaService danhGiaService;

	@Autowired
	private DanhMucService danhMucService;
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
	
	
	@GetMapping({ "/{maDanhMuc}", "/all", "/", "" })
	public String productsByCategoryOrAll(
	        @PathVariable(value = "maDanhMuc", required = false) Integer maDanhMuc,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
	        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
	        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
	        Model model,
	        Authentication authentication) {

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

	    Set<Integer> favoriteProductIds = new HashSet<>();
	    if (currentUser != null) {
	        favoriteProductIds = yeuThichService.getFavoriteProductIdsForUser(currentUser);
	    }

	    Page<SanPham> products;
	    String selectedCategoryName;
	    if (minPrice == null) minPrice = BigDecimal.ZERO;
	    if (maxPrice == null) maxPrice = new BigDecimal("999999999");

	    if (maDanhMuc == null) {
	        products = sanPhamService.findAllActiveByPriceRange(minPrice, maxPrice, PageRequest.of(page, 1000));
	        selectedCategoryName = "Tất cả";
	    } else {
	        products = sanPhamService.findActiveProductsByCategoryAndPrice(maDanhMuc, minPrice, maxPrice, PageRequest.of(page, 1000));
	        DanhMuc selectedCategory = danhMucService.findById(maDanhMuc);
	        selectedCategoryName = selectedCategory != null ? selectedCategory.getTenDanhMuc() : "Danh mục không tồn tại";
	    }
	    
	    Map<Integer, Integer> sanPhamSoLuongTonKhoMap = new HashMap<>();

	    List<SanPham> filteredProducts = products.getContent().stream().filter(sp -> {
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
	        return soLuongTonKho > 0;
	    }).collect(Collectors.toList());

	    Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
	    Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
	    Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();
	    Map<Integer, String> sanPhamThuongHieuMap = new HashMap<>();
	    LocalDate today = LocalDate.now();

	    for (SanPham sanPham : filteredProducts) {
	        Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
	                .filter(km -> km.getTrangThai())
	                .filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
	                        && !km.getNgayKetThuc().toLocalDate().isBefore(today))
	                .max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

	        BigDecimal giaSauGiam = sanPham.getDonGiaBan();
	        if (highestCurrentKhuyenMai.isPresent()) {
	            BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
	            giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
	            sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.get());
	        }
	        sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);

	        List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);
	        Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
	        sanPhamAverageRatingMap.put(sanPham.getMaSanPham(), averageRating);

	        if (sanPham.getThuongHieu() != null) {
	            sanPhamThuongHieuMap.put(sanPham.getMaSanPham(), sanPham.getThuongHieu().getTenThuongHieu());
	        } else {
	            sanPhamThuongHieuMap.put(sanPham.getMaSanPham(), "Không có thương hiệu");
	        }
	    }

	    List<SanPham> sortedProducts = new ArrayList<>(filteredProducts);
	    sortedProducts.sort((p1, p2) -> {
	        BigDecimal gia1 = sanPhamGiaSauGiamMap.getOrDefault(p1.getMaSanPham(), p1.getDonGiaBan());
	        BigDecimal gia2 = sanPhamGiaSauGiamMap.getOrDefault(p2.getMaSanPham(), p2.getDonGiaBan());
	        return sortOrder.equals("asc") ? gia1.compareTo(gia2) : gia2.compareTo(gia1);
	    });

	    int pageSize = 15;
	    int start = Math.min(page * pageSize, sortedProducts.size());
	    int end = Math.min((page + 1) * pageSize, sortedProducts.size());
	    List<SanPham> paginatedProducts = sortedProducts.subList(start, end);

	    int totalPages = (int) Math.ceil((double) sortedProducts.size() / pageSize);
	    model.addAttribute("totalPages", totalPages);
	    model.addAttribute("currentPage", page);

	    model.addAttribute("sanPhams", paginatedProducts);
	    model.addAttribute("maDanhMuc", maDanhMuc);
	    model.addAttribute("selectedCategory", selectedCategoryName);
	    model.addAttribute("categories", danhMucService.getAll());
	    model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
	    model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
	    model.addAttribute("sortOrder", sortOrder);
	    model.addAttribute("favoriteProductIds", favoriteProductIds);
	    model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);
	    model.addAttribute("minPrice", minPrice);
	    model.addAttribute("maxPrice", maxPrice);
	    model.addAttribute("sanPhamThuongHieuMap", sanPhamThuongHieuMap);
	    model.addAttribute("sanPhamSoLuongTonKhoMap", sanPhamSoLuongTonKhoMap);

	    return "customer/categoryProduct";
	}


	@GetMapping("/search")
	public String searchProducts(@RequestParam(value = "category", required = false) Integer category,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(defaultValue = "0") int page, Model model, Authentication authentication) {

		NguoiDung currentUser = null;
		if (authentication != null && authentication.isAuthenticated()) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof NguoiDungDetails) {
				// Ép kiểu principal thành NguoiDungDetails và lấy NguoiDung
				NguoiDungDetails userDetails = (NguoiDungDetails) principal;
				currentUser = userDetails.getNguoiDung();
				System.out.println("Current user: " + currentUser.getTenNguoiDung());
			}
		}
		if (currentUser != null) {
			model.addAttribute("currentUser", currentUser);
		}
		model.addAttribute("timestamp", System.currentTimeMillis()); // Thêm timestamp vào Model

		// Nếu người dùng đã đăng nhập, lấy danh sách sản phẩm yêu thích
		Set<Integer> favoriteProductIds = new HashSet<>();
		if (currentUser != null) {
			favoriteProductIds = yeuThichService.getFavoriteProductIdsForUser(currentUser);
			System.out.println("favoriteProductIds: " + favoriteProductIds);
		}

		model.addAttribute("favoriteProductIds", favoriteProductIds);

		Page<SanPham> searchResults;
		if (category == null || category == -1) {
			// Tìm kiếm trong tất cả danh mục
			searchResults = sanPhamService.searchAllActiveProductsWithOrderDetails(keyword, PageRequest.of(page, 15));
			model.addAttribute("selectedCategory", "Tất cả");
		} else {
			// Tìm kiếm theo mã danh mục cụ thể
			searchResults = sanPhamService.searchByCategoryWithOrderDetails(category, keyword,
					PageRequest.of(page, 15));

			// Kiểm tra nếu danh mục không tồn tại
			DanhMuc selectedDanhMuc = danhMucService.findById(category);
			if (selectedDanhMuc != null) {
				model.addAttribute("selectedCategory", selectedDanhMuc.getTenDanhMuc());
			} else {
				model.addAttribute("selectedCategory", "Không xác định"); // Giá trị mặc định nếu danh mục không tồn tại
			}
		}

		// Kiểm tra nếu không có sản phẩm nào trong kết quả tìm kiếm
		if (searchResults.isEmpty()) {
			model.addAttribute("noResultsMessage", "Không có sản phẩm nào được tìm thấy.");
		} else {
			model.addAttribute("sanPhams", searchResults.getContent());
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", searchResults.getTotalPages());
		}

		// Khởi tạo các map cho khuyến mãi và giá sau giảm giá
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();

		LocalDate today = LocalDate.now();

		for (SanPham sanPham : searchResults) {
			Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
					.filter(km -> km.getTrangThai())
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			BigDecimal giaSauGiam = sanPham.getDonGiaBan();
			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.get());
			}
			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);
			// Lấy danh sách đánh giá sản phẩm và tính trung bình số sao
			List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);
			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
			sanPhamAverageRatingMap.put(sanPham.getMaSanPham(), averageRating);
		}

		// Lấy danh sách danh mục và thêm vào model
		List<DanhMuc> categories = danhMucService.getAll();
		model.addAttribute("categories", categories);
		model.addAttribute("category", category);
		model.addAttribute("keyword", keyword);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);

		return "customer/categoryProduct";
	}

}
