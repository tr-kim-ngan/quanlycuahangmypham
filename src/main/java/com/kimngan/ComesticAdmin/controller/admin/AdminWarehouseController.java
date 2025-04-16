package com.kimngan.ComesticAdmin.controller.admin;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
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

@Controller
@RequestMapping("/admin/warehouse-staff")
public class AdminWarehouseController {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private QuyenTruyCapRepository quyenTruyCapRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Thêm thông tin user vào model
    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
            NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
            model.addAttribute("user", userDetails);
        }
    }

    // 1. Danh sách nhân viên kho
    @GetMapping
    public String listWarehouseStaff(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<NguoiDung> nhanViens = nguoiDungService.findByRole("NHAP_KHO")
                .stream()
                .filter(NguoiDung::isTrangThai)
                .filter(nv -> keyword == null || nv.getHoTen().toLowerCase().contains(keyword.toLowerCase()))
                .sorted(Comparator.comparingInt(NguoiDung::getMaNguoiDung).reversed())
                .collect(Collectors.toList());

        model.addAttribute("warehouseStaff", nhanViens);
        model.addAttribute("keyword", keyword); // để hiển thị lại ô input nếu cần
        return "admin/warehouse/index";
    }


    // 2. Form thêm nhân viên kho
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "admin/warehouse/add";
    }

    // 3. Xử lý thêm
    @PostMapping("/add")
    public String addWarehouseStaff(@ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                                    RedirectAttributes redirectAttributes) {
      
    	if (!nguoiDung.getTenNguoiDung().matches("^[a-zA-Z0-9._-]{4,30}$")) {
            redirectAttributes.addFlashAttribute("error", "Tên người dùng không hợp lệ! Chỉ dùng chữ không dấu, số, và không có khoảng trắng.");
            return "redirect:/admin/warehouse-staff/add";
        }
    	if (nguoiDungService.existsByTenNguoiDung(nguoiDung.getTenNguoiDung())) {
            redirectAttributes.addFlashAttribute("error", "Tên người dùng đã tồn tại!");
            return "redirect:/admin/warehouse-staff/add";
        }

        if (nguoiDungService.existsByEmail(nguoiDung.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/admin/warehouse-staff/add";
        }

        if (nguoiDungService.existsBySoDienThoai(nguoiDung.getSoDienThoai())) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại đã tồn tại!");
            return "redirect:/admin/warehouse-staff/add";
        }

        nguoiDung.setQuyenTruyCap(quyenTruyCapRepository.findByTenQuyen("NHAP_KHO"));
        nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        nguoiDung.setTrangThai(true);
        nguoiDungService.save(nguoiDung);
        redirectAttributes.addFlashAttribute("success", "Thêm nhân viên kho thành công!");
        return "redirect:/admin/warehouse-staff";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
    	NguoiDung nhanVien = nguoiDungService.findById(id);
    	model.addAttribute("nguoiDung", nhanVien);
    	return "admin/warehouse-staff/edit"; // Giao diện sửa nhân viên kho
    }

    @PostMapping("/edit")
    public String updateWarehouseStaff(@ModelAttribute("nguoiDung") NguoiDung updatedNV,
    			RedirectAttributes redirectAttributes) {
    	// Tìm nhân viên hiện tại trong database
    	NguoiDung existingNV = nguoiDungService.findById(updatedNV.getMaNguoiDung());

    	if (existingNV == null) {
    		redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên cần cập nhật!");
    		return "redirect:/admin/warehouse-staff";
    	}
    	// Kiểm tra nếu tên đã tồn tại ở nhân viên khác
    	if (nguoiDungService.existsByTenNguoiDungAndNotId(updatedNV.getTenNguoiDung(), updatedNV.getMaNguoiDung())) {
    		redirectAttributes.addFlashAttribute("error", "Tên người dùng đã được sử dụng!");
    		return "redirect:/admin/warehouse-staff/edit/" + updatedNV.getMaNguoiDung();
    	}
    	// Kiểm tra nếu email đã tồn tại ở nhân viên khác
    	if (nguoiDungService.existsByEmailAndNotId(updatedNV.getEmail(), updatedNV.getMaNguoiDung())) {
    		redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
    		return "redirect:/admin/warehouse-staff/edit/" + updatedNV.getMaNguoiDung();
    	}
    	// Kiểm tra nếu số điện thoại đã tồn tại ở nhân viên khác
    	if (nguoiDungService.existsBySoDienThoaiAndNotId(updatedNV.getSoDienThoai(), updatedNV.getMaNguoiDung())) {
    		redirectAttributes.addFlashAttribute("error", "Số điện thoại đã được sử dụng!");
    		return "redirect:/admin/warehouse-staff/edit/" + updatedNV.getMaNguoiDung();
    	}

    	// Cập nhật thông tin nhân viên
    	existingNV.setTenNguoiDung(updatedNV.getTenNguoiDung());
    	existingNV.setEmail(updatedNV.getEmail());
    	existingNV.setSoDienThoai(updatedNV.getSoDienThoai());

    	if (updatedNV.getMatKhau() != null && !updatedNV.getMatKhau().isEmpty()) {
    		existingNV.setMatKhau(passwordEncoder.encode(updatedNV.getMatKhau()));
    	}

    	nguoiDungService.save(existingNV);
    	redirectAttributes.addFlashAttribute("success", "Cập nhật nhân viên kho thành công!");
    	return "redirect:/admin/warehouse-staff/edit/" + updatedNV.getMaNguoiDung();
    }

    @GetMapping("/delete/{id}")
    public String disableWarehouseStaff(@PathVariable("id") Integer id) {
        NguoiDung nguoiDung = nguoiDungService.findById(id);
        if (nguoiDung != null) {
            nguoiDung.setTrangThai(false);
            nguoiDungService.save(nguoiDung);
        }
        return "redirect:/admin/warehouse-staff";
    }
}
