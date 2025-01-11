package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.ThuongHieu;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHangId;
import com.kimngan.ComesticAdmin.entity.DanhMuc;

import com.kimngan.ComesticAdmin.entity.DonViTinh;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.NhaCungCap;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DanhMucService;

import com.kimngan.ComesticAdmin.services.DonViTinhService;
import com.kimngan.ComesticAdmin.services.NhaCungCapService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.StorageService;
import com.kimngan.ComesticAdmin.services.ThuongHieuService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

@Controller
@RequestMapping("/admin")
public class ProductController {

	@Autowired
	private SanPhamService sanPhamService;
	@Autowired
	private DanhMucService danhMucService;
	@Autowired
	private StorageService storageService;

	@Autowired
	private NhaCungCapService nhaCungCapService;
	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private DonViTinhService donViTinhService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	@Autowired
	private ThuongHieuService thuongHieuService;

	// Hiển thị danh sách sản phẩm
	@GetMapping("/product")
	public String index(HttpServletRequest request, Model model,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "15") int size,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "category", required = false) Integer maDanhMuc

	) {

		Page<SanPham> pageSanPham = Page.empty();

		if (maDanhMuc != null && maDanhMuc > 0) {
			if (keyword != null && !keyword.isEmpty()) {
				// Tìm kiếm theo danh mục và từ khóa
				pageSanPham = sanPhamService.searchByCategoryAndName(maDanhMuc, keyword, PageRequest.of(page, size));
				model.addAttribute("category", maDanhMuc);
				model.addAttribute("keyword", keyword);
			} else {
				// Tìm kiếm theo danh mục và trạng thái
				pageSanPham = sanPhamService.findByDanhMucAndTrangThaiWithPagination(maDanhMuc, true,
						PageRequest.of(page, size));
				model.addAttribute("category", maDanhMuc);
			}
		} else if (keyword != null && !keyword.isEmpty()) {
			// Tìm kiếm theo từ khóa sản phẩm
			pageSanPham = sanPhamService.searchActiveByName(keyword, PageRequest.of(page, size));
			model.addAttribute("keyword", keyword);
		} else {
			// Tìm tất cả sản phẩm hoạt động
			pageSanPham = sanPhamService.findAllActive(PageRequest.of(page, size));
		}

		// Kiểm tra nếu không có sản phẩm nào được tìm thấy
		if (pageSanPham.isEmpty()) {
			model.addAttribute("errorMessage", "Không có sản phẩm nào được tìm thấy");
		}

		List<SanPham> sanPhams = pageSanPham.getContent();
		LocalDate today = LocalDate.now();

		// Sử dụng Map với maSanPham làm key
		Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
		Map<SanPham, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		Map<Integer, String> formattedPrices = new HashMap<>();
		Map<Integer, String> formattedDiscountPrices = new HashMap<>();
		for (SanPham sanPham : sanPhams) {
			// Định dạng giá bán
			// Kiểm tra và định dạng giá gốc
			if (sanPham.getDonGiaBan() != null) {
				String formattedPrice = decimalFormat.format(sanPham.getDonGiaBan()) + " VND";
				formattedPrices.put(sanPham.getMaSanPham(), formattedPrice);
			} else {
				formattedPrices.put(sanPham.getMaSanPham(), "0.00 VND");
			}

			// Tìm khuyến mãi cao nhất hiện tại có trạng thái true
			Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
					.filter(km -> km.getTrangThai()) // Chỉ lấy khuyến mãi có trạng thái true
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			// Thêm vào map với maSanPham làm key
			sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.orElse(null));

			// Tính giá sau khi giảm
			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal giaGoc = sanPham.getDonGiaBan();
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(new BigDecimal(100)));

				// Định dạng giá sau khi giảm
				String formattedDiscountPrice = decimalFormat.format(giaSauGiam) + " VND";
				formattedDiscountPrices.put(sanPham.getMaSanPham(), formattedDiscountPrice);
			} else {
				// Nếu không có khuyến mãi, giá sau khi giảm = giá gốc
				formattedDiscountPrices.put(sanPham.getMaSanPham(), formattedPrices.get(sanPham.getMaSanPham()));
			}

		}

		// model.addAttribute("listSanPham", sanPhams);
		model.addAttribute("highestKhuyenMais", sanPhamKhuyenMaiMap);
		model.addAttribute("giaSauGiams", sanPhamGiaSauGiamMap);
		model.addAttribute("listSanPham", sanPhams);
		model.addAttribute("listSanPham", pageSanPham.getContent());
		model.addAttribute("currentPage", pageSanPham.getNumber());
		model.addAttribute("totalPages", pageSanPham.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/admin/product"); // Đường dẫn cho tìm kiếm
		model.addAttribute("formattedPrices", formattedPrices);
		model.addAttribute("formattedDiscountPrices", formattedDiscountPrices);

		// Thêm thông tin danh mục cho dropdown
		// Thêm danh mục cho dropdown
		List<DanhMuc> listDanhMuc = danhMucService.getAll(); // Không cần kiểm tra trạng thái
		model.addAttribute("listDanhMuc", listDanhMuc);

		// Thêm requestUri vào model để sử dụng trong header
		model.addAttribute("requestUri", request.getRequestURI());
		// Thêm thông tin người dùng để hiển thị

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/index";
	}

	// Trang thêm sản phẩm
	// Trang thêm sản phẩm
	@GetMapping("/add-product")
	public String addProduct(Model model, @RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

		// Nếu có từ khóa tìm kiếm, thực hiện tìm kiếm và trả về trang danh sách sản
		// phẩm
		if (keyword != null && !keyword.isEmpty()) {
			Page<SanPham> pageSanPham = sanPhamService.searchActiveByName(keyword, PageRequest.of(page, size));
			List<SanPham> sanPhams = pageSanPham.getContent();
			LocalDate today = LocalDate.now();

			Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
			Map<SanPham, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();

			for (SanPham sanPham : sanPhams) {
				Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
						.filter(km -> km.getTrangThai()) // Chỉ lấy khuyến mãi có trạng thái true
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.orElse(null));

				if (highestCurrentKhuyenMai.isPresent()) {
					BigDecimal giaGoc = sanPham.getDonGiaBan();
					BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(new BigDecimal(100)));
					sanPhamGiaSauGiamMap.put(sanPham, giaSauGiam);
				} else {
					sanPhamGiaSauGiamMap.put(sanPham, sanPham.getDonGiaBan());
				}
			}

			model.addAttribute("highestKhuyenMais", sanPhamKhuyenMaiMap);
			model.addAttribute("giaSauGiams", sanPhamGiaSauGiamMap);
			model.addAttribute("listSanPham", sanPhams);
			model.addAttribute("currentPage", pageSanPham.getNumber());
			model.addAttribute("totalPages", pageSanPham.getTotalPages());
			model.addAttribute("size", size);
			model.addAttribute("searchAction", "/admin/add-product");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);

			// Trả về trang danh sách sản phẩm với kết quả tìm kiếm
			return "admin/product/index";
		}
		// Lấy danh sách thương hiệu
		List<ThuongHieu> listThuongHieu = thuongHieuService.getAll();
		model.addAttribute("listThuongHieu", listThuongHieu);
		// Nếu không có từ khóa, trả về trang thêm sản phẩm
		List<NhaCungCap> activeSuppliers = nhaCungCapService.findByTrangThaiTrue();
		model.addAttribute("activeSuppliers", activeSuppliers);

		model.addAttribute("sanPham", new SanPham());
		model.addAttribute("listDonViTinh", donViTinhService.getAll());
		model.addAttribute("listDanhMuc", danhMucService.getAll());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/add"; // Trả về trang thêm sản phẩm
	}

	@PostMapping("/add-product")
	public String saveProduct(@ModelAttribute("sanPham") SanPham sanPham,
			@RequestParam("donViTinh") Integer donViTinhId,
			@RequestParam(value = "thuongHieuId", required = false) Integer thuongHieuId,
			@RequestParam(value = "nhaCungCapId", required = false) Integer nhaCungCapId,
			@RequestParam("imageFile") MultipartFile imageFile, Model model) {
		sanPham.setSoLuong(0);
		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Tìm nhà cung cấp theo ID
		// NhaCungCap nhaCungCap = nhaCungCapService.findById(nhaCungCapId);

		// Liên kết nhà cung cấp với sản phẩm
		// sanPham.setNhaCungCaps(Collections.singleton(nhaCungCap));
		// Kiểm tra nếu `nhaCungCapId` không null, thì mới tìm và gán nhà cung cấp cho
		// sản phẩm
		if (nhaCungCapId != null) {
			NhaCungCap nhaCungCap = nhaCungCapService.findById(nhaCungCapId);
			if (nhaCungCap != null) {
				// Liên kết nhà cung cấp với sản phẩm
				sanPham.setNhaCungCaps(Collections.singleton(nhaCungCap));
			}
		}
		// Kiểm tra nếu sản phẩm với tên đã tồn tại và đang hoạt động
		if (sanPhamService.existsByTenSanPham(sanPham.getTenSanPham())) {
			model.addAttribute("errorMessage", "Sản phẩm đã tồn tại!");
			return "admin/product/add"; // Quay lại trang thêm sản phẩm nếu đã tồn tại
		}
		// Gán thương hiệu cho sản phẩm
		if (thuongHieuId != null) {
			ThuongHieu thuongHieu = thuongHieuService.findById(thuongHieuId);
			if (thuongHieu != null) {
				sanPham.setThuongHieu(thuongHieu);
			}
		}
		// Xử lý lưu ảnh sản phẩm
		if (!imageFile.isEmpty()) {
			try {
				String fileName = storageService.storeFile(imageFile);
				sanPham.setHinhAnh(fileName);
			} catch (IOException e) {
				e.printStackTrace();
				return "admin/product/add";
			}
		}

		// Tìm DonViTinh theo id
		Optional<DonViTinh> donViTinhOpt = donViTinhService.findById(donViTinhId);
		if (donViTinhOpt.isPresent()) {
			sanPham.setDonViTinh(donViTinhOpt.get()); // Gán DonViTinh cho sản phẩm
		} else {
			// Nếu không tìm thấy DonViTinh, xử lý lỗi
			return "admin/product/add";
		}

		// Lưu sản phẩm trước tiên
		SanPham savedSanPham = sanPhamService.create(sanPham);
		if (savedSanPham != null) {

			return "redirect:/admin/product";
		} else {
			return "admin/product/add";
		}
	}

	@GetMapping("/edit-product/{id}")
	public String editProductPage(@PathVariable("id") Integer id, Model model,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

		// Nếu có từ khóa tìm kiếm và không rỗng, thực hiện tìm kiếm và trả về trang
		// danh sách sản phẩm
		if (keyword != null && !keyword.isEmpty()) {
			Page<SanPham> pageSanPham = sanPhamService.searchActiveByName(keyword, PageRequest.of(page, size));
			List<SanPham> sanPhams = pageSanPham.getContent();
			LocalDate today = LocalDate.now();

			Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
			Map<SanPham, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();

			for (SanPham sanPham : sanPhams) {
				Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
						.filter(km -> km.getTrangThai()) // Chỉ lấy khuyến mãi có trạng thái true
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.orElse(null));

				if (highestCurrentKhuyenMai.isPresent()) {
					BigDecimal giaGoc = sanPham.getDonGiaBan();
					BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(new BigDecimal(100)));
					sanPhamGiaSauGiamMap.put(sanPham, giaSauGiam);
				} else {
					sanPhamGiaSauGiamMap.put(sanPham, sanPham.getDonGiaBan());
				}
			}

			model.addAttribute("highestKhuyenMais", sanPhamKhuyenMaiMap);
			model.addAttribute("giaSauGiams", sanPhamGiaSauGiamMap);
			model.addAttribute("listSanPham", sanPhams);
			model.addAttribute("currentPage", pageSanPham.getNumber());
			model.addAttribute("totalPages", pageSanPham.getTotalPages());
			model.addAttribute("size", size);
			model.addAttribute("searchAction", "/admin/product");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);

			// Trả về trang danh sách sản phẩm với kết quả tìm kiếm
			return "admin/product/index";
		}

		// Xử lý nếu không có từ khóa
		SanPham sanPham = sanPhamService.findById(id);
		if (sanPham == null) {
			return "redirect:/admin/product";
		}

		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		String formattedPrice = "0.00 VND";
		String formattedDiscountPrice = "0.00 VND";

		if (sanPham.getDonGiaBan() != null) {
			formattedPrice = decimalFormat.format(sanPham.getDonGiaBan()) + " VND";

			// Tìm khuyến mãi cao nhất hiện tại
			LocalDate today = LocalDate.now();
			Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
					.filter(km -> km.getTrangThai())
					.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
							&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
					.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				BigDecimal giaSauGiam = sanPham.getDonGiaBan()
						.subtract(sanPham.getDonGiaBan().multiply(phanTramGiam).divide(new BigDecimal(100)));
				formattedDiscountPrice = decimalFormat.format(giaSauGiam) + " VND";
			} else {
				formattedDiscountPrice = formattedPrice;
			}
		}

		PageRequest pageRequest = PageRequest.of(0, 10); // Lấy 10 đơn vị tính đầu tiên
		List<DonViTinh> listDonViTinh = donViTinhService.findAll(pageRequest).getContent();
		List<ChiTietDonNhapHang> chiTietDonNhapHangList = chiTietDonNhapHangService.findBySanPham(sanPham);
		boolean hasDetails = !chiTietDonNhapHangList.isEmpty();

		Integer maxSoLuongNhap = hasDetails
				? chiTietDonNhapHangList.stream().mapToInt(ChiTietDonNhapHang::getSoLuongNhap).sum()
				: 0;

		Integer soldQuantity = chiTietDonHangService.getSoldQuantityBySanPhamId(id);
		Integer editableMaxQuantity = maxSoLuongNhap - soldQuantity;

		model.addAttribute("sanPham", sanPham);
		model.addAttribute("listDonViTinh", listDonViTinh);
		model.addAttribute("hasDetails", hasDetails);
		model.addAttribute("maxSoLuongNhap", editableMaxQuantity);
		model.addAttribute("listThuongHieu", thuongHieuService.getAll());
		model.addAttribute("formattedPrice", formattedPrice);
		model.addAttribute("formattedDiscountPrice", formattedDiscountPrice);
		model.addAttribute("listDanhMuc", danhMucService.getAll());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/edit";
	}
	// Phương thức hiển thị trang chi tiết sản phẩm

	@PostMapping("/update-product")
	public String updateProduct(@ModelAttribute("sanPham") SanPham sanPham,
			@RequestParam("imageFile") MultipartFile imageFile, @RequestParam("donGiaBan") BigDecimal donGiaBan,
			@RequestParam(value = "thuongHieuId", required = false) Integer thuongHieuId,
			RedirectAttributes redirectAttributes) {

		// Lấy sản phẩm hiện tại từ cơ sở dữ liệu
		SanPham existingSanPham = sanPhamService.findById(sanPham.getMaSanPham());

		if (existingSanPham == null) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
			return "redirect:/admin/products";
		}
		// Đảm bảo soLuong không bị null
		if (sanPham.getSoLuong() == null) {
			sanPham.setSoLuong(0); // Đặt giá trị mặc định nếu là null
		}
		// Kiểm tra nếu sản phẩm có trong đơn nhập hàng
		List<ChiTietDonNhapHang> chiTietDonNhapHangList = chiTietDonNhapHangService.findBySanPham(existingSanPham);
		boolean hasDetails = !chiTietDonNhapHangList.isEmpty();

		if (hasDetails) {
			// Tính tổng số lượng nhập từ tất cả các đơn nhập hàng
			Integer totalSoLuongNhap = chiTietDonNhapHangList.stream().mapToInt(ChiTietDonNhapHang::getSoLuongNhap)
					.sum();

			// Lấy số lượng đã bán
			Integer soldQuantity = chiTietDonHangService.getSoldQuantityBySanPhamId(sanPham.getMaSanPham());

			// Tính số lượng tối đa có thể chỉnh sửa
			Integer editableMaxQuantity = totalSoLuongNhap - soldQuantity;

			// Kiểm tra số lượng tồn
			if (sanPham.getSoLuong() < 0 || sanPham.getSoLuong() > editableMaxQuantity) {
				redirectAttributes.addFlashAttribute("error", "Vui lòng nhập số lượng từ 0 đến " + editableMaxQuantity
						+ ". Số lượng tồn khả dụng được xác định dựa trên số lượng nhập và số lượng đã bán.");
				return "redirect:/admin/edit-product/" + sanPham.getMaSanPham();
			}
		}

		// Xử lý ảnh nếu có
		if (!imageFile.isEmpty()) {
			try {
				String imageName = storageService.storeFile(imageFile);
				existingSanPham.setHinhAnh(imageName); // Cập nhật ảnh trong sản phẩm hiện tại
			} catch (IOException e) {
				redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải ảnh.");
				return "redirect:/admin/edit-product/" + sanPham.getMaSanPham();
			}
		} else {
			// Nếu không có ảnh mới được tải lên, giữ lại ảnh cũ
			sanPham.setHinhAnh(existingSanPham.getHinhAnh());
		}
		// Cập nhật thông tin thương hiệu
		if (thuongHieuId != null) {
			ThuongHieu thuongHieu = thuongHieuService.findById(thuongHieuId);
			if (thuongHieu != null) {
				existingSanPham.setThuongHieu(thuongHieu);
			}
		}
		System.out.println("ID thương hiệu nhận được: " + thuongHieuId);

		// Cập nhật thông tin sản phẩm từ form
		existingSanPham.setTenSanPham(sanPham.getTenSanPham());
		existingSanPham.setMoTa(sanPham.getMoTa());
		existingSanPham.setSoLuong(sanPham.getSoLuong());
		existingSanPham.setDanhMuc(sanPham.getDanhMuc());
		existingSanPham.setDonViTinh(sanPham.getDonViTinh());
		existingSanPham.setDonGiaBan(donGiaBan);

		// Cập nhật chi tiết đơn nhập hàng nếu có thay đổi về tên sản phẩm
		chiTietDonNhapHangService.updateChiTietDonNhapHangForProduct(existingSanPham);

		// Lưu sản phẩm sau khi cập nhật các thông tin liên quan
		sanPhamService.update(existingSanPham);

		redirectAttributes.addFlashAttribute("success",
				"Sản phẩm và các thông tin liên quan đã được cập nhật thành công.");
		return "redirect:/admin/edit-product/" + sanPham.getMaSanPham();
	}

	// Controller cho xóa sản phẩm (chỉ cập nhật trạng thái thành false)
	@PostMapping("/delete-product/{id}")
	public String deleteProduct(@PathVariable("id") Integer id, Model model, @RequestParam(defaultValue = "0") int page,
			RedirectAttributes redirectAttributes) {

		SanPham sanPham = sanPhamService.findById(id);

		if (sanPham == null) {
			// Nếu sản phẩm không tồn tại
			redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
			return "redirect:/admin/product";
		}

		if (sanPham.isTrangThai() == false) {
			// Nếu sản phẩm đã bị xóa trước đó
			redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm đã bị ẩn trước đó.");
			return "redirect:/admin/product";
		}

		// Xóa sản phẩm (chuyển trạng thái thành false)
		Boolean deleted = sanPhamService.delete(id);

		if (deleted) {
			redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa thành công.");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm.");
		} // Chỉ hiển thị sản phẩm còn hoạt động (trangThai = true)

		Pageable pageable = PageRequest.of(page, 10); // 10 sản phẩm mỗi trang
		Page<SanPham> activeProducts = sanPhamService.findAllActive(pageable);

		// Thêm danh sách sản phẩm vào model để hiển thị trên trang
		model.addAttribute("sanPhams", activeProducts.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", activeProducts.getTotalPages());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "redirect:/admin/product"; // Trả về trang danh sách sản phẩm sau khi xóa
	}

	// Hiển thị chi tiết sản phẩm
	@GetMapping("/view-product/{id}")
	public String viewProductDetail(@PathVariable("id") Integer productId, Model model,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

		// Nếu có từ khóa tìm kiếm, thực hiện tìm kiếm sản phẩm
		if (keyword != null && !keyword.isEmpty()) {
			Page<SanPham> pageSanPham = sanPhamService.searchActiveByName(keyword, PageRequest.of(page, size));
			List<SanPham> sanPhams = pageSanPham.getContent();
			LocalDate today = LocalDate.now();

			// Sử dụng Map với maSanPham làm key
			Map<Integer, KhuyenMai> sanPhamKhuyenMaiMap = new HashMap<>();
			Map<SanPham, BigDecimal> sanPhamGiaSauGiamMap = new HashMap<>();

			for (SanPham sanPham : sanPhams) {
				// Tìm khuyến mãi cao nhất hiện tại có trạng thái true
				Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
						.filter(km -> km.getTrangThai()) // Chỉ lấy khuyến mãi có trạng thái true
						.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
								&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				// Thêm vào map với maSanPham làm key
				sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.orElse(null));

				// Tính giá sau khi giảm
				if (highestCurrentKhuyenMai.isPresent()) {
					BigDecimal giaGoc = sanPham.getDonGiaBan();
					BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
					BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(new BigDecimal(100)));
					sanPhamGiaSauGiamMap.put(sanPham, giaSauGiam);
				} else {
					sanPhamGiaSauGiamMap.put(sanPham, sanPham.getDonGiaBan());
				}
			}

			model.addAttribute("highestKhuyenMais", sanPhamKhuyenMaiMap);
			model.addAttribute("giaSauGiams", sanPhamGiaSauGiamMap);
			model.addAttribute("listSanPham", sanPhams);
			model.addAttribute("currentPage", pageSanPham.getNumber());
			model.addAttribute("totalPages", pageSanPham.getTotalPages());
			model.addAttribute("size", size);
			model.addAttribute("searchAction", "/admin/product");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);

			// Trả về trang tìm kiếm kết quả (index)
			return "admin/product/index";
		}

		// Nếu không có từ khóa, hiển thị chi tiết sản phẩm như bình thường
		SanPham sanPham = sanPhamService.findById(productId);

		if (sanPham == null) {
			return "redirect:/admin/product";
		}

		LocalDate today = LocalDate.now();
		Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
				.filter(km -> km.getTrangThai() == true)
				.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
						&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
				.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));
		// Định dạng giá gốc và giá sau khi giảm
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		// Giá gốc
		String formattedPrice = decimalFormat.format(sanPham.getDonGiaBan()) + " VND";
		// Giá sau khi giảm
		String formattedDiscountPrice = formattedPrice;

		if (highestCurrentKhuyenMai.isPresent()) {
			BigDecimal giaGoc = sanPham.getDonGiaBan();
			BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
			BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(new BigDecimal(100)));
			formattedDiscountPrice = decimalFormat.format(giaSauGiam) + " VND";
		}

		model.addAttribute("sanPham", sanPham);
		model.addAttribute("highestKhuyenMai", highestCurrentKhuyenMai.orElse(null));
		model.addAttribute("formattedPrice", formattedPrice);
		model.addAttribute("formattedDiscountPrice", formattedDiscountPrice);

		List<ChiTietDonNhapHang> chiTietDonNhapHangList = chiTietDonNhapHangService.findBySanPham(sanPham).stream()
				.filter(ctdnh -> ctdnh.getSoLuongNhap() > 0 && ctdnh.getDonGiaNhap().compareTo(BigDecimal.ZERO) > 0)
				.collect(Collectors.toList());

		  // Tạo Map để lưu giá trị định dạng của Đơn giá nhập
	    Map<ChiTietDonNhapHangId, String> formattedDonGiaNhapMap = new HashMap<>();

	    for (ChiTietDonNhapHang chiTiet : chiTietDonNhapHangList) {
	        String formattedValue = decimalFormat.format(chiTiet.getDonGiaNhap()) + " VND";
	        formattedDonGiaNhapMap.put(chiTiet.getId(), formattedValue);
	    }

		
		
		boolean hasDetails = !chiTietDonNhapHangList.isEmpty();
		
		DanhMuc danhMuc = sanPham.getDanhMuc();
		Set<NhaCungCap> nhaCungCaps = sanPham.getNhaCungCaps();
		Integer soldQuantity = chiTietDonHangService.getSoldQuantityBySanPhamId(productId);
		// Thêm Thương Hiệu
		ThuongHieu thuongHieu = sanPham.getThuongHieu();

		model.addAttribute("sanPham", sanPham);
		model.addAttribute("hasDetails", hasDetails);
		model.addAttribute("danhMuc", danhMuc);
		model.addAttribute("nhaCungCaps", nhaCungCaps);
		model.addAttribute("chiTietDonNhapHangList", chiTietDonNhapHangList);
	    model.addAttribute("formattedDonGiaNhapMap", formattedDonGiaNhapMap);

		model.addAttribute("soldQuantity", soldQuantity);
		model.addAttribute("thuongHieu", thuongHieu);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/view";
	}

}
