package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.SanPham;
//import com.kimngan.ComesticAdmin.entity.ThoiDiem;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.DanhMuc;
//import com.kimngan.ComesticAdmin.entity.DonGiaBanHang;
//import com.kimngan.ComesticAdmin.entity.DonGiaBanHangId;
import com.kimngan.ComesticAdmin.entity.DonViTinh;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.NhaCungCap;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DanhMucService;
//import com.kimngan.ComesticAdmin.services.DonGiaBanHangService;
import com.kimngan.ComesticAdmin.services.DonViTinhService;
import com.kimngan.ComesticAdmin.services.NhaCungCapService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.StorageService;
//import com.kimngan.ComesticAdmin.services.ThoiDiemService;

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

//import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
//import java.util.List;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

	// Hiển thị danh sách sản phẩm
	@GetMapping("/product")
	public String index(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<SanPham> pageSanPham;

		if (keyword != null && !keyword.isEmpty()) {
			// Nếu có từ khóa tìm kiếm
			pageSanPham = sanPhamService.searchActiveByName(keyword, PageRequest.of(page, size));
			model.addAttribute("keyword", keyword);
		} else {
			// Nếu không có từ khóa, lấy tất cả sản phẩm
			pageSanPham = sanPhamService.findAllActive(PageRequest.of(page, size));
		}
		// Kiểm tra nếu trang yêu cầu vượt quá tổng số trang, điều hướng về trang cuối
		// cùng
		if (page > pageSanPham.getTotalPages()) {
			pageSanPham = sanPhamService.findAll(PageRequest.of(pageSanPham.getTotalPages() - 1, size));
		}

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
			// Debug in ra nếu tìm thấy khuyến mãi
			// highestCurrentKhuyenMai.ifPresent(km -> System.out.println("Khuyến mãi ID: "
			// + km.getMaKhuyenMai()));

			// Thêm vào map với maSanPham làm key
			sanPhamKhuyenMaiMap.put(sanPham.getMaSanPham(), highestCurrentKhuyenMai.orElse(null));

			// Tính giá sau khi giảm
			if (highestCurrentKhuyenMai.isPresent()) {
				BigDecimal giaGoc = sanPham.getDonGiaBan();
				BigDecimal phanTramGiam = highestCurrentKhuyenMai.get().getPhanTramGiamGia();
				BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(new BigDecimal(100)));
				sanPhamGiaSauGiamMap.put(sanPham, giaSauGiam); // Lưu giá sau khi giảm
			} else {
				sanPhamGiaSauGiamMap.put(sanPham, sanPham.getDonGiaBan()); // Không có khuyến mãi, giữ nguyên giá gốc
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

// Thêm thông tin người dùng để hiển thị
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/index";
	}

	// Trang thêm sản phẩm
	@GetMapping("/add-product")
	public String addProduct(Model model) {

		// Lấy danh sách các nhà cung cấp đang hoạt động
		List<NhaCungCap> activeSuppliers = nhaCungCapService.findByTrangThaiTrue();
		model.addAttribute("activeSuppliers", activeSuppliers);

		model.addAttribute("sanPham", new SanPham());
		model.addAttribute("listDonViTinh", donViTinhService.getAll());
		// Tạo đối tượng trống cho form
		model.addAttribute("listDanhMuc", danhMucService.getAll()); // Lấy danh sách các danh mục

		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/add"; // Trả về trang thêm sản phẩm
	}

	@PostMapping("/add-product")
	public String saveProduct(@ModelAttribute("sanPham") SanPham sanPham,
			@RequestParam("donViTinh") Integer donViTinhId,

			@RequestParam("nhaCungCapId") Integer nhaCungCapId, @RequestParam("imageFile") MultipartFile imageFile,
			Model model) {
		sanPham.setSoLuong(0);
		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Tìm nhà cung cấp theo ID
		NhaCungCap nhaCungCap = nhaCungCapService.findById(nhaCungCapId);

		// Liên kết nhà cung cấp với sản phẩm
		sanPham.setNhaCungCaps(Collections.singleton(nhaCungCap));

		// Kiểm tra nếu sản phẩm với tên đã tồn tại và đang hoạt động
		if (sanPhamService.existsByTenSanPham(sanPham.getTenSanPham())) {
			model.addAttribute("errorMessage", "Sản phẩm đã tồn tại!");
			return "admin/product/add"; // Quay lại trang thêm sản phẩm nếu đã tồn tại
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

	// Trang chỉnh sửa sản phẩm
	@GetMapping("/edit-product/{id}")
	public String editProductPage(@PathVariable("id") Integer id, Model model) {
		SanPham sanPham = sanPhamService.findById(id);
		if (sanPham == null) {
			return "redirect:/admin/products"; // Nếu không tìm thấy sản phẩm
		}

		PageRequest pageRequest = PageRequest.of(0, 10); // Lấy 10 đơn vị tính đầu tiên
		List<DonViTinh> listDonViTinh = donViTinhService.findAll(pageRequest).getContent();
		// Kiểm tra nếu sản phẩm có chi tiết đơn nhập hàng hay không
		List<ChiTietDonNhapHang> chiTietDonNhapHangList = chiTietDonNhapHangService.findBySanPham(sanPham);
		boolean hasDetails = !chiTietDonNhapHangList.isEmpty();
		
		
		 // Tìm giá trị lớn nhất của soLuongNhap nếu có chi tiết đơn nhập hàng
		// Tính tổng số lượng nhập từ tất cả các đơn nhập hàng nếu có chi tiết đơn nhập hàng
		Integer maxSoLuongNhap = hasDetails ? chiTietDonNhapHangList.stream()
		        .mapToInt(ChiTietDonNhapHang::getSoLuongNhap)
		        .sum() : 0;

		
		 // Tính tổng số lượng đã bán
	    Integer soldQuantity = chiTietDonHangService.getSoldQuantityBySanPhamId(id);

	    // Tính số lượng tối đa có thể chỉnh sửa
	    Integer editableMaxQuantity = maxSoLuongNhap - soldQuantity;
		
		
		
		// Thêm sản phẩm vào model
		model.addAttribute("sanPham", sanPham);
		model.addAttribute("listDonViTinh", listDonViTinh);
		model.addAttribute("hasDetails", hasDetails);
	    model.addAttribute("maxSoLuongNhap", editableMaxQuantity);

		// Lấy danh sách danh mục để hiển thị trong form
		List<DanhMuc> listDanhMuc = danhMucService.getAll();
		model.addAttribute("listDanhMuc", listDanhMuc);
		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/edit"; // Chuyển hướng tới trang chỉnh sửa
	}

	// Phương thức hiển thị trang chi tiết sản phẩm

	@PostMapping("/update-product")
	public String updateProduct(@ModelAttribute("sanPham") SanPham sanPham,
			@RequestParam("imageFile") MultipartFile imageFile, @RequestParam("donGiaBan") BigDecimal donGiaBan,
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
	        Integer totalSoLuongNhap = chiTietDonNhapHangList.stream()
	                .mapToInt(ChiTietDonNhapHang::getSoLuongNhap)
	                .sum();

	        // Lấy số lượng đã bán
	        Integer soldQuantity = chiTietDonHangService.getSoldQuantityBySanPhamId(sanPham.getMaSanPham());

	        // Tính số lượng tối đa có thể chỉnh sửa
	        Integer editableMaxQuantity = totalSoLuongNhap - soldQuantity;

	        // Kiểm tra số lượng tồn
	        if (sanPham.getSoLuong() < 0 || sanPham.getSoLuong() > editableMaxQuantity) {
	        	redirectAttributes.addFlashAttribute("error", "Vui lòng nhập số lượng từ 0 đến " + editableMaxQuantity + ". Số lượng tồn khả dụng được xác định dựa trên số lượng nhập và số lượng đã bán.");
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
		return "redirect:/admin/view-product/" + sanPham.getMaSanPham();
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

	@GetMapping("/view-product/{id}")
	public String viewProduct(@PathVariable("id") Integer id, Model model) {
		// Tìm sản phẩm theo ID
		SanPham sanPham = sanPhamService.findById(id);

		// Kiểm tra nếu sản phẩm không tồn tại
		if (sanPham == null) {
			return "redirect:/admin/product"; // Nếu không tìm thấy, quay lại trang danh sách
		}
		// Lấy ngày hiện tại
		LocalDate today = LocalDate.now();
		// Tìm khuyến mãi cao nhất hiện tại có trạng thái true
		Optional<KhuyenMai> highestCurrentKhuyenMai = sanPham.getKhuyenMais().stream()
				.filter(km -> km.getTrangThai() == true) // Chỉ lấy khuyến mãi có trạng thái true
				.filter(km -> !km.getNgayBatDau().toLocalDate().isAfter(today)
						&& !km.getNgayKetThuc().toLocalDate().isBefore(today))
				.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

		
		
		
		
		
		
		
		
		
		// Thêm sản phẩm và khuyến mãi cao nhất vào model
		model.addAttribute("sanPham", sanPham);
		model.addAttribute("highestKhuyenMai", highestCurrentKhuyenMai.orElse(null));

		// Lấy danh sách các chi tiết đơn nhập hàng liên quan đến sản phẩm
		List<ChiTietDonNhapHang> chiTietDonNhapHangList = chiTietDonNhapHangService.findBySanPham(sanPham).stream()
				.filter(ctdnh -> ctdnh.getSoLuongNhap() > 0 && ctdnh.getDonGiaNhap().compareTo(BigDecimal.ZERO) > 0)
				.collect(Collectors.toList())

		;
		// Kiểm tra nếu sản phẩm có trong đơn nhập hàng hay không

		boolean hasDetails = !chiTietDonNhapHangList.isEmpty();
		// Lấy thông tin danh mục và nhà cung cấp liên quan đến sản phẩm
		DanhMuc danhMuc = sanPham.getDanhMuc();
		Set<NhaCungCap> nhaCungCaps = sanPham.getNhaCungCaps();
		// Tính tổng số lượng đã bán
	    Integer soldQuantity = chiTietDonHangService.getSoldQuantityBySanPhamId(id);
		// Lấy giá bán mới nhất
		
		model.addAttribute("sanPham", sanPham);
		model.addAttribute("hasDetails", hasDetails);
		model.addAttribute("danhMuc", danhMuc);
		
		// model.addAttribute("nhaCungCap", nhaCungCaps);
		model.addAttribute("chiTietDonNhapHangList", chiTietDonNhapHangList);
		model.addAttribute("nhaCungCaps", nhaCungCaps);
		model.addAttribute("soldQuantity", soldQuantity);
		System.out.println(nhaCungCaps);


		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/product/view"; // Trả về trang chi tiết sản phẩm
	}

}
