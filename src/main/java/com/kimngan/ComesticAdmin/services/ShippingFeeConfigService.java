package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.util.List;

import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;

public interface ShippingFeeConfigService {
	BigDecimal getShippingFeeForOrder(BigDecimal orderValue);
	List<ShippingFeeConfig> getAllShippingConfigs();
	void saveShippingConfig(ShippingFeeConfig config);
	void deleteShippingConfig(Integer id);
	
	 ShippingFeeConfig getShippingConfigById(Integer id) ;
	
	 void updateShippingConfig(ShippingFeeConfig config) ;
	 boolean isValidRange(ShippingFeeConfig newConfig);
	
}
