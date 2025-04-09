package com.kimngan.ComesticAdmin.controller.seller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/seller")
public class SellerDanhGiaController {

	@Autowired
	private DanhGiaService danhGiaService;
	@Autowired
	private NguoiDungService nguoiDungService;


	@ModelAttribute("currentSeller")
	public NguoiDung getCurrentUser(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	@GetMapping("/danhgia")
	public String getAllDanhGias(Model model, @RequestParam(value = "soSao", required = false) Integer soSao,
			@RequestParam(value = "maSanPham", required = false) String maSanPham,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, HttpServletRequest request) {

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

		String requestUri = request.getRequestURI();
		model.addAttribute("requestUri", requestUri);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "seller/danhgia/index";
	}

	// Chi tiết đánh giá
	@GetMapping("/danhgia/{maDanhGia}")
	public String getDanhGiaDetail(@PathVariable("maDanhGia") Integer maDanhGia, Model model) {
		DanhGia danhGia = danhGiaService.findById(maDanhGia);

		if (danhGia == null) {
			model.addAttribute("errorMessage", "Không tìm thấy đánh giá.");
			return "redirect:/seller/danhgia";
		}

		model.addAttribute("danhGia", danhGia);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "seller/danhgia/detail"; // Giao diện chi tiết đánh giá bên seller
	}

	// Phản hồi đánh giá
	@PostMapping("/danhgia/{maDanhGia}/reply")
	public String replyToDanhGia(@PathVariable("maDanhGia") Integer maDanhGia,
			@RequestParam("adminReply") String adminReply, RedirectAttributes redirectAttributes) {
		DanhGia danhGia = danhGiaService.findById(maDanhGia);

		if (danhGia == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá.");
			return "redirect:/seller/danhgia";
		}

		danhGia.setAdminReply(adminReply);
		danhGiaService.save(danhGia);

		redirectAttributes.addFlashAttribute("successMessage", "Phản hồi đã được gửi.");
		return "redirect:/seller/danhgia";

	}

	@PostMapping("/danhgia/{maDanhGia}/delete")
	public String deleteDanhGia(@PathVariable("maDanhGia") Integer maDanhGia, RedirectAttributes redirectAttributes) {
		DanhGia danhGia = danhGiaService.findById(maDanhGia);

		if (danhGia == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá với mã: " + maDanhGia);
			return "redirect:/seller/danhgia";
		}

		danhGiaService.delete(maDanhGia);
		redirectAttributes.addFlashAttribute("successMessage", "Đánh giá đã được xóa thành công.");
		return "redirect:/seller/danhgia";
	}

}
