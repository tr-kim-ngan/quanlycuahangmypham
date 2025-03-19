package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.NhaCungCap;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.NhaCungCapService;
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
public class SupplierController {

	@Autowired
	private NhaCungCapService nhaCungCapService;

	// Hiển thị danh sách nhà cung cấp
	@GetMapping("/supplier")
	public String index(Model model, 
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		Page<NhaCungCap> pageNhaCungCap;

		if (keyword != null && !keyword.isEmpty()) {
			// Nếu có từ khóa tìm kiếm
            pageNhaCungCap = nhaCungCapService.searchByName(keyword, PageRequest.of(page, size, Sort.by("maNhaCungCap").descending()));
			model.addAttribute("keyword", keyword);
		} else {
			// Nếu không có từ khóa, lấy tất cả nhà cung cấp đang hoạt động
			 // Nếu không có từ khóa, lấy tất cả nhà cung cấp đang hoạt động, sắp xếp theo `maNhaCungCap` giảm dần
            pageNhaCungCap = nhaCungCapService.findAllActive(PageRequest.of(page, size, Sort.by("maNhaCungCap").descending()));
 		}
		  // Sắp xếp danh sách theo thứ tự mới nhất
       		// Kiểm tra nếu trang yêu cầu vượt quá tổng số trang, điều hướng về trang cuối
		// cùng

	       // Truyền dữ liệu vào Model
        model.addAttribute("listNhaCungCap", pageNhaCungCap.getContent());
        model.addAttribute("currentPage", pageNhaCungCap.getNumber());
        model.addAttribute("totalPages", pageNhaCungCap.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/admin/supplier"); // Đường dẫn cho tìm kiếm

		// Thêm thông tin người dùng để hiển thị
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/supplier/index";
	}

	// Trang thêm nhà cung cấp
	@GetMapping("/add-supplier")
	public String addSupplier(Model model) {

		model.addAttribute("nhaCungCap", new NhaCungCap());

		// Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/supplier/add";
	}

	@PostMapping("/add-supplier")
	public String saveSupplier(@ModelAttribute("nhaCungCap") NhaCungCap nhaCungCap, Model model) {

		// Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Kiểm tra số điện thoại đã tồn tại
		Boolean isPhoneDuplicate = nhaCungCapService.existsBySdtNhaCungCap(nhaCungCap.getSdtNhaCungCap());
		if (isPhoneDuplicate) {
			model.addAttribute("error", "Số điện thoại đã tồn tại. Vui lòng nhập số khác.");
			return "admin/supplier/add";
		}

		// Kiểm tra định dạng email
		String email = nhaCungCap.getEmailNhaCungCap();
		if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")) {
			model.addAttribute("error", "Email không đúng định dạng. Vui lòng nhập lại.");
			return "admin/supplier/add";
		}
		// Kiểm tra số điện thoại có đúng 10 số không
		if (nhaCungCap.getSdtNhaCungCap().length() != 10) {
			model.addAttribute("error", "Số điện thoại phải có đúng 10 chữ số.");
			return "admin/supplier/add";
		}

		// Kiểm tra email có bị trùng không
		Boolean isEmailDuplicate = nhaCungCapService.existsByEmailNhaCungCap(email);

		if (isEmailDuplicate) {
			model.addAttribute("error", "Email đã tồn tại. Vui lòng nhập email khác.");
			return "admin/supplier/add";
		}

		Boolean isCreated = nhaCungCapService.create(nhaCungCap);
		if (isCreated) {
			return "redirect:/admin/supplier";
		} else {
			model.addAttribute("error", "Tên nhà cung cấp này đã tồn tại"); // Thêm thông báo lỗi
			return "admin/supplier/add";
		}
	}

	// Trang chỉnh sửa nhà cung cấp
	@GetMapping("/edit-supplier/{id}")
	public String editSupplierPage(@PathVariable("id") Integer id, Model model) {
		Optional<NhaCungCap> nhaCungCapOpt = nhaCungCapService.findByIdOptional(id);
		if (!nhaCungCapOpt.isPresent()) {
			return "redirect:/admin/supplier"; // Nếu không tìm thấy, quay lại danh sách nhà cung cấp
		}

		model.addAttribute("nhaCungCap", nhaCungCapOpt.get());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/supplier/edit";
	}

	// Phương thức POST để xử lý dữ liệu từ form chỉnh sửa
	@PostMapping("/edit-supplier/{id}")
	public String updateSupplier(@PathVariable("id") Integer id, @ModelAttribute("nhaCungCap") NhaCungCap nhaCungCap) {
		Optional<NhaCungCap> existingSupplierOpt = nhaCungCapService.findByIdOptional(id);
		if (!existingSupplierOpt.isPresent()) {
			return "redirect:/admin/supplier"; // Nếu không tìm thấy, quay lại danh sách nhà cung cấp
		}

		NhaCungCap existingSupplier = existingSupplierOpt.get();
		// Cập nhật các trường cần thiết
		existingSupplier.setTenNhaCungCap(nhaCungCap.getTenNhaCungCap());
		existingSupplier.setSdtNhaCungCap(nhaCungCap.getSdtNhaCungCap());
		existingSupplier.setDiaChiNhaCungCap(nhaCungCap.getDiaChiNhaCungCap());
		existingSupplier.setEmailNhaCungCap(nhaCungCap.getEmailNhaCungCap());

		// Lưu lại thay đổi
		nhaCungCapService.update(existingSupplier);

		return "redirect:/admin/supplier";
	}


	@PostMapping("/delete-supplier/{id}")
	public String deleteSupplier(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		Optional<NhaCungCap> supplierOpt = nhaCungCapService.findByIdOptional(id);
		if (supplierOpt.isPresent()) {
			NhaCungCap supplier = supplierOpt.get();
			 // Kiểm tra xem nhà cung cấp có liên quan đến đơn nhập hàng hay không
	        if (supplier.getDonNhapHangs() == null || supplier.getDonNhapHangs().isEmpty()) {
	            // Nếu không liên quan đến đơn nhập hàng, xóa hoàn toàn
	            nhaCungCapService.deleteById(id);
	            redirectAttributes.addFlashAttribute("successMessage", "Nhà cung cấp đã được xóa hoàn toàn.");
	        } else {
	            // Nếu liên quan đến đơn nhập hàng, chỉ chuyển trạng thái
	            supplier.setTrangThai(false);
	            nhaCungCapService.update(supplier);
	            redirectAttributes.addFlashAttribute("successMessage", "Nhà cung cấp đã được chuyển trạng thái không hoạt động.");
	        }
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "Nhà cung cấp không tồn tại.");
	    }
		return "redirect:/admin/supplier";
	}

}
