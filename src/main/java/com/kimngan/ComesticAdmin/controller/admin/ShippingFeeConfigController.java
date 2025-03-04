package com.kimngan.ComesticAdmin.controller.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;
import com.kimngan.ComesticAdmin.services.ShippingFeeConfigService;

@Controller
@RequestMapping("/admin/shipping-fee")
public class ShippingFeeConfigController {
	@Autowired
	private ShippingFeeConfigService shippingFeeConfigService;

	@GetMapping
	public String viewShippingConfig(Model model) {
		List<ShippingFeeConfig> shippingConfigs = shippingFeeConfigService.getAllShippingConfigs();
		model.addAttribute("shippingConfigs", shippingConfigs);
		model.addAttribute("canAddNew", shippingConfigs.size() < 2); // Chỉ cho phép thêm nếu chưa đủ 2 mức

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/shipping_fee/index";
	}


	  // Hiển thị trang thêm phí vận chuyển
    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<ShippingFeeConfig> existingConfigs = shippingFeeConfigService.getAllShippingConfigs();
        ShippingFeeConfig newConfig = new ShippingFeeConfig();

        if (existingConfigs.size() == 1) {
            // Nếu đã có một mức giá, thì mức mới phải bắt đầu từ maxOrderValue của bản ghi trước đó
            newConfig.setMinOrderValue(existingConfigs.get(0).getMaxOrderValue());
        }
        
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

        model.addAttribute("newConfig", newConfig);
        model.addAttribute("existingConfigs", existingConfigs);
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
    

    @GetMapping("/edit/{id}")
    public String editShippingConfig(@PathVariable("id") Integer id, Model model) {
        ShippingFeeConfig config = shippingFeeConfigService.getShippingConfigById(id);
        if (config == null) {
            return "redirect:/admin/shipping-fee";
        }
        
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
        
        model.addAttribute("config", config);
        return "admin/shipping_fee/edit";
    }


    // Cập nhật phí vận chuyển
    @PostMapping("/update")
    public String updateShippingConfig(@ModelAttribute ShippingFeeConfig config, RedirectAttributes redirectAttributes) {
        if (config.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: ID không hợp lệ!");
            return "redirect:/admin/shipping-fee";
        }
        try {
            shippingFeeConfigService.updateShippingConfig(config);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phí vận chuyển thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/shipping-fee/edit/" + config.getId();
        }
        return "redirect:/admin/shipping-fee";
    }

}
