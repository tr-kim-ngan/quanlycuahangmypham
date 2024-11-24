package com.kimngan.ComesticAdmin.repository;

import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
	// Tìm giỏ hàng theo người dùng
    Optional<GioHang> findByNguoiDung(NguoiDung nguoiDung);
    Optional<GioHang> findByNguoiDungTenNguoiDung(String tenNguoiDung);

}
