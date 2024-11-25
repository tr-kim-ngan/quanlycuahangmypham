package com.kimngan.ComesticAdmin.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/admin")
public class AdminDanhGiaController {

    @Autowired
    private DanhGiaService danhGiaService;

    @Autowired
    private NguoiDungService nguoiDungService;
    @Autowired
    private SanPhamService sanPhamService;

    // Hiển thị danh sách đánh giá
    @GetMapping("/danhgia")
    public String getAllDanhGias(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "5") int size) {
        if (page < 0) {
            page = 0;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<DanhGia> danhGiaPage = danhGiaService.getAllDanhGias(pageable);
        Page<SanPham> sanPhamPage = sanPhamService.findAll(pageable);
        model.addAttribute("sanPhams", sanPhamPage.getContent());

        model.addAttribute("danhGias", danhGiaPage.getContent());
        model.addAttribute("currentPage", danhGiaPage.getNumber());
        model.addAttribute("totalPages", danhGiaPage.getTotalPages());
        model.addAttribute("size", size);

        // Thêm thông tin người dùng để hiển thị
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/danhgia/index"; // Giao diện danh sách đánh giá
    }
//    @GetMapping("/danhgia")
//    public String getAllDanhGiaBySanPham(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
//                                         @RequestParam(value = "size", defaultValue = "5") int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<SanPham> sanPhamPage = sanPhamService.findAllActive(pageable);
//
//        model.addAttribute("sanPhams", sanPhamPage.getContent());
//        model.addAttribute("currentPage", sanPhamPage.getNumber());
//        model.addAttribute("totalPages", sanPhamPage.getTotalPages());
//        model.addAttribute("size", size);
//     // Thêm thông tin người dùng để hiển thị
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
//        model.addAttribute("user", userDetails);
//        return "admin/danhgia/index";
//    }

    // Hiển thị chi tiết đánh giá và cho phép admin phản hồi
    @GetMapping("/danhgia/{maDanhGia}")
    public String getDanhGiaDetail(@PathVariable("maDanhGia") Integer maDanhGia, Model model) {
        DanhGia danhGia = danhGiaService.findById(maDanhGia);

        if (danhGia == null) {
            model.addAttribute("errorMessage", "Không tìm thấy đánh giá với mã: " + maDanhGia);
            return "redirect:/admin/danhgia";
        }

        model.addAttribute("danhGia", danhGia);

        // Thêm thông tin người dùng để hiển thị
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/danhgia/detail"; // Giao diện chi tiết đánh giá
    }

    // Xử lý phản hồi của admin
    @PostMapping("/danhgia/{maDanhGia}/reply")
    public String replyToDanhGia(@PathVariable("maDanhGia") Integer maDanhGia,
                                 @RequestParam("adminReply") String adminReply,
                                 RedirectAttributes redirectAttributes) {
        DanhGia danhGia = danhGiaService.findById(maDanhGia);

        if (danhGia == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá với mã: " + maDanhGia);
            return "redirect:/admin/danhgia";
        }

        // Cập nhật phản hồi của admin
        danhGia.setAdminReply(adminReply);
        danhGiaService.save(danhGia);

        redirectAttributes.addFlashAttribute("successMessage", "Phản hồi của bạn đã được lưu thành công.");
        return "redirect:/admin/danhgia/" + maDanhGia;
    }

}
