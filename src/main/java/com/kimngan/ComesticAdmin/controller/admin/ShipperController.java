package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.QuyenTruyCap;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.repository.QuyenTruyCapRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/admin/shipper")
public class ShipperController {

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private QuyenTruyCapRepository quyenTruyCapRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// 💡 Thêm thông tin user vào model để tránh lỗi Thymeleaf
	@ModelAttribute
	public void addUserToModel(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
		}
	}

	// 1️ Xem danh sách Shipper
	@GetMapping
	public String listShippers(Model model) {
		List<NguoiDung> shippers = nguoiDungService.findByRole("SHIPPER")
				  .stream()
	                .sorted(Comparator.comparingInt(NguoiDung::getMaNguoiDung).reversed()) // Sắp xếp giảm dần
	                .collect(Collectors.toList());
				
		System.out.println("DEBUG - Shippers trong Controller: " + shippers); // Debug
		model.addAttribute("shippers", shippers);
		return "admin/shipper/index";
	}

	// 2️ Hiển thị form thêm Shipper
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("nguoiDung", new NguoiDung());
		return "admin/shipper/add"; // Giao diện thêm shipper
	}

	// 3️ Xử lý thêm Shipper

	@PostMapping("/add")
	public String addShipper(@ModelAttribute("nguoiDung") NguoiDung nguoiDung, 
			RedirectAttributes redirectAttributes) {
		 // Kiểm tra nếu tên đã tồn tại
	    if (nguoiDungService.existsByTenNguoiDung(nguoiDung.getTenNguoiDung())) {
	        redirectAttributes.addFlashAttribute("error", "Tên người dùng đã được sử dụng!");
	        return "redirect:/admin/shipper/add";
	    }
	    // Kiểm tra nếu email đã tồn tại
	    if (nguoiDungService.existsByEmail(nguoiDung.getEmail())) {
	        redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
	        return "redirect:/admin/shipper/add";
	    }

	    // Kiểm tra nếu số điện thoại đã tồn tại
	    if (nguoiDungService.existsBySoDienThoai(nguoiDung.getSoDienThoai())) {
	        redirectAttributes.addFlashAttribute("error", "Số điện thoại đã được sử dụng!");
	        return "redirect:/admin/shipper/add";
	    }

	    // Gán quyền SHIPPER
	    QuyenTruyCap quyenShipper = quyenTruyCapRepository.findByTenQuyen("SHIPPER");
	    nguoiDung.setQuyenTruyCap(quyenShipper);

	    // Mã hóa mật khẩu trước khi lưu
	    nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));

	    // Lưu vào DB
	    nguoiDungService.save(nguoiDung);
	    redirectAttributes.addFlashAttribute("success", "Thêm shipper thành công!");

	    return "redirect:/admin/shipper";
	}


	// 4️⃣ Hiển thị form sửa Shipper
	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Integer id, Model model) {
		NguoiDung shipper = nguoiDungService.findById(id);
		model.addAttribute("nguoiDung", shipper);
		return "admin/shipper/edit"; // Giao diện sửa shipper
	}

	// 5️⃣ Xử lý cập nhật Shipper
	@PostMapping("/edit")
	public String updateShipper(@ModelAttribute("nguoiDung") NguoiDung updatedShipper, RedirectAttributes redirectAttributes) {
	    // Tìm Shipper hiện tại trong database
	    NguoiDung existingShipper = nguoiDungService.findById(updatedShipper.getMaNguoiDung());

	    if (existingShipper == null) {
	        redirectAttributes.addFlashAttribute("error", "Không tìm thấy shipper cần cập nhật!");
	        return "redirect:/admin/shipper";
	    }
	    // Kiểm tra nếu tên đã tồn tại ở một Shipper khác
	    if (nguoiDungService.existsByTenNguoiDungAndNotId(updatedShipper.getTenNguoiDung(), updatedShipper.getMaNguoiDung())) {
	        redirectAttributes.addFlashAttribute("error", "Tên người dùng đã được sử dụng!");
	        return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();
	    }

	    // Kiểm tra nếu email đã tồn tại ở một Shipper khác
	    if (nguoiDungService.existsByEmailAndNotId(updatedShipper.getEmail(), updatedShipper.getMaNguoiDung())) {
	        System.out.println("❌ Lỗi: Email đã tồn tại!");
	        redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
	        return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();
	    }

	    // Kiểm tra nếu số điện thoại đã tồn tại ở một Shipper khác
	    if (nguoiDungService.existsBySoDienThoaiAndNotId(updatedShipper.getSoDienThoai(), updatedShipper.getMaNguoiDung())) {
	        System.out.println("❌ Lỗi: Số điện thoại đã tồn tại!");
	        redirectAttributes.addFlashAttribute("error", "Số điện thoại đã được sử dụng!");
	        return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();
	    }

	    // Cập nhật thông tin Shipper
	    existingShipper.setTenNguoiDung(updatedShipper.getTenNguoiDung());
	    existingShipper.setEmail(updatedShipper.getEmail());
	    existingShipper.setSoDienThoai(updatedShipper.getSoDienThoai());

	    // Chỉ mã hóa mật khẩu nếu admin nhập mật khẩu mới
	    if (updatedShipper.getMatKhau() != null && !updatedShipper.getMatKhau().isEmpty()) {
	        existingShipper.setMatKhau(passwordEncoder.encode(updatedShipper.getMatKhau()));
	    }

	    // Lưu vào DB
	    nguoiDungService.save(existingShipper);
	    redirectAttributes.addFlashAttribute("success", "Cập nhật shipper thành công!");

	    return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();

	}



	// 4️ Xóa hoặc vô hiệu hóa Shipper
	@GetMapping("/delete/{id}")
	public String deleteShipper(@PathVariable("id") Integer id) {
		nguoiDungService.deleteById(id);
		return "redirect:/admin/shipper";
	}
}
