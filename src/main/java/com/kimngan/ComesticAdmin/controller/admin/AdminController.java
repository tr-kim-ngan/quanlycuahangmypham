package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/index"})
    public String adminHome(Model model) {
        // Lấy thông tin người dùng đã đăng nhập
        NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Thêm thông tin người dùng vào model
        model.addAttribute("user", nguoiDungDetails);


        return "admin/index";  // Trang chính cho admin sau khi đăng nhập thành công
    }

    // Thêm một phương thức để chuyển hướng từ "/" sang "/admin/index"

}
