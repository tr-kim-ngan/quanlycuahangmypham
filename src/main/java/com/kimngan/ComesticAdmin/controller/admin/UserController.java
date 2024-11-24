package com.kimngan.ComesticAdmin.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/login";  // Điều hướng đến trang login.html
    }

    // Điều hướng sau khi đăng xuất
//    @GetMapping("/logout")
//    public String logout() {
//        return "logout";  // Điều hướng đến trang logout.html
//    }
}
