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
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.ShippingFeeConfigService;
import com.kimngan.ComesticAdmin.services.ThuongHieuService;
import com.kimngan.ComesticAdmin.services.YeuThichService;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;
import com.kimngan.ComesticAdmin.entity.ThuongHieu;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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
	private ThuongHieuService thuongHieuService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private HoaDonService hoaDonService;
	@Autowired
	private ShippingFeeConfigService shippingFeeConfigService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	@Autowired
	private KiemKeKhoService kiemKeKhoService;
	@Autowired
	private DonHangService donHangService;

	@GetMapping({ "/", "/index" })
	public String homeOrIndex(Model model, 
			
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, 
			Authentication authentication) {
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
		// Page<SanPham> sanPhamPage =
		// sanPhamService.getProductsInOrderDetails(pageable);
		// Page<SanPham> sanPhamPage = sanPhamService.findAllActiveWithStock(pageable);
		// Lọc sản phẩm có số lượng > 0
//		List<SanPham> filteredSanPhams = sanPhamPage.getContent().stream().filter(sanPham -> sanPham.getSoLuong() > 0)
//				.collect(Collectors.toList());

		// List<SanPham> filteredSanPhams = sanPhamPage.getContent();
		Page<SanPham> sanPhamPage = sanPhamService.findAllActive(pageable);
		List<SanPham> filteredSanPhams = sanPhamPage.getContent().stream().filter(sp -> {
			Integer maSanPham = sp.getMaSanPham();
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

			// int soLuongTonKho = tongSoLuongNhap - soLuongBan - soLuongTrenKe +
			// deltaKiemKe;
			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null) ? 
					(tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe +soLuongTraHang)

					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe +soLuongTraHang);

			return soLuongTonKho > 0; // Chỉ lấy sản phẩm còn tồn kho
		}).collect(Collectors.toList());

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
		Map<Integer, Integer> sanPhamSoLuongTonKhoMap = new HashMap<>();
//		List<SanPham> sanPhamsKhuyenMai = new ArrayList<>();

//		for (SanPham sanPham : filteredSanPhams) {
//			Integer maSanPham = sanPham.getMaSanPham();
//			// Lấy dữ liệu từ các bảng liên quan
//			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
//			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
//			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
//			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
//			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);
//
//			System.out.println("Sản phẩm ID: " + maSanPham);
//			System.out.println("Tổng số lượng nhập: " + tongSoLuongNhap);
//			System.out.println("Số lượng đã bán: " + soLuongBan);
//			System.out.println("Số lượng trên kệ: " + soLuongTrenKe);
//			System.out.println("Delta kiểm kê: " + deltaKiemKe);
//			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);
//
//			int soLuongTonKho = (tonKhoDaDuyet != null)
//					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe +soLuongTraHang)
//
//					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);
//
////			int soLuongTonKho = tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe;
//			// int soLuongTonKho = sanPhamService.getSoLuongTonKho(sanPham.getMaSanPham());
//			System.out.println("Số lượng tồn kho sau khi tính: " + soLuongTonKho);
//
//			// Tính khuyến mãi
//			Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
//					.filter(km -> km.getTrangThai())
//					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
//							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
//					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));
//
//			BigDecimal giaSauGiam = sanPham.getDonGiaBan();
//			if (highestCurrentKhuyenMai.isPresent()) {
//				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
//				giaSauGiam = giaSauGiam.subtract(giaSauGiam.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
//				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.get());
//				sanPhamsKhuyenMai.add(sanPham); // Thêm sản phẩm có khuyến mãi vào danh sách
//
//			} else {
//				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), null);
//			}
//
//			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);
//
//			// Tính trung bình đánh giá
//			List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);
//			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
//			sanPhamAverageRatingMap.put(sanPham.getMaSanPham(), averageRating);
//
//			// Map thương hiệu
//			ThuongHieu thuongHieu = sanPham.getThuongHieu();
//			if (thuongHieu != null) {
//				sanPhamThuongHieuMap.put(sanPham.getMaSanPham(), thuongHieu.getTenThuongHieu());
//			} else {
//				sanPhamThuongHieuMap.put(sanPham.getMaSanPham(), "Không xác định");
//			}
//			sanPhamSoLuongTonKhoMap.put(sanPham.getMaSanPham(), soLuongTonKho);
//		}
		List<SanPham> allSanPhams = sanPhamService.findAllActive(Pageable.unpaged()).getContent();
		List<SanPham> sanPhamsKhuyenMai = new ArrayList<>();
		List<SanPham> sanPhamsSapKhuyenMai = new ArrayList<>();
		Map<Integer, KhuyenMai> sanPhamSapKhuyenMaiMap = new HashMap<>();

		for (SanPham sanPham : allSanPhams) {
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

			if (soLuongTonKho <= 0) continue;

			//LocalDate today = LocalDate.now();
			Optional<KhuyenMai> sapKhuyenMai = sanPham.getKhuyenMais().stream()
				    .filter(km -> km.getTrangThai())
				    .filter(km -> km.getNgayBatDau().toLocalDate().isAfter(today)) // Sau ngày hiện tại
				    .filter(km -> km.getNgayKetThuc().toLocalDate().isAfter(today))
				    .min(Comparator.comparing(KhuyenMai::getNgayBatDau));

				if (sapKhuyenMai.isPresent()) {
				    sanPhamsSapKhuyenMai.add(sanPham);
				    sanPhamSapKhuyenMaiMap.put(maSanPham, sapKhuyenMai.get()); // ❗ THÊM DÒNG NÀY
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
				sanPhamsKhuyenMai.add(sanPham);
				sanPhamKhuyenMaiMap.put(maSanPham, highestCurrentKhuyenMai.get());
			} else {
				sanPhamKhuyenMaiMap.put(maSanPham, null);
			}

			sanPhamGiaSauGiamMap.put(maSanPham, giaSauGiam);

			List<DanhGia> danhGias = danhGiaService.findBySanPham(sanPham);
			Double averageRating = danhGias.stream().mapToInt(DanhGia::getSoSao).average().orElse(0.0);
			sanPhamAverageRatingMap.put(maSanPham, averageRating);

			ThuongHieu thuongHieu = sanPham.getThuongHieu();
			sanPhamThuongHieuMap.put(maSanPham, thuongHieu != null ? thuongHieu.getTenThuongHieu() : "Không xác định");

			sanPhamSoLuongTonKhoMap.put(maSanPham, soLuongTonKho);
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
	
		
		model.addAttribute("sanPhamsSapKhuyenMai", sanPhamsSapKhuyenMai);
		model.addAttribute("sanPhamSapKhuyenMaiMap", sanPhamSapKhuyenMaiMap);
		System.out.println("Số sản phẩm sắp khuyến mãi: " + sanPhamsSapKhuyenMai.size());

		model.addAttribute("sanPhamsKhuyenMai", sanPhamsKhuyenMai);

		model.addAttribute("categoryGroups", categoryGroups);
		model.addAttribute("brandGroups", brandGroups);

		// Thêm dữ liệu vào model
		model.addAttribute("sanPhamSoLuongTonKhoMap", sanPhamSoLuongTonKhoMap);

		model.addAttribute("sanPhams", filteredSanPhams);
		model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
		model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);
		model.addAttribute("sanPhamAverageRatingMap", sanPhamAverageRatingMap);
		model.addAttribute("sanPhamThuongHieuMap", sanPhamThuongHieuMap);
		model.addAttribute("danhMucs", danhMucs);
		// model.addAttribute("totalPages", sanPhamPage.getTotalPages());
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

		BigDecimal minFreeShipping = shippingFeeConfigService.getAllShippingConfigs().stream()
				.filter(fee -> fee.getMaxOrderValue() != null
						&& fee.getMaxOrderValue().compareTo(BigDecimal.valueOf(400000)) == 0)
				.map(ShippingFeeConfig::getMaxOrderValue).findFirst().orElse(null); // Nếu không có, trả về null

		System.out.println("🔍 DEBUG - minFreeShipping trong ProductController: " + minFreeShipping);

		model.addAttribute("minFreeShipping", minFreeShipping);

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
//			List<SanPham> topCategoryProducts = hoaDonService
//					.findTopSoldProductsByCategory(sanPham.getDanhMuc().getMaDanhMuc(), 4);
//
//			// Loại bỏ sản phẩm hiện tại khỏi danh sách
//			topCategoryProducts = topCategoryProducts.stream()
//					.filter(product -> !product.getMaSanPham().equals(sanPham.getMaSanPham())).limit(4) // Lấy tối đa 4
//																										// sản phẩm
//					.collect(Collectors.toList());
//			
			List<SanPham> topCategoryProducts = sanPhamService
				    .findByDanhMucAndTrangThai(sanPham.getDanhMuc().getMaDanhMuc(), true)
				    .stream()
				    .filter(product -> !product.getMaSanPham().equals(sanPham.getMaSanPham())) // bỏ chính sản phẩm đang xem
				    .limit(4) // lấy đúng 4 sản phẩm bất kỳ
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
				double averageRatingCategory = danhGiasCategory.stream().mapToInt(DanhGia::getSoSao).average()
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

				// Lấy thời gian kết thúc khuyến mãi
				Date ngayKetThuc = highestCurrentKhuyenMai.get().getNgayKetThuc();

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(ngayKetThuc);
				calendar.set(Calendar.HOUR_OF_DAY, 23);
				calendar.set(Calendar.MINUTE, 59);
				calendar.set(Calendar.SECOND, 59);
				calendar.set(Calendar.MILLISECOND, 999);

				long thoiGianKetThuc = calendar.getTimeInMillis(); // Timestamp chính xác đến cuối ngày

				// Lấy thời gian hiện tại
				long thoiGianHienTai = System.currentTimeMillis();
				long timeLeft = thoiGianKetThuc - thoiGianHienTai;

				System.out.println(" Thời gian hiện tại: " + thoiGianHienTai);
				System.out.println(" Thời gian kết thúc khuyến mãi: " + thoiGianKetThuc);
				System.out.println(" Thời gian còn lại (milliseconds): " + timeLeft);

				// Thay đổi điều kiện kiểm tra: Vẫn hiển thị khi còn dưới 1 ngày
				if (timeLeft > 0 || (timeLeft / (1000 * 60 * 60 * 24)) == 0) {
					long days = timeLeft / (1000 * 60 * 60 * 24);
					long hours = (timeLeft / (1000 * 60 * 60)) % 24;
					long minutes = (timeLeft / (1000 * 60)) % 60;
					long seconds = (timeLeft / 1000) % 60;

					days = Math.max(0, days);
					hours = Math.max(0, hours);
					minutes = Math.max(0, minutes);
					seconds = Math.max(0, seconds);

					Map<String, Long> countdown = new HashMap<>();
					countdown.put("days", days);
					countdown.put("hours", hours);
					countdown.put("minutes", minutes);
					countdown.put("seconds", seconds);

					System.out.println(" Dữ liệu gửi sang Thymeleaf: " + countdown);

					model.addAttribute("countdown", countdown);
				} else {
					System.out.println(" Khuyến mãi đã hết hạn hoặc thời gian còn lại bị lỗi.");
					model.addAttribute("countdown", null);
				}
			} else {

				Map<String, Long> countdown = new HashMap<>();
				countdown.put("days", 0L);
				countdown.put("hours", 0L);
				countdown.put("minutes", 0L);
				countdown.put("seconds", 0L);
				model.addAttribute("countdown", countdown);
			}

			sanPhamGiaSauGiamMap.put(sanPham.getMaSanPham(), giaSauGiam);

			model.addAttribute("sanPhamKhuyenMaiMap", sanPhamKhuyenMaiMap);
			model.addAttribute("sanPhamGiaSauGiamMap", sanPhamGiaSauGiamMap);

//			// Lấy danh sách sản phẩm cùng danh mục với sản phẩm hiện tại
//			List<SanPham> relatedSanPhams = sanPhamService
//					.findByDanhMucAndTrangThai(sanPham.getDanhMuc().getMaDanhMuc(), true);
//
//			relatedSanPhams = relatedSanPhams.stream()
//					.filter(relatedSanPham -> !relatedSanPham.getMaSanPham().equals(sanPham.getMaSanPham()))
//					.filter(relatedSanPham -> chiTietDonNhapHangService.existsBySanPham(relatedSanPham))
//					.collect(Collectors.toList());
			List<SanPham> relatedSanPhams = sanPhamService
			        .findByDanhMucAndTrangThai(sanPham.getDanhMuc().getMaDanhMuc(), true)
			        .stream()
			        .filter(relatedSanPham -> !relatedSanPham.getMaSanPham().equals(sanPham.getMaSanPham()))
			        .limit(4)
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

			// Lấy 4 sản phẩm cùng thương hiệu (KHÔNG cần bán chạy)
			List<SanPham> topSoldProducts = sanPhamService
			        .findByThuongHieuAndTrangThai(maThuongHieu, true)
			        .stream()
			        .filter(product -> !product.getMaSanPham().equals(sanPham.getMaSanPham())) // Bỏ sản phẩm hiện tại
			        .limit(4)
			        .collect(Collectors.toList());

			model.addAttribute("topSoldProducts", topSoldProducts);

			
			
			
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
			// Lấy số lượng tồn kho từ SanPhamService

			if (sanPham != null) {
				// Tính số lượng tồn kho chính xác
				Integer maSanPham = sanPham.getMaSanPham();
				// Lấy dữ liệu từ các bảng liên quan
				int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
				int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
				int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
				int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
				int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

//				int soLuongTonKho = tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe;
				Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

				int soLuongTonKho = (tonKhoDaDuyet != null)
						? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)

						: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

				// int soLuongTonKho = sanPhamService.getSoLuongTonKho(productId);

				// Debug kiểm tra số lượng tồn kho
				System.out.println("📦 DEBUG - Sản phẩm ID: " + productId);
				System.out.println(
						"📥 Tổng nhập: " + chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(productId));
				System.out.println("📤 Đã bán: " + chiTietDonHangService.getTotalQuantityBySanPhamId(productId));
				System.out.println("📌 Số lượng trên kệ: " + sanPhamService.getSoLuongTrenKe(productId));
				System.out.println("🔍 Số lượng tồn kho thực tế: " + soLuongTonKho);

				// Truyền số lượng tồn kho vào Model
				model.addAttribute("soLuongTonKho", soLuongTonKho);
			}

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
