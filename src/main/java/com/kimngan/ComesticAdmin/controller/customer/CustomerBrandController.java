package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.kimngan.ComesticAdmin.services.DanhGiaService;
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

//
//	@ModelAttribute
//	public void addAttributes(Model model, Principal principal) {
//		if (principal != null) {
//			String username = principal.getName();
//			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(username);
//			model.addAttribute("currentUser", currentUser);
//			model.addAttribute("timestamp", System.currentTimeMillis());
//		}
//	}

//	@GetMapping("/{brandId}")
//	public String productsByBrand(@PathVariable("brandId") Integer brandId, @RequestParam(defaultValue = "0") int page,
//			Model model, Authentication authentication) {
//
//		// Lấy thông tin người dùng hiện tại nếu đăng nhập
//		NguoiDung currentUser = null;
//		if (authentication != null && authentication.isAuthenticated()) {
//			Object principal = authentication.getPrincipal();
//			if (principal instanceof NguoiDungDetails) {
//				NguoiDungDetails userDetails = (NguoiDungDetails) principal;
//				currentUser = userDetails.getNguoiDung();
//				System.out.println("Current user: " + currentUser.getTenNguoiDung());
//			}
//		}
//
//		// Thêm thông tin người dùng vào model nếu có
//		if (currentUser != null) {
//			model.addAttribute("currentUser", currentUser);
//		}
//		model.addAttribute("timestamp", System.currentTimeMillis()); // Thêm timestamp vào model
//
//		// Lấy thương hiệu theo ID
//		ThuongHieu brand = thuongHieuService.findById(brandId);
//		if (brand == null) {
//			model.addAttribute("errorMessage", "Thương hiệu không tồn tại.");
//			return "error"; // Trang lỗi
//		}
//		model.addAttribute("brand", brand);
//
//		// Lấy danh sách sản phẩm theo thương hiệu
//		Page<SanPham> products = sanPhamService.findActiveProductsByBrand(brandId, PageRequest.of(page, 15));
//		model.addAttribute("products", products.getContent());
//		model.addAttribute("page", products);
//		
//		  // Map chứa giá sau giảm giá
//	    Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
//	    // Map chứa thông tin khuyến mãi
//	    Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
//	    // Map chứa đánh giá trung bình của sản phẩm
//	    Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();
//	    LocalDate today = LocalDate.now();
//	    
//	    for (SanPham sanPham : products.getContent()) {
//	        // Tìm khuyến mãi cao nhất hiện tại
//	        Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
//	                .filter(km -> km.getTrangThai()) // Lọc khuyến mãi đang hoạt động
//	                .filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
//	                        && !km.getNgayKetThuc().toLocalDate().isBefore(today)) // Trong khoảng ngày áp dụng
//	                .max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia)); // Lấy khuyến mãi cao nhất
//
//	        // Tính giá sau giảm
//	        BigDecimal giaSauGiam = sanPham.getDonGiaBan();
//	        if (highestCurrentKhuyenMai.isPresent()) {
//	            BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
//	            giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
//	            sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.get());
//	        }
//	        sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);
//
//	        // Tính đánh giá trung bình
//	        List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);
//	        Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
//	        sanPhamAverageRatingMap.put(sanPham.getMaSanPham(), averageRating);
//	    }
//	    
//	    // Thêm các map vào model
//	    model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
//	    model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
//	    model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);
//
//		
//
//		return "customer/brandProducts";
//	}

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
		Page<SanPham> productPage = sanPhamService.findActiveProductsByBrand(brandId, PageRequest.of(page, 15));
		List<SanPham> products = productPage.getContent();

		// Tính toán giá sau giảm giá và lưu thông tin khuyến mãi
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();
		for (SanPham product : products) {
			List<DanhGia> danhGias = danhGiaService.findBySanPham(product);
			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
			sanPhamAverageRatingMap.put(product.getMaSanPham(), averageRating);
		}
		LocalDate today = LocalDate.now();

		List<SanPham> filteredProducts = products.stream()
				.filter(sp -> sp.getDonGiaBan().compareTo(minPrice) >= 0 && sp.getDonGiaBan().compareTo(maxPrice) <= 0)
				.peek(sp -> {
					Optional<KhuyenMai> highestCurrentKhuyenMai = sp.getKhuyenMais().stream()
							.filter(km -> km.getTrangThai())
							.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
									&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
							.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

					BigDecimal giaSauGiam = sp.getDonGiaBan();
					if (highestCurrentKhuyenMai.isPresent()) {
						BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
						giaSauGiam = giaSauGiam
								.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
						sanPhamKhuyenMaiMap.put(sp.getMaSanPham(), highestCurrentKhuyenMai.get());
					}
					sanPhamGiaSauGiamMap.put(sp.getMaSanPham(), giaSauGiam);

				}).sorted((p1, p2) -> {
					BigDecimal giaSauGiam1 = sanPhamGiaSauGiamMap.getOrDefault(p1.getMaSanPham(), p1.getDonGiaBan());
					BigDecimal giaSauGiam2 = sanPhamGiaSauGiamMap.getOrDefault(p2.getMaSanPham(), p2.getDonGiaBan());
					return sortOrder.equals("asc") ? giaSauGiam1.compareTo(giaSauGiam2)
							: giaSauGiam2.compareTo(giaSauGiam1);
				}).collect(Collectors.toList());

		// Phân trang sau khi lọc
		int pageSize = 15;
		int start = Math.min(page * pageSize, filteredProducts.size());
		int end = Math.min((page + 1) * pageSize, filteredProducts.size());
		List<SanPham> paginatedProducts = filteredProducts.subList(start, end);

		// Thêm dữ liệu vào model
		model.addAttribute("products", paginatedProducts);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("page", page);
		model.addAttribute("totalPages", (filteredProducts.size() + pageSize - 1) / pageSize);
		model.addAttribute("minPrice", minPrice);
		model.addAttribute("maxPrice", maxPrice);
		model.addAttribute("sortOrder", sortOrder);
		model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);

		return "customer/brandProducts";
	}

}
