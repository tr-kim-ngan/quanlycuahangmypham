package com.kimngan.ComesticAdmin.entity;

import java.math.BigDecimal;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Table;

@Entity
@Table(name = "ShippingFeeConfig")
public class ShippingFeeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "min_order_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal minOrderValue;

    @Column(name = "max_order_value", precision = 15, scale = 2, nullable = true) // Null nếu là mức cuối
    private BigDecimal maxOrderValue;

    @Column(name = "shipping_fee", precision = 15, scale = 2, nullable = false)
    private BigDecimal shippingFee;

    public ShippingFeeConfig() {
    }

   

	public ShippingFeeConfig(Integer id, BigDecimal minOrderValue, BigDecimal maxOrderValue, BigDecimal shippingFee) {
		super();
		this.id = id;
		this.minOrderValue = minOrderValue;
		this.maxOrderValue = maxOrderValue;
		this.shippingFee = shippingFee;
	}



	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BigDecimal getMinOrderValue() {
		return minOrderValue;
	}

	public void setMinOrderValue(BigDecimal minOrderValue) {
		this.minOrderValue = minOrderValue;
	}

	public BigDecimal getMaxOrderValue() {
		return maxOrderValue;
	}

	public void setMaxOrderValue(BigDecimal maxOrderValue) {
		this.maxOrderValue = maxOrderValue;
	}

	public BigDecimal getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}


}
