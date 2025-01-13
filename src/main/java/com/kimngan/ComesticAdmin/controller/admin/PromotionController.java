package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.KhuyenMaiService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/admin")
public class PromotionController {

	@Autowired
	private KhuyenMaiService khuyenMaiService;

	@Autowired
	private SanPhamService sanPhamService;

	// Hiển thị danh sách khuyến mãi với phân trang
	@GetMapping("/promotion")
	public String index(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<KhuyenMai> pageKhuyenMai;
		// Tạo đối tượng PageRequest với sắp xếp giảm dần theo ngayBatDau
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maKhuyenMai"));

// hiện tên khuyến mãi có trạng thá =1
		if (keyword != null && !keyword.isEmpty()) {
			// Tìm kiếm theo tên khuyến mãi với sắp xếp
			pageKhuyenMai = khuyenMaiService.searchByName(keyword, pageRequest);
			model.addAttribute("keyword", keyword);
		} else {
			// Lấy danh sách khuyến mãi với sắp xếp
			pageKhuyenMai = khuyenMaiService.findAllActive(pageRequest);
		}

		model.addAttribute("listKhuyenMai", pageKhuyenMai.getContent());
		model.addAttribute("currentPage", pageKhuyenMai.getNumber());
		model.addAttribute("totalPages", pageKhuyenMai.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/admin/promotion");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/promotion/index";
	}
	@GetMapping("/view-promotion/{id}")
	public String viewPromotion(@PathVariable("id") Integer id, Model model) {
	    KhuyenMai khuyenMai = khuyenMaiService.findById(id);
	    if (khuyenMai == null) {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
	        model.addAttribute("user", userDetails);
	        model.addAttribute("errorMessage", "Không tìm thấy khuyến mãi.");
	        return "redirect:/admin/promotion";
	    }

	    List<SanPham> sanPhams = new ArrayList<>(khuyenMai.getSanPhams());
	    Map<Integer, String> formattedDonGiaBanMap = new HashMap<>();
	    Map<Integer, String> formattedGiaSauGiamMap = new HashMap<>();
	    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

	    // Định dạng giá bán và giá sau khi giảm
	    for (SanPham sanPham : sanPhams) {
	        BigDecimal giaGoc = sanPham.getDonGiaBan();
	        formattedDonGiaBanMap.put(sanPham.getMaSanPham(), decimalFormat.format(giaGoc) + " VNĐ");

	        BigDecimal giaSauGiam = giaGoc.subtract(giaGoc.multiply(khuyenMai.getPhanTramGiamGia()).divide(new BigDecimal(100)));
	        formattedGiaSauGiamMap.put(sanPham.getMaSanPham(), decimalFormat.format(giaSauGiam) + " VNĐ");
	    }

	    model.addAttribute("khuyenMai", khuyenMai);
	    model.addAttribute("sanPhams", sanPhams);
	    model.addAttribute("formattedDonGiaBanMap", formattedDonGiaBanMap);
	    model.addAttribute("formattedGiaSauGiamMap", formattedGiaSauGiamMap);

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
	    model.addAttribute("user", userDetails);

	    return "admin/promotion/view";
	}


	

	// Trang thêm khuyến mãi
	@GetMapping("/add-promotion")
	public String addPromotion(Model model) {
		model.addAttribute("khuyenMai", new KhuyenMai());

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/promotion/add";
	}

	// Lưu khuyến mãi
	@PostMapping("/add-promotion")
	public String savePromotion(@ModelAttribute("khuyenMai") KhuyenMai khuyenMai, Model model) {
		// Chuyển đổi Date sang LocalDate
		LocalDate ngayBatDau = khuyenMai.getNgayBatDau().toLocalDate();
		LocalDate ngayKetThuc = khuyenMai.getNgayKetThuc().toLocalDate();
		LocalDate today = LocalDate.now();

		// Kiểm tra ngày bắt đầu
		if (ngayBatDau.isBefore(today)) {
			model.addAttribute("errorMessage", "Ngày bắt đầu không thể trong quá khứ.");
			model.addAttribute("khuyenMai", khuyenMai);
			// Thêm thông tin người dùng
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails); // Thêm user vào model
			return "admin/promotion/add";
		}

		// Kiểm tra ngày kết thúc
		if (ngayKetThuc.isBefore(ngayBatDau)) {
			model.addAttribute("errorMessage", "Ngày kết thúc phải lớn hơn ngày bắt đầu.");
			model.addAttribute("khuyenMai", khuyenMai);
			// Thêm thông tin người dùng
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/promotion/add";
		}

		// Kiểm tra thời gian áp dụng khuyến mãi
		long daysBetween = ChronoUnit.DAYS.between(ngayBatDau, ngayKetThuc);
		if (daysBetween > 365) {
			model.addAttribute("errorMessage", "Thời gian áp dụng khuyến mãi không được quá 1 năm.");
			model.addAttribute("khuyenMai", khuyenMai);
			// Thêm thông tin người dùng
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/promotion/add";
		}

		// Thiết lập trạng thái mặc định là true
		khuyenMai.setTrangThai(true);
		khuyenMaiService.create(khuyenMai);

		// Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "redirect:/admin/promotion";
	}

	// Trang chỉnh sửa khuyến mãi
	@GetMapping("/edit-promotion/{id}")
	public String editPromotion(@PathVariable("id") Integer id, Model model) {
		KhuyenMai khuyenMai = khuyenMaiService.findById(id);
		if (khuyenMai == null) {
			return "redirect:/admin/promotion"; // Nếu không tìm thấy khuyến mãi
		}

		// Lấy danh sách sản phẩm có trạng thái true để hiển thị trong form
		List<SanPham> listSanPham = sanPhamService.findByTrangThai(true); // Chỉ lấy sản phẩm có trạng thái hoạt động

		// Đưa khuyến mãi và danh sách sản phẩm vào model
		model.addAttribute("khuyenMai", khuyenMai);
		model.addAttribute("listSanPham", listSanPham);
		// Thêm thuộc tính để đánh dấu sản phẩm nào đã được áp dụng khuyến mãi
		Set<Integer> sanPhamIds = khuyenMai.getSanPhams().stream().map(SanPham::getMaSanPham)
				.collect(Collectors.toSet());
		model.addAttribute("sanPhamIds", sanPhamIds); // Đưa danh sách ID sản phẩm đã được chọn vào model

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/promotion/edit";
	}

	// lưu khi đã sửa
	@PostMapping("/edit-promotion/{id}")
	public String updatePromotion(@PathVariable("id") Integer id, @ModelAttribute("khuyenMai") KhuyenMai khuyenMai,
			@RequestParam(value = "selectedProducts", required = false) List<Integer> selectedProducts, Model model) {

		// Kiểm tra nếu khuyến mãi tồn tại
		KhuyenMai existingKhuyenMai = khuyenMaiService.findById(id);
		if (existingKhuyenMai == null) {
			model.addAttribute("errorMessage", "Không tìm thấy khuyến mãi.");
			return "redirect:/admin/promotion"; // Quay lại danh sách nếu không tìm thấy
		}

		// Chuyển đổi Date sang LocalDate để kiểm tra logic
		LocalDate ngayBatDau = khuyenMai.getNgayBatDau().toLocalDate();
		LocalDate ngayKetThuc = khuyenMai.getNgayKetThuc().toLocalDate();
		LocalDate today = LocalDate.now();

		// Kiểm tra ngày bắt đầu
		if (ngayBatDau.isBefore(today)) {
			model.addAttribute("errorMessage", "Ngày bắt đầu không thể trong quá khứ.");
			model.addAttribute("khuyenMai", existingKhuyenMai);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/promotion/edit"; // Trả lại trang chỉnh sửa
		}

		// Kiểm tra ngày kết thúc
		if (ngayKetThuc.isBefore(ngayBatDau)) {
			model.addAttribute("errorMessage", "Ngày kết thúc phải lớn hơn ngày bắt đầu.");
			model.addAttribute("khuyenMai", existingKhuyenMai);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/promotion/edit";
		}

		// Kiểm tra thời gian áp dụng khuyến mãi
		long daysBetween = ChronoUnit.DAYS.between(ngayBatDau, ngayKetThuc);
		if (daysBetween > 365) {
			model.addAttribute("errorMessage", "Thời gian áp dụng khuyến mãi không được quá 1 năm.");
			model.addAttribute("khuyenMai", existingKhuyenMai);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/promotion/edit";
		}

		// Cập nhật thông tin khuyến mãi
		if (khuyenMai.getTenKhuyenMai() != null && !khuyenMai.getTenKhuyenMai().isEmpty()) {
			existingKhuyenMai.setTenKhuyenMai(khuyenMai.getTenKhuyenMai());
		}
		if (khuyenMai.getMoTa() != null && !khuyenMai.getMoTa().isEmpty()) {
			existingKhuyenMai.setMoTa(khuyenMai.getMoTa());
		}
		if (khuyenMai.getPhanTramGiamGia() != null) {
			existingKhuyenMai.setPhanTramGiamGia(khuyenMai.getPhanTramGiamGia());
		}
		if (khuyenMai.getNgayBatDau() != null) {
			existingKhuyenMai.setNgayBatDau(khuyenMai.getNgayBatDau());
		}
		if (khuyenMai.getNgayKetThuc() != null) {
			existingKhuyenMai.setNgayKetThuc(khuyenMai.getNgayKetThuc());
		}
		if (khuyenMai.getTrangThai() != null) {
			existingKhuyenMai.setTrangThai(khuyenMai.getTrangThai());
		}

		// Lưu các thay đổi vào cơ sở dữ liệu
		khuyenMaiService.update(existingKhuyenMai);

		// Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		model.addAttribute("successMessage", "Cập nhật khuyến mãi thành công.");
		return "redirect:/admin/edit-promotion/" + id;// Điều hướng về danh sách khuyến mãi sau khi cập nhật thành công
	}

	@PostMapping("/edit-promotion/{id}/add-product")
	public String addProductToPromotion(@PathVariable("id") Integer id,
			@RequestParam("selectedProducts") List<Integer> selectedProducts, // Chọn nhiều sản phẩm
			Model model) {

		// Tìm khuyến mãi hiện tại
		KhuyenMai existingKhuyenMai = khuyenMaiService.findById(id);
		if (existingKhuyenMai == null) {
			return "redirect:/admin/promotion";
		}

		// Xử lý danh sách sản phẩm tùy chọn nếu có
		if (selectedProducts != null && !selectedProducts.isEmpty()) {
			existingKhuyenMai.getSanPhams().clear(); // Xóa các sản phẩm cũ
			for (Integer productId : selectedProducts) {
				SanPham selectedSanPham = sanPhamService.findById(productId);
				if (selectedSanPham != null) {
					existingKhuyenMai.getSanPhams().add(selectedSanPham); // Thêm sản phẩm vào khuyến mãi
					selectedSanPham.getKhuyenMais().add(existingKhuyenMai); // Đồng bộ cả hai chiều
				}
			}
		}

		// Cập nhật khuyến mãi
		khuyenMaiService.update(existingKhuyenMai);

		// Trả về trang chỉnh sửa sau khi thêm sản phẩm
		model.addAttribute("khuyenMai", existingKhuyenMai);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "redirect:/admin/edit-promotion/" + id;
	}

	@PostMapping("/remove-promotion-product/{id}/{maSanPham}")
	public String removeProductFromPromotion(@PathVariable("id") Integer id,
			@PathVariable("maSanPham") Integer maSanPham, Model model) {

		// Tìm khuyến mãi hiện tại
		KhuyenMai khuyenMai = khuyenMaiService.findById(id);
		if (khuyenMai == null) {
			return "redirect:/admin/promotion"; // Nếu không tìm thấy khuyến mãi
		}

		// Tìm sản phẩm
		SanPham sanPham = sanPhamService.findById(maSanPham);
		if (sanPham == null) {
			return "redirect:/admin/edit-promotion/" + id; // Quay lại nếu không tìm thấy sản phẩm
		}

		// Xóa sản phẩm khỏi khuyến mãi
		khuyenMai.getSanPhams().remove(sanPham);
		sanPham.getKhuyenMais().remove(khuyenMai); // Đồng bộ xóa ở cả hai chiều (Many-to-Many)

		// Cập nhật khuyến mãi trong cơ sở dữ liệu
		khuyenMaiService.update(khuyenMai);

		// Đưa lại thông tin khuyến mãi và người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
		model.addAttribute("khuyenMai", khuyenMai);

		return "redirect:/admin/edit-promotion/" + id; // Quay lại trang chỉnh sửa sau khi xóa sản phẩm
	}

	// Xóa khuyến mãi (chuyển trạng thái thành false)
	// Xóa khuyến mãi (chuyển trạng thái thành false)
	@PostMapping("/delete-promotion/{id}")
	public String deletePromotion(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

		KhuyenMai khuyenMai = khuyenMaiService.findById(id);

		if (khuyenMai != null) {
			// Loại bỏ tất cả các sản phẩm liên quan đến khuyến mãi
			khuyenMai.getSanPhams().clear();
			khuyenMaiService.update(khuyenMai); // Cập nhật khuyến mãi

			// Cập nhật khuyến mãi với danh sách sản phẩm rỗng
			khuyenMai.setTrangThai(false);
			khuyenMaiService.update(khuyenMai);

			// Hiển thị thông báo thành công
			redirectAttributes.addFlashAttribute("successMessage",
					"Khuyến mãi đã được chuyển trạng thái thành không hoạt động và các sản phẩm không còn được khuyến mãi.");
		} else {
			// Hiển thị thông báo lỗi nếu không tìm thấy khuyến mãi
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khuyến mãi để xóa.");
		}

		return "redirect:/admin/promotion"; // Quay lại danh sách khuyến mãi sau khi thực hiện
	}

}
