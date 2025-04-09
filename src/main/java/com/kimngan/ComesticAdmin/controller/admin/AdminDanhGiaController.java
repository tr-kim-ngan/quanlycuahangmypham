package com.kimngan.ComesticAdmin.controller.admin;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/admin")
public class AdminDanhGiaController {

	@Autowired
	private DanhGiaService danhGiaService;

	
	@Autowired
	private SanPhamService sanPhamService;

	// Hiển thị danh sách đánh giá

//	@GetMapping("/danhgia")
//	public String getAllDanhGias(Model model, @RequestParam(value = "soSao", required = false) Integer soSao,
//			@RequestParam(value = "keyword", required = false) String keyword, HttpServletRequest request
//
//	) {
//		// Lấy tất cả sản phẩm có đánh giá
//		List<SanPham> sanPhams;
//
//		if (soSao != null  && soSao > 0) {
//			// Tìm tất cả sản phẩm có đánh giá và trạng thái = true với số sao tương ứng
//			sanPhams = sanPhamService.findAllWithDanhGiasAndTrangThaiTrueBySoSao(soSao);
//		} else {
//			// Lấy tất cả sản phẩm có trạng thái = true và có đánh giá
//			sanPhams = sanPhamService.findAllWithDanhGiasAndTrangThaiTrue();
//		}
//
//		model.addAttribute("sanPhams", sanPhams);
//		model.addAttribute("soSao", soSao); // Giữ lại giá trị số sao trong form sau khi tìm kiếm
//
//		// Lấy thông tin URL hiện tại để sử dụng cho form tìm kiếm
//		String requestUri = request.getRequestURI();
//		model.addAttribute("requestUri", requestUri);
//		// Thêm thông tin người dùng để hiển thị
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
//		model.addAttribute("user", userDetails);
//
//		return "admin/danhgia/index"; // Giao diện danh sách đánh giá
//	}
//
//	// Hiển thị chi tiết đánh giá và cho phép admin phản hồi
//	@GetMapping("/danhgia/{maDanhGia}")
//	public String getDanhGiaDetail(@PathVariable("maDanhGia") Integer maDanhGia, Model model) {
//		DanhGia danhGia = danhGiaService.findById(maDanhGia);
//
//		if (danhGia == null) {
//			model.addAttribute("errorMessage", "Không tìm thấy đánh giá với mã: " + maDanhGia);
//			return "redirect:/admin/danhgia";
//		}
//
//		model.addAttribute("danhGia", danhGia);
//
//		// Thêm thông tin người dùng để hiển thị
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
//		model.addAttribute("user", userDetails);
//
//		return "admin/danhgia/detail"; // Giao diện chi tiết đánh giá
//	}
//
//	// Xử lý phản hồi của admin
//	@PostMapping("/danhgia/{maDanhGia}/reply")
//	public String replyToDanhGia(@PathVariable("maDanhGia") Integer maDanhGia,
//			@RequestParam("adminReply") String adminReply, RedirectAttributes redirectAttributes) {
//		DanhGia danhGia = danhGiaService.findById(maDanhGia);
//
//		if (danhGia == null) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá với mã: " + maDanhGia);
//			return "redirect:/admin/danhgia";
//		}
//
//		// Cập nhật phản hồi của admin
//		danhGia.setAdminReply(adminReply);
//		danhGiaService.save(danhGia);
//
//		redirectAttributes.addFlashAttribute("successMessage", "Phản hồi của bạn đã được lưu thành công.");
//		return "redirect:/admin/danhgia/" + maDanhGia;
//	}
//
//	// Xóa đánh giá
//	@PostMapping("/danhgia/{maDanhGia}/delete")
//	public String deleteDanhGia(@PathVariable("maDanhGia") Integer maDanhGia, RedirectAttributes redirectAttributes) {
//		DanhGia danhGia = danhGiaService.findById(maDanhGia);
//
//		if (danhGia == null) {
//			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá với mã: " + maDanhGia);
//			return "redirect:/admin/danhgia";
//		}
//
//		danhGiaService.delete(maDanhGia);
//		redirectAttributes.addFlashAttribute("successMessage", "Đánh giá đã được xóa thành công.");
//		return "redirect:/admin/danhgia";
//	}
	// ✅ Dùng chung để lấy user từ session (tránh lỗi khi authentication = null)
    @ModelAttribute("user")
    public NguoiDungDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    // ✅ Trang danh sách đánh giá
    @GetMapping("/danhgia")
    public String getAllDanhGias(Model model,
                                 @RequestParam(value = "soSao", required = false) Integer soSao,
                                 @RequestParam(value = "maSanPham", required = false) String maSanPham,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("thoiGianDanhGia").descending());
        Page<DanhGia> danhGiaPage;

        if (maSanPham != null && !maSanPham.trim().isEmpty()) {
            danhGiaPage = danhGiaService.findAllWithSanPhamAndNguoiDungByMaSanPham(maSanPham.trim(), pageable);
        } else if (soSao != null && soSao > 0) {
            danhGiaPage = danhGiaService.findAllWithSanPhamAndNguoiDungBySoSao(soSao, pageable);
        } else {
            danhGiaPage = danhGiaService.findAllWithSanPhamAndNguoiDung(pageable);
        }

        model.addAttribute("danhGias", danhGiaPage.getContent());
        model.addAttribute("soSao", soSao);
        model.addAttribute("maSanPham", maSanPham);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", danhGiaPage.getTotalPages());

        model.addAttribute("requestUri", request.getRequestURI());

        return "admin/danhgia/index";
    }

    // ✅ Trang chi tiết đánh giá
    @GetMapping("/danhgia/{maDanhGia}")
    public String getDanhGiaDetail(@PathVariable("maDanhGia") Integer maDanhGia, Model model) {
        DanhGia danhGia = danhGiaService.findById(maDanhGia);

        if (danhGia == null) {
            model.addAttribute("errorMessage", "Không tìm thấy đánh giá.");
            return "redirect:/admin/danhgia";
        }

        model.addAttribute("danhGia", danhGia);
        return "admin/danhgia/detail";
    }

    // ✅ Phản hồi đánh giá
    @PostMapping("/danhgia/{maDanhGia}/reply")
    public String replyToDanhGia(@PathVariable("maDanhGia") Integer maDanhGia,
                                 @RequestParam("adminReply") String adminReply,
                                 RedirectAttributes redirectAttributes) {
        DanhGia danhGia = danhGiaService.findById(maDanhGia);

        if (danhGia == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá.");
            return "redirect:/admin/danhgia";
        }

        danhGia.setAdminReply(adminReply);
        danhGiaService.save(danhGia);

        redirectAttributes.addFlashAttribute("successMessage", "Phản hồi của bạn đã được lưu.");
        return "redirect:/admin/danhgia";
    }

    // ✅ Xóa đánh giá
    @PostMapping("/danhgia/{maDanhGia}/delete")
    public String deleteDanhGia(@PathVariable("maDanhGia") Integer maDanhGia, RedirectAttributes redirectAttributes) {
        DanhGia danhGia = danhGiaService.findById(maDanhGia);

        if (danhGia == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá.");
            return "redirect:/admin/danhgia";
        }

        danhGiaService.delete(maDanhGia);
        redirectAttributes.addFlashAttribute("successMessage", "Đánh giá đã được xóa thành công.");
        return "redirect:/admin/danhgia";
    }

}
