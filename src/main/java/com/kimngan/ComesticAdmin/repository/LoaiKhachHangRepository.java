package com.kimngan.ComesticAdmin.repository;


import com.kimngan.ComesticAdmin.entity.LoaiKhachHang;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoaiKhachHangRepository extends JpaRepository<LoaiKhachHang, Integer> {

    // Tìm tất cả loại khách hàng với trạng thái là true (active)
    Page<LoaiKhachHang> findByTrangThaiTrue(Pageable pageable);
    Optional<LoaiKhachHang> findByTenLoaiKH(String tenLoaiKH);

}
