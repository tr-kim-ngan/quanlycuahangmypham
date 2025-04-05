package com.kimngan.ComesticAdmin.controller.warehouse;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.StorageService;

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

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/warehouse/import")
public class WarehouseAccountController {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private StorageService storageService;

    @GetMapping("/account")
    public String showWarehouseAccount(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/warehouse/import/login";
        }

        NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
        model.addAttribute("nguoiDung", currentUser);
        return "warehouse/account";
    }

    @PostMapping("/account")
    public String updateWarehouseAccount(@ModelAttribute("nguoiDung") NguoiDung updatedUser,
                                         @RequestParam("avatarFile") MultipartFile avatarFile,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/warehouse/import/login";
        }

        NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "redirect:/warehouse/import/account";
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
                return "redirect:/warehouse/import/account";
            }
        }

        nguoiDungService.save(currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/warehouse/import/account";
    }
} 