package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.DonViTinh;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.DonViTinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class UnitController {

	@Autowired
	private DonViTinhService donViTinhService;

	// Hiển thị danh sách đơn vị tính
	@GetMapping("/unit")
	public String index(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<DonViTinh> pageDonViTinh;

		if (keyword != null && !keyword.isEmpty()) {
	        // Tìm kiếm và sắp xếp theo mã đơn vị tính giảm dần
	        pageDonViTinh = donViTinhService.searchByName(keyword,
	                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonVi")));
	        model.addAttribute("keyword", keyword);
	    } else {
	        // Hiển thị tất cả và sắp xếp theo mã đơn vị tính giảm dần
	        pageDonViTinh = donViTinhService.findAll(
	                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonVi")));
	    }

	    if (page > pageDonViTinh.getTotalPages()) {
	        pageDonViTinh = donViTinhService.findAll(
	                PageRequest.of(pageDonViTinh.getTotalPages() - 1, size, Sort.by(Sort.Direction.DESC, "maDonVi")));
	    }
	    
		model.addAttribute("listDonViTinh", pageDonViTinh.getContent());

		// model.addAttribute("listUnits", pageDonViTinh.getContent());
		model.addAttribute("currentPage", pageDonViTinh.getNumber());
		model.addAttribute("totalPages", pageDonViTinh.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/admin/unit");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/unit/index";
	}

	// Trang thêm đơn vị tính
	@GetMapping("/add-unit")
	public String addUnit(Model model) {
		model.addAttribute("donViTinh", new DonViTinh());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/unit/add";
	}

	@PostMapping("/add-unit")
	public String saveUnit(@ModelAttribute("donViTinh") DonViTinh donViTinh, Model model) {
		// Kiểm tra xem tên đơn vị đã tồn tại chưa
		if (donViTinhService.existsByTenDonVi(donViTinh.getTenDonVi())) {
			model.addAttribute("error", "Tên đơn vị đã tồn tại!");
			// Đảm bảo thông tin người dùng vẫn được hiển thị
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
			return "admin/unit/add"; // Quay lại trang thêm nếu tên đơn vị đã tồn tại
		}

		// Nếu không trùng, tiến hành thêm mới đơn vị
		if (this.donViTinhService.create(donViTinh) != null) {
			return "redirect:/admin/unit";
		} else {
			model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
			return "admin/unit/add";
		}

	}

	// Trang chỉnh sửa đơn vị tính
	@GetMapping("/edit-unit/{id}")
	public String editUnit(@PathVariable("id") Integer id, Model model) {
		Optional<DonViTinh> optionalDonViTinh = donViTinhService.findById(id);
		if (!optionalDonViTinh.isPresent()) {
			return "redirect:/admin/unit";
		}

		DonViTinh donViTinh = optionalDonViTinh.get();
		model.addAttribute("donViTinh", donViTinh);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/unit/edit";
	}

	@PostMapping("/edit-unit/{id}")
	public String updateUnit(@PathVariable("id") Integer id, @ModelAttribute("donViTinh") DonViTinh donViTinh,
			Model model) {
		// Kiểm tra xem đơn vị tính có trùng tên với đơn vị khác không
		Optional<DonViTinh> existingDonViTinh = donViTinhService.findByTenDonVi(donViTinh.getTenDonVi());

		// Nếu tên đơn vị tính đã tồn tại và không phải của chính đơn vị hiện tại (được
		// sửa)
		if (existingDonViTinh.isPresent() && !existingDonViTinh.get().getMaDonVi().equals(id)) {
			model.addAttribute("error", "Tên đơn vị đã tồn tại ở đơn vị khác!");

			// Thêm thông tin người dùng vào model
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);

			return "admin/unit/edit";
		}

		// Cập nhật đơn vị tính
		donViTinh.setMaDonVi(id); // Gán mã đơn vị tính để cập nhật đúng đối tượng
		if (donViTinhService.update(donViTinh)) {
			return "redirect:/admin/unit"; // Chuyển hướng nếu cập nhật thành công
		} else {
			model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
			return "admin/unit/edit"; // Hiển thị lại trang nếu có lỗi
		}
	}

	@GetMapping("/delete-unit/{id}")
	public String deleteUnit(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
	    if (donViTinhService.hasProducts(id)) {
	        redirectAttributes.addFlashAttribute("error", "Không thể xóa đơn vị vì đang được sử dụng bởi sản phẩm!");
	        return "redirect:/admin/unit";
	    }

	    donViTinhService.delete(id);
	    redirectAttributes.addFlashAttribute("success", "Xóa đơn vị thành công!");
	    return "redirect:/admin/unit";
	}


}
