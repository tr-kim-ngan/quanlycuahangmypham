package com.kimngan.ComesticAdmin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHangId;
import com.kimngan.ComesticAdmin.entity.DonNhapHang;
import com.kimngan.ComesticAdmin.entity.SanPham;

public interface ChiTietDonNhapHangRepository extends JpaRepository<ChiTietDonNhapHang, ChiTietDonNhapHangId> {

	Page<ChiTietDonNhapHang> findBySanPham_TenSanPhamContainingIgnoreCase(String tenSanPham, Pageable pageable);

	List<ChiTietDonNhapHang> findByDonNhapHang(DonNhapHang donNhapHang);

	List<ChiTietDonNhapHang> findBySanPham(SanPham sanPham);

	boolean existsBySanPham(SanPham sanPham);

	Page<ChiTietDonNhapHang> findByDonNhapHang(DonNhapHang donNhapHang, Pageable pageable);

}