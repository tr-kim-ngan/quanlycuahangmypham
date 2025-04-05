package com.kimngan.ComesticAdmin.controller.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

@Component
@ControllerAdvice("com.kimngan.ComesticAdmin.controller.customer") // Chỉ áp dụng cho bên customer
public class GlobalModelAttributeConfig {

    @Autowired
    private NguoiDungService nguoiDungService;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        NguoiDung seller = nguoiDungService.findFirstByRole(5); // 5 = SELLER
        model.addAttribute("seller", seller);
    }
}
