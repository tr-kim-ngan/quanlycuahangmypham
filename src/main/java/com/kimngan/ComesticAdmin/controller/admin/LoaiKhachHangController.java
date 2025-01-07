package com.kimngan.ComesticAdmin.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kimngan.ComesticAdmin.entity.LoaiKhachHang;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.LoaiKhachHangService;

@Controller
@RequestMapping("/admin/loai-khach-hang")
public class LoaiKhachHangController {

    @Autowired
    private LoaiKhachHangService loaiKhachHangService;

    @GetMapping
    public String listLoaiKhachHang(Model model) {
        List<LoaiKhachHang> loaiKhachHangList = loaiKhachHangService.getAllLoaiKhachHang();
        model.addAttribute("loaiKhachHangList", loaiKhachHangList);

        // Thêm thông tin người dùng vào model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
            NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
            model.addAttribute("user", userDetails);
        }

        return "admin/loai-khach-hang/index";
    }
}


