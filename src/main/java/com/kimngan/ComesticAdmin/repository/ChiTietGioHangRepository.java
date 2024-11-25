package com.kimngan.ComesticAdmin.repository;

import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHangId;
import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, ChiTietGioHangId> {
    List<ChiTietGioHang> findByGioHang(GioHang gioHang);
    Optional<ChiTietGioHang> findByGioHangAndSanPham(GioHang gioHang, SanPham sanPham);
    // Thêm phương thức xóa tất cả các ChiTietGioHang của một giỏ hàng cụ thể
    void deleteByGioHang(GioHang gioHang);
    
    

    
}
