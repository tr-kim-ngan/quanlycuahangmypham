package com.kimngan.ComesticAdmin.controller.seller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;

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
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.StorageService;

@Controller
@RequestMapping("/seller")
public class SellerGlobalController {

	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private StorageService storageService;
	@ModelAttribute("currentSeller")
	public NguoiDung getCurrentSeller(Principal principal) {
	    if (principal != null) {
	        return nguoiDungService.findByTenNguoiDung(principal.getName());
	    }
	    return null;
	}
	@GetMapping("/account")
	public String showSellerAccountPage(Model model, Principal principal) {
	    if (principal == null) {
	        return "redirect:/seller/login";
	    }

	    NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
	    model.addAttribute("nguoiDung", currentUser);
	    return "seller/account"; // file account.html trong seller
	}

	@PostMapping("/account")
	public String updateSellerAccount(@ModelAttribute("nguoiDung") NguoiDung updatedUser,
	                                  @RequestParam("avatarFile") MultipartFile avatarFile,
	                                  Principal principal,
	                                  RedirectAttributes redirectAttributes) {
	    if (principal == null) {
	        return "redirect:/seller/login";
	    }

	    try {
	        NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());

	        currentUser.setHoTen(updatedUser.getHoTen());
	        currentUser.setEmail(updatedUser.getEmail());
	        currentUser.setSoDienThoai(updatedUser.getSoDienThoai());
	        currentUser.setDiaChi(updatedUser.getDiaChi());

	        if (avatarFile != null && !avatarFile.isEmpty()) {
	            String fileName = storageService.storeFile(avatarFile);
	            currentUser.setAvatar(fileName);
	        }

	        nguoiDungService.save(currentUser);
	        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật.");
	    }

	    return "redirect:/seller/account";
	}


}
