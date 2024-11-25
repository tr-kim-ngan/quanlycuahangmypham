package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.DanhMucService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.YeuThichService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

	@GetMapping({ "/{maDanhMuc}", "/all", "/", "" })
	public String productsByCategoryOrAll(@PathVariable(value = "maDanhMuc", required = false) Integer maDanhMuc,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder, Model model,
			Authentication authentication) {

		// Lấy thông tin người dùng hiện tại nếu đăng nhập
		NguoiDung currentUser = null;
		if (authentication != null && authentication.isAuthenticated()) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof NguoiDungDetails) {
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

		Page<SanPham> products;
		String selectedCategoryName;

		if (maDanhMuc == null) {
			// Hiển thị tất cả sản phẩm đang hoạt động
			products = sanPhamService.findAllActive(PageRequest.of(page, 15));
			selectedCategoryName = "Tất cả";
		} else {
			// Hiển thị sản phẩm theo danh mục đã chọn
			products = sanPhamService.findActiveProductsInOrderDetailsByCategory(maDanhMuc, PageRequest.of(page, 15));
			DanhMuc selectedCategory = danhMucService.findById(maDanhMuc);
			selectedCategoryName = selectedCategory != null ? selectedCategory.getTenDanhMuc()
					: "Danh mục không tồn tại";
		}

		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();
		LocalDate today = LocalDate.now();

		// Tính toán giá sau giảm và lưu vào map
		for (SanPham sanPham : products.getContent()) {
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

		// Chuyển đổi Page<SanPham> sang List<SanPham> và thực hiện sắp xếp
		List<SanPham> sortedProducts = new ArrayList<>(products.getContent());
		sortedProducts.sort((p1, p2) -> {
			BigDecimal giaSauGiam1 = sanPhamGiaSauGiamMap.getOrDefault(p1.getMaSanPham(), p1.getDonGiaBan());
			BigDecimal giaSauGiam2 = sanPhamGiaSauGiamMap.getOrDefault(p2.getMaSanPham(), p2.getDonGiaBan());
			return sortOrder.equals("asc") ? giaSauGiam1.compareTo(giaSauGiam2) : giaSauGiam2.compareTo(giaSauGiam1);
		});

		// In ra để kiểm tra sắp xếp
		System.out.println("Sorted Products:");
		sortedProducts.forEach(
				sp -> System.out.println(sp.getMaSanPham() + " - " + sanPhamGiaSauGiamMap.get(sp.getMaSanPham())));

		// Phân trang danh sách đã sắp xếp
		int pageSize = 15;
		int start = Math.min(page * pageSize, sortedProducts.size());
		int end = Math.min((page + 1) * pageSize, sortedProducts.size());
		List<SanPham> paginatedProducts = sortedProducts.subList(start, end);

		// Thêm dữ liệu vào model
		model.addAttribute("sanPhams", paginatedProducts);
		model.addAttribute("maDanhMuc", maDanhMuc);
		model.addAttribute("selectedCategory", selectedCategoryName);
		model.addAttribute("categories", danhMucService.getAll());
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("sortOrder", sortOrder); // Để giữ giá trị sắp xếp hiện tại trên giao diện
		model.addAttribute("favoriteProductIds", favoriteProductIds);
		model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);

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
