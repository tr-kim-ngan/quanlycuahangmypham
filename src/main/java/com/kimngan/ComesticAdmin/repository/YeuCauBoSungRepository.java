package com.kimngan.ComesticAdmin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.YeuCauBoSung;

public interface YeuCauBoSungRepository extends JpaRepository<YeuCauBoSung, Integer> {
	Page<YeuCauBoSung> findBySanPham_MaSanPham(Integer maSanPham, Pageable pageable);

	Page<YeuCauBoSung> findBySanPham_TenSanPhamContainingIgnoreCase(String tenSanPham, Pageable pageable);

}
