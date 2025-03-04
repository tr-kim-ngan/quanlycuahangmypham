package com.kimngan.ComesticAdmin.controller.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.DanhMucService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.ThuongHieuService;
import com.kimngan.ComesticAdmin.services.YeuThichService;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.ThuongHieu;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
public class CustomerController {

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private DanhMucService danhMucService;

	@Autowired
	private YeuThichService yeuThichService;

	@Autowired
	private DanhGiaService danhGiaService;

	@Autowired
	private ThuongHieuService thuongHieuService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private HoaDonService hoaDonService;

	@GetMapping({ "/", "/index" })
	public String homeOrIndex(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Authentication authentication) {
		NguoiDung currentUser = null;

		// Kiểm tra người dùng hiện tại
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

		model.addAttribute("timestamp", System.currentTimeMillis());

		// Nếu người dùng đã đăng nhập, lấy danh sách sản phẩm yêu thích
		Set<Integer> favoriteProductIds = new HashSet<>();
		if (currentUser != null) {
			favoriteProductIds = yeuThichService.getFavoriteProductIdsForUser(currentUser);
			System.out.println("favoriteProductIds: " + favoriteProductIds);
		}
		model.addAttribute("favoriteProductIds", favoriteProductIds);

		// Lấy sản phẩm từ phương thức của service
		Pageable pageable = PageRequest.of(page, size);
		//Page<SanPham> sanPhamPage = sanPhamService.getProductsInOrderDetails(pageable);
		Page<SanPham> sanPhamPage = sanPhamService.findAllActiveWithStock(pageable);
		// Lọc sản phẩm có số lượng > 0
//		List<SanPham> filteredSanPhams = sanPhamPage.getContent().stream().filter(sanPham -> sanPham.getSoLuong() > 0)
//				.collect(Collectors.toList());
		List<SanPham> filteredSanPhams = sanPhamPage.getContent();

		// Nếu không có sản phẩm nào sau khi lọc
		if (filteredSanPhams.isEmpty()) {
			model.addAttribute("sanPhams", new ArrayList<>());
			model.addAttribute("totalPages", 0);
			model.addAttribute("currentPage", page);
			model.addAttribute("size", size);
			return "index";
		}

		// Tính toán thông tin cho sản phẩm
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();
		Map<Integer, String> sanPhamThuongHieuMap = new HashMap<>();
		LocalDate today = LocalDate.now();

		for (SanPham sanPham : filteredSanPhams) {
			// Tính khuyến mãi
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
			} else {
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), null);
			}

			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);

			// Tính trung bình đánh giá
			List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);
			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
			sanPhamAverageRatingMap.put(sanPham.getMaSanPham(), averageRating);

			// Map thương hiệu
			ThuongHieu thuongHieu = sanPham.getThuongHieu();
			if (thuongHieu != null) {
				sanPhamThuongHieuMap.put(sanPham.getMaSanPham(), thuongHieu.getTenThuongHieu());
			} else {
				sanPhamThuongHieuMap.put(sanPham.getMaSanPham(), "Không xác định");
			}
		}

		// Lấy danh mục và thêm vào model
		List<DanhMuc> danhMucs = danhMucService.getAll();
		List<DanhMuc> allCategories = danhMucService.getAll();
		List<List<DanhMuc>> categoryGroups = new ArrayList<>();

		int totalCategories = allCategories.size();
		int itemsPerRow = 6;

		// Sắp xếp danh mục thành nhóm
		for (int i = 0; i < totalCategories; i += itemsPerRow) {
			List<DanhMuc> group = new ArrayList<>();
			for (int j = 0; j < itemsPerRow; j++) {
				int index = (i + j) % totalCategories;
				group.add(allCategories.get(index));
			}
			categoryGroups.add(group);
		}

		// Lấy danh sách thương hiệu
		List<ThuongHieu> allBrands = thuongHieuService.getAllBrands();
		List<List<ThuongHieu>> brandGroups = new ArrayList<>();

		int totalBrands = allBrands.size();
		for (int i = 0; i < totalBrands; i += itemsPerRow) {
			List<ThuongHieu> group = new ArrayList<>();
			for (int j = 0; j < itemsPerRow; j++) {
				int index = (i + j) % totalBrands;
				group.add(allBrands.get(index));
			}
			brandGroups.add(group);
		}

		model.addAttribute("categoryGroups", categoryGroups);
		model.addAttribute("brandGroups", brandGroups);

		// Thêm dữ liệu vào model
		model.addAttribute("sanPhams", filteredSanPhams);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);
		model.addAttribute("sanPhamThuongHieuMap", sanPhamThuongHieuMap);
		model.addAttribute("danhMucs", danhMucs);
		//model.addAttribute("totalPages", sanPhamPage.getTotalPages());
		int totalPages = sanPhamPage.getTotalPages();
		if (filteredSanPhams.size() < size && page < totalPages - 1) {
		    totalPages--; // Giảm tổng số trang nếu trang cuối bị thiếu sản phẩm
		}
		model.addAttribute("totalPages", totalPages);

		
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);

		System.out.println("Số lượng sản phẩm trả về: " + filteredSanPhams.size());
		System.out.println("Trang hiện tại: " + page);

		return "index";
	}

	@GetMapping("/product/{id}")
	public String viewProductDetail(@PathVariable("id") Integer productId, Model model, Authentication authentication) {
		// Lấy thông tin người dùng hiện tại nếu đăng nhập
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

		// Lấy thông tin sản phẩm
		SanPham sanPham = sanPhamService.findById(productId);
		if (sanPham != null) {
			model.addAttribute("sanPham", sanPham);
			// Lấy danh sách đánh giá sản phẩm
			List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);
			model.addAttribute("danhGias", danhGias);
			// Tính trung bình số sao
			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0); // Nếu chưa có
																										// đánh giá thì
																										// trả về 0
			model.addAttribute("averageRating", averageRating);

			// Lấy danh sách sản phẩm cùng danh mục được bán nhiều nhất
			List<SanPham> topCategoryProducts = hoaDonService.findTopSoldProductsByCategory(
			        sanPham.getDanhMuc().getMaDanhMuc(), 4);
			
			// Loại bỏ sản phẩm hiện tại khỏi danh sách
			topCategoryProducts = topCategoryProducts.stream()
			        .filter(product -> !product.getMaSanPham().equals(sanPham.getMaSanPham()))
			        .limit(4) // Lấy tối đa 4 sản phẩm
			        .collect(Collectors.toList());
			model.addAttribute("topCategoryProducts", topCategoryProducts);
			Map<Integer, BigDecimal> topCategoryProductsGiaSauGiamMap = new HashMap<>();
			Map<Integer, BigDecimal> topCategoryProductsPhanTramGiamMap = new HashMap<>();

			for (SanPham topCategoryProduct : topCategoryProducts) {
			    Map<String, Object> discount = calculateDiscount(topCategoryProduct);
			    topCategoryProductsGiaSauGiamMap.put(topCategoryProduct.getMaSanPham(),
			            (BigDecimal) discount.get("giaSauGiam"));
			    topCategoryProductsPhanTramGiamMap.put(topCategoryProduct.getMaSanPham(),
			            (BigDecimal) discount.get("phanTramGiam"));
			}
			model.addAttribute("topCategoryProductsGiaSauGiamMap", topCategoryProductsGiaSauGiamMap);
			model.addAttribute("topCategoryProductsPhanTramGiamMap", topCategoryProductsPhanTramGiamMap);

			Map<Integer, Double> topCategoryProductsAverageRatingMap = new HashMap<>();
			for (SanPham topCategoryProduct : topCategoryProducts) {
			    List<DanhGia> danhGiasCategory = danhGiaService.findBySanPham(topCategoryProduct);
			    double averageRatingCategory = danhGiasCategory.stream()
			                                                   .mapToInt(DanhGia::getSoSao)
			                                                   .average()
			                                                   .orElse(0.0);
			    topCategoryProductsAverageRatingMap.put(topCategoryProduct.getMaSanPham(), averageRatingCategory);
			}
			model.addAttribute("topCategoryProductsAverageRatingMap", topCategoryProductsAverageRatingMap);

			Map<Integer, Integer> topCategoryProductsSoLuongBanMap = new HashMap<>();
			for (SanPham topCategoryProduct : topCategoryProducts) {
			    int soLuongBan = hoaDonService.getTotalSoldQuantityByProduct(topCategoryProduct.getMaSanPham());
			    topCategoryProductsSoLuongBanMap.put(topCategoryProduct.getMaSanPham(), soLuongBan);
			}
			model.addAttribute("topCategoryProductsSoLuongBanMap", topCategoryProductsSoLuongBanMap);

			
			
			
			BigDecimal giaSauGiam = sanPham.getDonGiaBan() != null ? sanPham.getDonGiaBan() : BigDecimal.ZERO;

			Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
			Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();

			Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
					.filter(km -> km.getTrangThai())
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(LocalDate.now())
							&& !km.getNgayKetThuc().toLocalDate().isBefore(LocalDate.now()))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.get());
			} else {
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), null);
			}

			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);

			model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
			model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);

			// Lấy danh sách sản phẩm cùng danh mục với sản phẩm hiện tại
			List<SanPham> relatedSanPhams = sanPhamService
					.findByDanhMucAndTrangThai(sanPham.getDanhMuc().getMaDanhMuc(), true);

			relatedSanPhams = relatedSanPhams.stream()
					.filter(relatedSanPham -> !relatedSanPham.getMaSanPham().equals(sanPham.getMaSanPham()))
					.filter(relatedSanPham -> chiTietDonNhapHangService.existsBySanPham(relatedSanPham))
					.collect(Collectors.toList());

			Map<Integer, KhuyenMai> relatedSanPhamKhuyenMaiMap = new HashMap<>();
			Map<Integer, BigDecimal> relatedSanPhamGiaSauGiamMap = new HashMap<>();
			Map<Integer, Double> relatedSanPhamAverageRatingMap = new HashMap<>();

			for (SanPham relatedSanPham : relatedSanPhams) {
				List<DanhGia> relatedDanhGias = danhGiaService.findBySanPham(relatedSanPham);
				Double relatedAverageRating = relatedDanhGias.stream().mapToInt(DanhGia::getSoSao).average()
						.orElse(0.0);
				relatedSanPhamAverageRatingMap.put(relatedSanPham.getMaSanPham(), relatedAverageRating);

				BigDecimal relatedGiaSauGiam = relatedSanPham.getDonGiaBan() != null ? relatedSanPham.getDonGiaBan()
						: BigDecimal.ZERO;
				Optional<KhuyenMai> relatedHighestCurrentKhuyenMai = relatedSanPham.getKhuyenMais().stream()
						.filter(km -> km.getTrangThai())
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(LocalDate.now())
								&& !km.getNgayKetThuc().toLocalDate().isBefore(LocalDate.now()))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				if (relatedHighestCurrentKhuyenMai.isPresent()) {
					BigDecimal relatedPhanTramGiam = relatedHighestCurrentKhuyenMai.get().getPhanTramGiamGia();
					relatedGiaSauGiam = relatedGiaSauGiam
							.subtract(relatedGiaSauGiam.multiply(relatedPhanTramGiam).divide(BigDecimal.valueOf(100)));
					relatedSanPhamKhuyenMaiMap.put(relatedSanPham.getMaSanPham(), relatedHighestCurrentKhuyenMai.get());
				} else {
					relatedSanPhamKhuyenMaiMap.put(relatedSanPham.getMaSanPham(), null);
				}

				relatedSanPhamGiaSauGiamMap.put(relatedSanPham.getMaSanPham(), relatedGiaSauGiam);
			}
			// Lấy thông tin thương hiệu từ sản phẩm
			ThuongHieu thuongHieu = sanPham.getThuongHieu();
			if (thuongHieu != null) {
				model.addAttribute("thuongHieu", thuongHieu.getTenThuongHieu());
			} else {
				model.addAttribute("thuongHieu", "Không xác định");
			}

			model.addAttribute("relatedSanPhams", relatedSanPhams);
			model.addAttribute("relatedSanPhamKhuyenMaiMap", relatedSanPhamKhuyenMaiMap);
			model.addAttribute("relatedSanPhamGiaSauGiamMap", relatedSanPhamGiaSauGiamMap);
			model.addAttribute("relatedSanPhamAverageRatingMap", relatedSanPhamAverageRatingMap);

			// Thêm danh sách danh mục để hiển thị trong dropdown tìm kiếm
			List<DanhMuc> categories = danhMucService.getAll();
			model.addAttribute("categories", categories);

			Integer maThuongHieu = sanPham.getThuongHieu().getMaThuongHieu();

			// Lấy danh sách top sản phẩm bán chạy nhất theo thương hiệu
			List<SanPham> topSoldProducts = hoaDonService.findTopSoldProductsByBrand(maThuongHieu, 7);

			// Loại bỏ sản phẩm hiện tại khỏi danh sách
			topSoldProducts = topSoldProducts.stream()
					.filter(product -> !product.getMaSanPham().equals(sanPham.getMaSanPham())) // Bỏ sản phẩm hiện tại
					.limit(7) // Lấy tối đa 4 sản phẩm
					.collect(Collectors.toList());
			model.addAttribute("topSoldProducts", topSoldProducts);

			// Tính giá sau giảm cho các sản phẩm bán chạy

			Map<Integer, BigDecimal> topSoldSanPhamGiaSauGiamMap = new HashMap<>();
			Map<Integer, BigDecimal> topSoldSanPhamPhanTramGiamMap = new HashMap<>();

			for (SanPham topSoldSanPham : topSoldProducts) {
				Map<String, Object> discount = calculateDiscount(topSoldSanPham);
				topSoldSanPhamGiaSauGiamMap.put(topSoldSanPham.getMaSanPham(), (BigDecimal) discount.get("giaSauGiam"));
				topSoldSanPhamPhanTramGiamMap.put(topSoldSanPham.getMaSanPham(),
						(BigDecimal) discount.get("phanTramGiam"));
			}

			model.addAttribute("topSoldSanPhamGiaSauGiamMap", topSoldSanPhamGiaSauGiamMap);
			model.addAttribute("topSoldSanPhamPhanTramGiamMap", topSoldSanPhamPhanTramGiamMap);
			// Log kiểm tra
			System.out.println("Danh sách sản phẩm trả về từ hoaDonService:");
			for (SanPham sp : topSoldProducts) {
				System.out.println("Tên: " + sp.getTenSanPham() + ", Số lượng bán: " + sp.getSoLuong());
			}
			Map<Integer, Double> topSoldSanPhamAverageRatingMap = new HashMap<>();
			for (SanPham topSoldSanPham : topSoldProducts) {
				List<DanhGia> danhGias1 = danhGiaService.findBySanPham(topSoldSanPham);
				double averageRating1 = danhGias1.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0); // Nếu
																												// không
																												// có
																												// đánh
																												// giá,
																												// trả
																												// về 0
				topSoldSanPhamAverageRatingMap.put(topSoldSanPham.getMaSanPham(), averageRating1);
			}
			model.addAttribute("topSoldSanPhamAverageRatingMap", topSoldSanPhamAverageRatingMap);

			Map<Integer, Integer> topSoldSanPhamSoLuongBanMap = new HashMap<>();
			for (SanPham topSoldSanPham : topSoldProducts) {
			    int soLuongBan = hoaDonService.getTotalSoldQuantityByProduct(topSoldSanPham.getMaSanPham());
			    topSoldSanPhamSoLuongBanMap.put(topSoldSanPham.getMaSanPham(), soLuongBan);
			}
			model.addAttribute("topSoldSanPhamSoLuongBanMap", topSoldSanPhamSoLuongBanMap);

			return "customer/productdetail";
		} else {
			return "redirect:/"; // Nếu không tìm thấy sản phẩm, quay lại trang chủ
		}
	}

	private Map<String, Object> calculateDiscount(SanPham sanPham) {
		Map<String, Object> discountData = new HashMap<>();
		BigDecimal giaBan = sanPham.getDonGiaBan() != null ? sanPham.getDonGiaBan() : BigDecimal.ZERO;
		BigDecimal giaSauGiam = giaBan;

		Optional<KhuyenMai> highestKhuyenMai = sanPham.getKhuyenMais().stream().filter(km -> km.getTrangThai())
				.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(LocalDate.now())
						&& !km.getNgayKetThuc().toLocalDate().isBefore(LocalDate.now()))
				.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

		if (highestKhuyenMai.isPresent()) {
			BigDecimal phanTramGiam = highestKhuyenMai.get().getPhanTramGiamGia();
			giaSauGiam = giaBan.subtract(giaBan.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
			discountData.put("khuyenMai", highestKhuyenMai.get());
			discountData.put("phanTramGiam", phanTramGiam);
		} else {
			discountData.put("khuyenMai", null);
			discountData.put("phanTramGiam", BigDecimal.ZERO);
		}

		discountData.put("giaSauGiam", giaSauGiam);
		return discountData;
	}
}
