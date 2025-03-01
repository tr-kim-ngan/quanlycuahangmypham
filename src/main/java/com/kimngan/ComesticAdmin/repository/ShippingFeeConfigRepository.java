package com.kimngan.ComesticAdmin.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kimngan.ComesticAdmin.entity.ShippingFeeConfig;

@Repository
public interface ShippingFeeConfigRepository extends JpaRepository<ShippingFeeConfig, Integer> {
    @Query("SELECT s FROM ShippingFeeConfig s WHERE :orderValue >= s.minOrderValue AND (:orderValue < s.maxOrderValue OR s.maxOrderValue IS NULL)")
    ShippingFeeConfig findShippingFeeByOrderValue(@Param("orderValue") BigDecimal orderValue);
}

