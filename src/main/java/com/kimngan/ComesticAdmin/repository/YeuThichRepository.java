package com.kimngan.ComesticAdmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.YeuThich;

public interface YeuThichRepository extends JpaRepository<YeuThich, Integer> {
    List<YeuThich> findByNguoiDungMaNguoiDung(Integer maNguoiDung);
    boolean existsByNguoiDungAndSanPham(NguoiDung nguoiDung, SanPham sanPham);
    void deleteByNguoiDungAndSanPham(NguoiDung nguoiDung, SanPham sanPham);
    YeuThich findByNguoiDungAndSanPham(NguoiDung nguoiDung, SanPham sanPham);
    List<YeuThich> findByNguoiDung(NguoiDung nguoiDung);
    
    List<YeuThich> findByNguoiDungMaNguoiDungAndSanPhamTrangThaiTrue(Integer maNguoiDung);


}
