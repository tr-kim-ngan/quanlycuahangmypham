package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.util.Comparator;
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
	public void saveShippingConfig(ShippingFeeConfig newConfig) {
		List<ShippingFeeConfig> existingConfigs = getAllShippingConfigs();

		if (existingConfigs.size() >= 2) {
			throw new IllegalArgumentException("Không thể thêm nhiều hơn 2 khoảng giá.");
		}

		if (existingConfigs.size() == 1) {
			BigDecimal previousMax = existingConfigs.get(0).getMaxOrderValue();
			if (!newConfig.getMinOrderValue().equals(previousMax)) {
				throw new IllegalArgumentException("Khoảng giá không nối tiếp nhau.");
			}
		}

		shippingFeeConfigRepository.save(newConfig);
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
		if (config.getId() == null) {
			throw new IllegalArgumentException("ID không được null khi cập nhật!");
		}

		System.out.println("Cập nhật DB: ID = " + config.getId());

		ShippingFeeConfig existingConfig = shippingFeeConfigRepository.findById(config.getId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình phí vận chuyển"));

		System.out.println(
				"Trước khi cập nhật: " + existingConfig.getMinOrderValue() + " - " + existingConfig.getMaxOrderValue());

		existingConfig.setMinOrderValue(config.getMinOrderValue());
		existingConfig.setMaxOrderValue(config.getMaxOrderValue());
		existingConfig.setShippingFee(config.getShippingFee());

		shippingFeeConfigRepository.save(existingConfig);

		System.out.println(
				"Sau khi cập nhật: " + existingConfig.getMinOrderValue() + " - " + existingConfig.getMaxOrderValue());
	}

}
