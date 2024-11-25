package com.kimngan.ComesticAdmin.controller.customer;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

@Controller
@RequestMapping("/customer/rating")
public class DanhGiaController {

    @Autowired
    private DanhGiaService danhGiaService;

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private NguoiDungService nguoiDungService;
    @ModelAttribute
	public void addAttributes(Model model, Principal principal) {
	    if (principal != null) {
	        // Lấy tên đăng nhập từ Principal
	        String username = principal.getName();

	        // Tìm thông tin người dùng
	        NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(username);

	        // Thêm thông tin người dùng và timestamp vào Model
	        model.addAttribute("currentUser", currentUser);
	        model.addAttribute("timestamp", System.currentTimeMillis()); // Timestamp luôn được cập nhật
	    }
	}
    // Hiển thị form đánh giá sản phẩm
    @GetMapping("/create/{maSanPham}")
    public String showRatingForm(@PathVariable Integer maSanPham, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/customer/login";
        }

        SanPham sanPham = sanPhamService.findById(maSanPham);
        if (sanPham == null) {
            model.addAttribute("errorMessage", "Sản phẩm không tồn tại.");
            return "redirect:/customer/order";
        }

        model.addAttribute("sanPham", sanPham);
        return "customer/rating_form";
    }

    // Lưu đánh giá sản phẩm
    @PostMapping("/create")
    public String submitRating(@RequestParam("maSanPham") Integer maSanPham,
                               @RequestParam("soSao") Integer soSao,
                               @RequestParam("noiDung") String noiDung,
                               Principal principal) {
        if (principal == null) {
            return "redirect:/customer/login";
        }

        String username = principal.getName();
        NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);

        SanPham sanPham = sanPhamService.findById(maSanPham);
        if (sanPham == null) {
            return "redirect:/customer/order";
        }

        DanhGia danhGia = new DanhGia();
        danhGia.setSanPham(sanPham);
        danhGia.setNguoiDung(nguoiDung);
        danhGia.setSoSao(soSao);
        danhGia.setNoiDung(noiDung);
        danhGia.setThoiGianDanhGia(LocalDateTime.now());

        danhGiaService.saveDanhGia(danhGia);

        return "redirect:/customer/order";
    }
}
