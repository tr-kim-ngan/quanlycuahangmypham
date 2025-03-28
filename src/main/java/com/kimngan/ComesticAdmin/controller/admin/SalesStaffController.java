package com.kimngan.ComesticAdmin.controller.admin;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.repository.QuyenTruyCapRepository;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

@Controller
@RequestMapping("/admin/sales-staff")
public class SalesStaffController {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private QuyenTruyCapRepository quyenTruyCapRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
            NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
            model.addAttribute("user", userDetails);
        }
    }

    // 1. Danh sách nhân viên bán hàng
    @GetMapping
    public String listSalesStaff(Model model) {
        List<NguoiDung> nhanViens = nguoiDungService.findByRole("NHAN_VIEN_BAN_HANG")
                .stream()
                .filter(NguoiDung::isTrangThai)
                .sorted(Comparator.comparingInt(NguoiDung::getMaNguoiDung).reversed())
                .collect(Collectors.toList());
        model.addAttribute("salesStaff", nhanViens);
        return "admin/sales/index";
    }

    // 2. Hiển thị form thêm nhân viên bán hàng
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "admin/sales/add";
    }

    // 3. Xử lý thêm
    @PostMapping("/add")
    public String addSalesStaff(@ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                                RedirectAttributes redirectAttributes) {
    	if (!nguoiDung.getTenNguoiDung().matches("^[a-zA-Z0-9._-]{4,30}$")) {
            redirectAttributes.addFlashAttribute("error", "Tên người dùng không hợp lệ! Chỉ dùng chữ không dấu, số, và không có khoảng trắng.");
            return "redirect:/admin/warehouse-staff/add";
        }
        if (nguoiDungService.existsByTenNguoiDung(nguoiDung.getTenNguoiDung())) {
            redirectAttributes.addFlashAttribute("error", "Tên người dùng đã tồn tại!");
            return "redirect:/admin/sales-staff/add";
        }

        if (nguoiDungService.existsByEmail(nguoiDung.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/admin/sales-staff/add";
        }

        if (nguoiDungService.existsBySoDienThoai(nguoiDung.getSoDienThoai())) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại đã tồn tại!");
            return "redirect:/admin/sales-staff/add";
        }

        nguoiDung.setQuyenTruyCap(quyenTruyCapRepository.findByTenQuyen("NHAN_VIEN_BAN_HANG"));
        nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        nguoiDung.setTrangThai(true);
        nguoiDungService.save(nguoiDung);
        redirectAttributes.addFlashAttribute("success", "Thêm nhân viên bán hàng thành công!");
        return "redirect:/admin/sales-staff";
    }

    // 4. Xóa (chuyển trạng thái false)
    @GetMapping("/delete/{id}")
    public String deleteSalesStaff(@PathVariable("id") Integer id) {
        NguoiDung staff = nguoiDungService.findById(id);
        if (staff != null) {
            staff.setTrangThai(false);
            nguoiDungService.save(staff);
        }
        return "redirect:/admin/sales-staff";
    }
}
