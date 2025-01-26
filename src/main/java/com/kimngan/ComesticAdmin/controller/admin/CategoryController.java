package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.DanhMucService;
import com.kimngan.ComesticAdmin.services.StorageService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin")
public class CategoryController {

	@Autowired
	private DanhMucService danhMucService;
	@Autowired
	private StorageService storageService;

// hiện danh sách câc danh mục
	@GetMapping("/category")
	public String index(Model model, 
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<DanhMuc> pageDanhMuc;

		if (keyword != null && !keyword.isEmpty()) {
			//pageDanhMuc = danhMucService.searchByName(keyword, PageRequest.of(page, size));
	        pageDanhMuc = danhMucService.searchByName(keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDanhMuc")));

			model.addAttribute("keyword", keyword);
		} else {
			//pageDanhMuc = danhMucService.findAll(PageRequest.of(page, size));
			 // Lấy tất cả danh mục, sắp xếp theo maDanhMuc giảm dần
	        pageDanhMuc = danhMucService.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDanhMuc")));
	    
		}

		// Kiểm tra nếu trang yêu cầu vượt quá tổng số trang, điều hướng về trang cuối
		// cùng
		if (page > pageDanhMuc.getTotalPages()) {
	        pageDanhMuc = danhMucService.findAll(PageRequest.of(pageDanhMuc.getTotalPages() - 1, size, Sort.by(Sort.Direction.DESC, "maDanhMuc")));
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
	public String save(@ModelAttribute("danhMuc") DanhMuc danhMuc, @RequestParam("imageFile") MultipartFile imageFile,
			Model model) {
		// Kiểm tra xem tên danh mục đã tồn tại chưa

		if (danhMucService.existsByTenDanhMuc(danhMuc.getTenDanhMuc())) {
			model.addAttribute("error", "Tên danh mục đã tồn tại!");
			// Đảm bảo thông tin người dùng vẫn được hiển thị
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/category/add"; // Quay lại trang thêm nếu tên danh mục đã tồn tại
		}

		// Xử lý lưu ảnh nếu file không rỗng
		if (!imageFile.isEmpty()) {
			try {
				// Kiểm tra kích thước file (giới hạn 2MB)
//				long maxFileSize = 2 * 1024 * 1024; // 2MB
//				if (imageFile.getSize() > maxFileSize) {
//					model.addAttribute("error", "Kích thước ảnh không được vượt quá 2MB!");
//					model.addAttribute("danhMuc", danhMuc);
//					return "admin/category/add";
//				}

				// Kiểm tra định dạng file
				String contentType = imageFile.getContentType();
				if (contentType != null && !contentType.startsWith("image/")) {
					model.addAttribute("error", "Vui lòng chọn tệp hình ảnh hợp lệ!");
					model.addAttribute("danhMuc", danhMuc);
					return "admin/category/add";
				}

				// Lưu file sử dụng StorageService
				String fileName = storageService.storeFile(imageFile);
				danhMuc.setHinhAnh(fileName); // Gán tên file cho thuộc tính hình ảnh của danh mục
			} catch (IOException e) {
				model.addAttribute("error", "Lỗi khi lưu ảnh.");
				model.addAttribute("danhMuc", danhMuc);
				return "admin/category/add";
			}
		} else {
			model.addAttribute("error", "Vui lòng chọn một hình ảnh!");
			model.addAttribute("danhMuc", danhMuc);
			return "admin/category/add";
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
	public String update(@ModelAttribute("danhMuc") DanhMuc danhMuc, @RequestParam("imageFile") MultipartFile imageFile,
			Model model) {

		// Thêm thông tin người dùng vào Model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
		}

		// Lấy danh mục hiện tại từ cơ sở dữ liệu
		DanhMuc existingDanhMuc = danhMucService.findById(danhMuc.getMaDanhMuc());
		if (existingDanhMuc == null) {
			model.addAttribute("error", "Danh mục không tồn tại!");
			return "admin/category/edit"; // Ở lại trang chỉnh sửa
		}

		// Kiểm tra nếu tên danh mục bị thay đổi và tên mới đã tồn tại
		if (!existingDanhMuc.getTenDanhMuc().equals(danhMuc.getTenDanhMuc())
				&& danhMucService.existsByTenDanhMuc(danhMuc.getTenDanhMuc())) {
			model.addAttribute("error", "Tên danh mục đã tồn tại!");
			model.addAttribute("danhMuc", danhMuc);
			return "admin/category/edit"; // Ở lại trang chỉnh sửa
		}

		// Xử lý cập nhật hình ảnh nếu có upload mới
		if (!imageFile.isEmpty()) {
			try {
//				long maxFileSize = 2 * 1024 * 1024; // 2MB
//				if (imageFile.getSize() > maxFileSize) {
//					model.addAttribute("error", "Kích thước ảnh không được vượt quá 2MB!");
//					model.addAttribute("danhMuc", danhMuc);
//					return "admin/category/edit";
//				}

				String contentType = imageFile.getContentType();
				if (contentType != null && !contentType.startsWith("image/")) {
					model.addAttribute("error", "Vui lòng chọn tệp hình ảnh hợp lệ!");
					return "admin/category/edit";
				}

				String fileName = storageService.storeFile(imageFile);
				danhMuc.setHinhAnh(fileName);
			} catch (IOException e) {
				model.addAttribute("error", "Có lỗi xảy ra khi lưu hình ảnh!");
				return "admin/category/edit";
			}
		} else {
			danhMuc.setHinhAnh(existingDanhMuc.getHinhAnh());
		}

		if (danhMucService.update(danhMuc)) {
			model.addAttribute("success", "Cập nhật thành công!");
			model.addAttribute("danhMuc", danhMuc);
		} else {
			model.addAttribute("error", "Cập nhật thất bại!");
		}

		return "admin/category/edit"; // Ở lại trang chỉnh sửa
	}

// xóa
	@GetMapping("/delete-category/{id}")
	public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		// Kiểm tra nếu danh mục tồn tại
		DanhMuc danhMuc = danhMucService.findById(id);
		if (danhMuc == null) {
			return "redirect:/admin/category"; // Nếu không tìm thấy, quay về trang danh sách
		}
		// Kiểm tra xem danh mục có sản phẩm liên kết hay không
		if (danhMucService.hasProducts(id)) {
			redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục này vì nó đang chứa sản phẩm!");
			return "redirect:/admin/category"; // Quay về danh sách nếu danh mục có sản phẩm liên kết
		}
		// Xóa danh mục
		danhMucService.delete(id);

		// Quay về trang danh sách sau khi xóa thành công
		return "redirect:/admin/category";
	}

}
