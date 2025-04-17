package com.kimngan.ComesticAdmin.controller.admin;

import java.io.IOException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.StorageService;

@Controller
@RequestMapping("/admin")
public class AdminAccountController {

	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private StorageService storageService;

	@GetMapping("/account")
	public String showAdminAccount(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof NguoiDungDetails) {
			NguoiDungDetails userDetails = (NguoiDungDetails) auth.getPrincipal();
			NguoiDung admin = userDetails.getNguoiDung();
			model.addAttribute("admin", admin);
		} else {
			return "redirect:/admin/login";
		}

		return "admin/account"; // tên file HTML
	}

	@PostMapping("/account")
	public String updateAdminAccount(@ModelAttribute("admin") NguoiDung updatedUser,
			@RequestParam("avatarFile") MultipartFile avatarFile, Principal principal,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/admin/login";
		}

		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		if (currentUser == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
			return "redirect:/admin/account";
		}

		currentUser.setHoTen(updatedUser.getHoTen());
		currentUser.setEmail(updatedUser.getEmail());
		currentUser.setSoDienThoai(updatedUser.getSoDienThoai());
		currentUser.setDiaChi(updatedUser.getDiaChi());

		if (avatarFile != null && !avatarFile.isEmpty()) {
			try {
				String fileName = storageService.storeFile(avatarFile);
				currentUser.setAvatar(fileName);
			} catch (IOException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải ảnh đại diện.");
				return "redirect:/admin/account";
			}
		}

		nguoiDungService.save(currentUser);

		UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
				new NguoiDungDetails(currentUser,
						SecurityContextHolder.getContext().getAuthentication().getAuthorities()),
				null, SecurityContextHolder.getContext().getAuthentication().getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuth);

		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
		return "redirect:/admin/account";
	}

}