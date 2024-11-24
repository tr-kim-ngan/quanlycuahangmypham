package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private DonHangService donHangService;

    // Hiển thị danh sách hóa đơn
    @GetMapping("/hoadon")
    public String getHoaDons(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "5") int size) {

        // Kiểm tra nếu page nhỏ hơn 0 thì đặt lại giá trị page về 0
        if (page < 0) {
            page = 0;
        }

        Page<HoaDon> hoaDonPage = hoaDonService.getAllHoaDons(PageRequest.of(page, size));
        model.addAttribute("hoaDons", hoaDonPage.getContent());
        model.addAttribute("currentPage", hoaDonPage.getNumber());
        model.addAttribute("totalPages", hoaDonPage.getTotalPages());
        model.addAttribute("size", size);

        // Thêm thông tin người dùng vào model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/hoadon/index";
    }

    // Xem chi tiết hóa đơn
    @GetMapping("/hoadon/{maDonHang}")
    public String viewHoaDon(@PathVariable("maDonHang") Integer maDonHang, Model model) {
        DonHang donHang = donHangService.getDonHangById(maDonHang);
        if (donHang == null) {
            return "redirect:/admin/orders"; // Nếu đơn hàng không tồn tại, chuyển về danh sách đơn hàng
        }

        HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
        if (hoaDon == null) {
            model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
            return "redirect:/admin/orders"; // Nếu hóa đơn không tồn tại, chuyển về danh sách đơn hàng
        }

        model.addAttribute("hoaDon", hoaDon);
        
        // Thêm thông tin người dùng vào model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/hoadon/view"; // Trả về view để hiển thị chi tiết hóa đơn
    }
}
