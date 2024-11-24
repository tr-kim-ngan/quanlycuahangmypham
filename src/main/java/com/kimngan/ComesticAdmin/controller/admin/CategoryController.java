package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/admin")
public class CategoryController {

	@Autowired
	private DanhMucService danhMucService;
// hiện danh sách câc danh mục
	@GetMapping("/category")
	public String index(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<DanhMuc> pageDanhMuc;

		if (keyword != null && !keyword.isEmpty()) {
			pageDanhMuc = danhMucService.searchByName(keyword, PageRequest.of(page, size));
			model.addAttribute("keyword", keyword);
		} else {
			pageDanhMuc = danhMucService.findAll(PageRequest.of(page, size));
		}

		// Kiểm tra nếu trang yêu cầu vượt quá tổng số trang, điều hướng về trang cuối
		// cùng
		if (page > pageDanhMuc.getTotalPages()) {
			pageDanhMuc = danhMucService.findAll(PageRequest.of(pageDanhMuc.getTotalPages() - 1, size));
		}

		model.addAttribute("listDanhMuc", pageDanhMuc.getContent());
		model.addAttribute("currentPage", pageDanhMuc.getNumber());
		model.addAttribute("totalPages", pageDanhMuc.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/admin/category");

		// Thêm thông tin người dùng để hiển thị
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/category/index";
	}
// thêm danh mục
	@GetMapping("/add-category")
	public String addCategoryPage(Model model) {
		model.addAttribute("danhMuc", new DanhMuc());

		// Thêm đoạn code lấy thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/category/add"; // Trả về view form thêm mới danh mục
	}
// lưu danh mục
	@PostMapping("/add-category")
	public String save(@ModelAttribute("danhMuc") DanhMuc danhMuc, Model model) {
		// Kiểm tra xem tên danh mục đã tồn tại chưa

		if (danhMucService.existsByTenDanhMuc(danhMuc.getTenDanhMuc())) {
			model.addAttribute("error", "Tên danh mục đã tồn tại!");
			// Đảm bảo thông tin người dùng vẫn được hiển thị
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/category/add"; // Quay lại trang thêm nếu tên danh mục đã tồn tại
		}

		// Nếu không trùng, tiến hành thêm mới danh mục
		if (this.danhMucService.create(danhMuc)) {
			return "redirect:/admin/category";
		} else {
			model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
			return "admin/category/add";
		}

	}
// sửa danh mục
	@GetMapping("/edit-category/{id}")
	public String edit(@PathVariable("id") Integer id, Model model) {
		// Tìm danh mục theo ID
		DanhMuc danhMuc = danhMucService.findById(id);

		// Kiểm tra nếu danh mục không tồn tại
		if (danhMuc == null) {
			return "redirect:/admin/category"; // Nếu không tìm thấy, quay lại trang danh sách
		}

		// Đưa danh mục vào Model để hiển thị
		model.addAttribute("danhMuc", danhMuc);

		// Thêm thông tin người dùng (nếu cần)
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/category/edit"; // Trả về view form chỉnh sửa danh mục
	}
// lưu danh mục
	@PostMapping("/edit-category")
	public String update(@ModelAttribute("danhMuc") DanhMuc danhMuc, Model model) {
		if (danhMuc.getMaDanhMuc() != null) {
			// Kiểm tra xem tên danh mục đã tồn tại chưa
			if (danhMucService.existsByTenDanhMuc(danhMuc.getTenDanhMuc())) {
				model.addAttribute("error", "Tên danh mục đã tồn tại!");

				// Đảm bảo model nạp lại danh mục hiện tại và thông tin người dùng khi gặp lỗi
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
				model.addAttribute("user", userDetails);
				model.addAttribute("danhMuc", danhMuc); // Đưa lại danh mục vào model để hiển thị trên form

				return "admin/category/edit"; // Trả về form chỉnh sửa với lỗi
			}

			// Nếu không trùng tên, thực hiện cập nhật
			if (this.danhMucService.update(danhMuc)) {
				return "redirect:/admin/category";
			} else {
				model.addAttribute("error", "Cập nhật thất bại!");
				return "admin/category/edit";
			}
		} else {
			// Nếu không có ID, trả về lỗi hoặc xử lý khác (nếu cần)
			return "redirect:/admin/category";
		}
	}
// xóa
	@GetMapping("/delete-category/{id}")
	public String deleteCategory(@PathVariable("id") Integer id) {
		// Kiểm tra nếu danh mục tồn tại
		DanhMuc danhMuc = danhMucService.findById(id);
		if (danhMuc == null) {
			return "redirect:/admin/category"; // Nếu không tìm thấy, quay về trang danh sách
		}

		// Xóa danh mục
		danhMucService.delete(id);

		// Quay về trang danh sách sau khi xóa thành công
		return "redirect:/admin/category";
	}

}
