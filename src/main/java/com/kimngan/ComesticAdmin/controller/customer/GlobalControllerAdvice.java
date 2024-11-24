package com.kimngan.ComesticAdmin.controller.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.services.GioHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private GioHangService gioHangService;
	@Autowired
	private NguoiDungService nguoiDungService;
    

	 @ModelAttribute("cartCount")
	    public Integer getCartCount(Principal principal) {
	        if (principal != null) {
	            NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
	            List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);
	            return cartItems.size();
	        }
	        return 0;
	    }
	 @GetMapping("/count")
	 @ResponseBody
	 public Integer getCartItemCount(Principal principal) {
	     if (principal != null) {
	         NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
	         List<ChiTietGioHang> cartItems = gioHangService.viewCartItems(currentUser);
	         int itemCount = cartItems.size();
	         System.out.println("Số lượng sản phẩm trong giỏ hàng: " + itemCount); // In ra console
	         return itemCount;
	     }
	     System.out.println("Số lượng sản phẩm trong giỏ hàng: 0"); // In ra console
	     return 0;
	 }
	 @ModelAttribute("cartItems")
	    public List<ChiTietGioHang> getCartItems(Principal principal) {
	        if (principal != null) {
	            NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
	            return gioHangService.viewCartItems(currentUser);
	        }
	        return Collections.emptyList();
	    }

}
