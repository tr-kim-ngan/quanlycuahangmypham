package com.kimngan.ComesticAdmin.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;
import com.kimngan.ComesticAdmin.services.ShippingFeeConfigService;

@Controller
@RequestMapping("/admin/shipping-fee")
public class ShippingFeeConfigController {
    @Autowired
    private ShippingFeeConfigService shippingFeeConfigService;

    // ✅ Hiển thị danh sách phí vận chuyển
    @GetMapping
    public String viewShippingConfig(Model model) {
        
    	List<ShippingFeeConfig> shippingConfigs = shippingFeeConfigService.getAllShippingConfigs();
        
    	model.addAttribute("shippingConfigs", shippingConfigs);
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
    	return "admin/shipping_fee/index";
    }
    // Hiển thị trang thêm mới phí vận chuyển
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("newConfig", new ShippingFeeConfig());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
        return "admin/shipping_fee/add";
    }
    @PostMapping("/add")
    public String addShippingConfig(@ModelAttribute ShippingFeeConfig newConfig, RedirectAttributes redirectAttributes) {
    	 try {
             shippingFeeConfigService.saveShippingConfig(newConfig);
             redirectAttributes.addFlashAttribute("successMessage", "Thêm mới phí vận chuyển thành công!");
         } catch (IllegalArgumentException e) {
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
             return "redirect:/admin/shipping-fee/add";
         }
         return "redirect:/admin/shipping-fee";
    }
    
    // ✅ Hiển thị trang chỉnh sửa phí vận chuyển
    @GetMapping("/edit/{id}")
    public String editShippingConfig(@PathVariable("id") Integer id, Model model) {
        ShippingFeeConfig config = shippingFeeConfigService.getShippingConfigById(id);
        if (config == null) {
            return "redirect:/admin/shipping-fee";
        }
        model.addAttribute("config", config);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
        return "admin/shipping_fee/edit";
    }

    // ✅ Cập nhật phí vận chuyển
    @PostMapping("/update")
    public String updateShippingConfig(@ModelAttribute ShippingFeeConfig config, RedirectAttributes redirectAttributes) {
        shippingFeeConfigService.updateShippingConfig(config);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phí vận chuyển thành công!");
        return "redirect:/admin/shipping-fee";
    }
}

