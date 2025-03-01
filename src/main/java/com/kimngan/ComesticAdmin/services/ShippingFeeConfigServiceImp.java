package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;
import com.kimngan.ComesticAdmin.repository.ShippingFeeConfigRepository;

@Service
public class ShippingFeeConfigServiceImp implements ShippingFeeConfigService {
	@Autowired
	private ShippingFeeConfigRepository shippingFeeConfigRepository;

	@Override
	public BigDecimal getShippingFeeForOrder(BigDecimal orderValue) {
		ShippingFeeConfig config = shippingFeeConfigRepository.findShippingFeeByOrderValue(orderValue);
		return config != null ? config.getShippingFee() : BigDecimal.ZERO; // Mặc định là 0 nếu không có config
	}

	@Override
	public List<ShippingFeeConfig> getAllShippingConfigs() {
		return shippingFeeConfigRepository.findAll();
	}

	@Override
	public void saveShippingConfig(ShippingFeeConfig config) {
		if (!isValidRange(config)) {
            throw new IllegalArgumentException("Khoảng giá bị trùng hoặc có lỗ hổng! Vui lòng kiểm tra lại.");
        }
		shippingFeeConfigRepository.save(config);
	}

	@Override
	public void deleteShippingConfig(Integer id) {
		shippingFeeConfigRepository.deleteById(id);
	}

	@Override
	public ShippingFeeConfig getShippingConfigById(Integer id) {
		// TODO Auto-generated method stub
		return shippingFeeConfigRepository.findById(id).orElse(null);
	}

	@Override
	public void updateShippingConfig(ShippingFeeConfig config) {
		// TODO Auto-generated method stub
		ShippingFeeConfig existingConfig = shippingFeeConfigRepository.findById(config.getId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình phí vận chuyển"));
		existingConfig.setMinOrderValue(config.getMinOrderValue());
		existingConfig.setMaxOrderValue(config.getMaxOrderValue());
		existingConfig.setShippingFee(config.getShippingFee());
		shippingFeeConfigRepository.save(existingConfig);
	}

	@Override
	public boolean isValidRange(ShippingFeeConfig newConfig) {
		// TODO Auto-generated method stub
		List<ShippingFeeConfig> existingConfigs = shippingFeeConfigRepository.findAll();

		for (ShippingFeeConfig config : existingConfigs) {
			if (!config.getId().equals(newConfig.getId())) { // Bỏ qua nếu đang cập nhật chính nó
				// Kiểm tra xem có khoảng giá nào bị chồng lấn không
				boolean isOverlap = !(newConfig.getMaxOrderValue().compareTo(config.getMinOrderValue()) < 0
						|| newConfig.getMinOrderValue().compareTo(config.getMaxOrderValue()) > 0);
				if (isOverlap)
					return false;
			}
		}
		return true;
	}

}
