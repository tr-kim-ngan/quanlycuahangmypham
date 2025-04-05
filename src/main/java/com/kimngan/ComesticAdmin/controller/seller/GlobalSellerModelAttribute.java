package com.kimngan.ComesticAdmin.controller.seller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

@Component
@ControllerAdvice("com.kimngan.ComesticAdmin.controller.seller") // áp dụng cho controller seller
public class GlobalSellerModelAttribute {

    @Autowired
    private NguoiDungService nguoiDungService;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
       // NguoiDung customer = nguoiDungService.findByRole(2); // 2 = CUSTOMER
    	List<NguoiDung> customers = nguoiDungService.findByRole("CUSTOMER");
    	model.addAttribute("customer", customers);
    }
}
