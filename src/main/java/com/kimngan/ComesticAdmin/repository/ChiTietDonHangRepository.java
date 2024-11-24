package com.kimngan.ComesticAdmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, ChiTietDonHangId> {
    // Nếu cần thêm các query tùy chỉnh, có thể thêm ở đây
	List<ChiTietDonHang> findByDonHangMaDonHang(Integer maDonHang);

}
