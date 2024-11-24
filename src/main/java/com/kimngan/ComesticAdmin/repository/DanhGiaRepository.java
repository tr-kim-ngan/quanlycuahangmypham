package com.kimngan.ComesticAdmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;


import java.util.List;
import java.util.Optional;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
    List<DanhGia> findBySanPham(SanPham sanPham);
    List<DanhGia> findByNguoiDung(NguoiDung nguoiDung);
	List<DanhGia> findByHoaDonAndNguoiDung(HoaDon  maHoaDon, NguoiDung  maNguoiDung);
	// Thay đổi từ Integer sang các đối tượng liên quan
    boolean existsByHoaDonAndNguoiDung(HoaDon hoaDon, NguoiDung nguoiDung);
    Optional<DanhGia> findByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung);
    boolean existsByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung);

}
