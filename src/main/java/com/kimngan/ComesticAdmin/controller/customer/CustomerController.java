package com.kimngan.ComesticAdmin.controller.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.DanhMucService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.YeuThichService;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.DanhGiaRepository;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
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
	private DanhGiaRepository danhGiaRepository;
	
	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@GetMapping({ "/", "/index" })
	public String homeOrIndex(Model model, @RequestParam(defaultValue = "0") int page, Authentication authentication) {
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
			model.addAttribute("currentUser", currentUser); // Thêm currentUser vào model
		}

		model.addAttribute("timestamp", System.currentTimeMillis());

		// Nếu người dùng đã đăng nhập, lấy danh sách sản phẩm yêu thích
		Set<Integer> favoriteProductIds = new HashSet<>();
		if (currentUser != null) {
			favoriteProductIds = yeuThichService.getFavoriteProductIdsForUser(currentUser);
			System.out.println("favoriteProductIds: " + favoriteProductIds);
		}

		model.addAttribute("favoriteProductIds", favoriteProductIds);
		Pageable pageable = PageRequest.of(page, 15);
		Page<SanPham> sanPhams = sanPhamService.getProductsInOrderDetails(pageable);
		LocalDate today = LocalDate.now();

		// Sử dụng Map với maSanPham làm key
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<Integer, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		Map<Integer, Double> sanPhamAverageRatingMap = new HashMap<>();
		// Tính khuyến mãi cao nhất cho từng sản phẩm và giá sau khi giảm
		for (SanPham sanPham : sanPhams) {
			Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
					.filter(km -> km.getTrangThai()) // Chỉ lấy khuyến mãi có trạng thái true
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today)) // Chỉ lấy khuyến mãi còn hạn
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia)); // Lấy khuyến mãi cao nhất

			BigDecimal giaSauGiam = sanPham.getDonGiaBan();
			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.get());
			} else {
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), null);
			}

			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);
			// Lấy danh sách đánh giá sản phẩm
			List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);

			// Tính trung bình số sao
			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0); // Nếu chưa có
																										// đánh giá thì
																										// trả về 0
			sanPhamAverageRatingMap.put(sanPham.getMaSanPham(), averageRating);
		}

		// Lấy danh sách danh mục
		List<DanhMuc> danhMucs = danhMucService.getAll();
		List<DanhMuc> categories = danhMucService.getAll();

		// Tự động chia danh mục thành 2 nhóm: hiển thị và ẩn
		int maxVisible = 4; // Số lượng danh mục hiển thị ban đầu
		List<DanhMuc> visibleDanhMucs = danhMucs.subList(0, Math.min(danhMucs.size(), maxVisible));
		List<DanhMuc> hiddenDanhMucs = danhMucs.size() > maxVisible ? danhMucs.subList(maxVisible, danhMucs.size())
				: new ArrayList<>();

		model.addAttribute("sanPhams", sanPhams);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap); // Map khuyến mãi cao nhất cho từng sản phẩm
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap); // Giá sau khi giảm
		model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);
		model.addAttribute("danhMucs", danhMucs);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", sanPhams.getTotalPages());

		// Thêm vào model để sử dụng trong view
		model.addAttribute("visibleDanhMucs", visibleDanhMucs);
		model.addAttribute("hiddenDanhMucs", hiddenDanhMucs);
		model.addAttribute("categories", categories);

		System.out.println("Danh sách danh mục: " + danhMucs.size());

		return "index"; // Trả về trang index hiển thị tổng quan các sản phẩm // Trả về trang index hiển
						// thị tổng quan các sản phẩm
	}

	// Trang xem chi tiết sản phẩm, không yêu cầu đăng nhập
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

			model.addAttribute("relatedSanPhams", relatedSanPhams);
			model.addAttribute("relatedSanPhamKhuyenMaiMap", relatedSanPhamKhuyenMaiMap);
			model.addAttribute("relatedSanPhamGiaSauGiamMap", relatedSanPhamGiaSauGiamMap);
			model.addAttribute("relatedSanPhamAverageRatingMap", relatedSanPhamAverageRatingMap);

			// Thêm danh sách danh mục để hiển thị trong dropdown tìm kiếm
			List<DanhMuc> categories = danhMucService.getAll();
			model.addAttribute("categories", categories);

			return "customer/productdetail";
		} else {
			return "redirect:/"; // Nếu không tìm thấy sản phẩm, quay lại trang chủ
		}
	}

}
