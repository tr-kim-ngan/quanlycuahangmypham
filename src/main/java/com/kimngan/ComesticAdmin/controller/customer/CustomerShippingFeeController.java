package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;
import com.kimngan.ComesticAdmin.services.ShippingFeeConfigService;

@Controller
@RequestMapping("/shipping-fee")
public class CustomerShippingFeeController {
	@Autowired
	private ShippingFeeConfigService shippingFeeConfigService;
	
	 @GetMapping
	    public String getShippingFees(Model model) {
	        List<ShippingFeeConfig> shippingFees = shippingFeeConfigService.getAllShippingConfigs();
	        
	        // Lấy mức minOrderValue lớn nhất của các đơn hàng có phí giao hàng = 0
	        BigDecimal maxOrderValue = shippingFeeConfigService.getAllShippingConfigs().stream()
	        	    .filter(fee -> fee.getMaxOrderValue() != null)
	        	    .map(ShippingFeeConfig::getMaxOrderValue)
	        	    .max(BigDecimal::compareTo) // Lấy giá trị lớn nhất
	        	    .orElse(BigDecimal.ZERO); // Nếu không có giá trị nào, trả về 0

	        	model.addAttribute("minFreeShipping", maxOrderValue);


	        model.addAttribute("shippingFees", shippingFees);

	        return "customer/shipping-fee";
	    }
}
