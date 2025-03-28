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
@RequestMapping("/admin/customers")
public class AdminCustomerController {

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

    // 1️⃣ Hiển thị danh sách khách hàng
    @GetMapping
    public String listCustomers(Model model) {
        List<NguoiDung> customers = nguoiDungService.findByRole("CUSTOMER").stream()
                .filter(NguoiDung::isTrangThai)
                .sorted(Comparator.comparingInt(NguoiDung::getMaNguoiDung).reversed())
                .collect(Collectors.toList());
        model.addAttribute("customers", customers);
        return "admin/customer/index";
    }

    // 2️⃣ Hiển thị form thêm khách hàng
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        return "admin/customer/add";
    }

    // 3️⃣ Xử lý thêm khách hàng
    @PostMapping("/add")
    public String addCustomer(@ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                              RedirectAttributes redirectAttributes) {
        if (nguoiDungService.existsByTenNguoiDung(nguoiDung.getTenNguoiDung())) {
            redirectAttributes.addFlashAttribute("error", "Tên người dùng đã tồn tại!");
            return "redirect:/admin/customers/add";
        }

        if (nguoiDungService.existsByEmail(nguoiDung.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/admin/customers/add";
        }

        if (nguoiDungService.existsBySoDienThoai(nguoiDung.getSoDienThoai())) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại đã tồn tại!");
            return "redirect:/admin/customers/add";
        }

        nguoiDung.setQuyenTruyCap(quyenTruyCapRepository.findByTenQuyen("CUSTOMER"));
        nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        nguoiDung.setTrangThai(true);

        nguoiDungService.save(nguoiDung);
        redirectAttributes.addFlashAttribute("success", "Thêm khách hàng thành công!");

        return "redirect:/admin/customers";
    }

    // 4️⃣ Xóa mềm khách hàng
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id) {
        NguoiDung customer = nguoiDungService.findById(id);
        if (customer != null) {
            customer.setTrangThai(false);
            nguoiDungService.save(customer);
        }
        return "redirect:/admin/customers";
    }
}
