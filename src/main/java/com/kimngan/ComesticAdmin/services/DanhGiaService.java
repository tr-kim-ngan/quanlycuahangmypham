package com.kimngan.ComesticAdmin.services;

import java.util.List;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;

public interface DanhGiaService {
    DanhGia saveDanhGia(DanhGia danhGia);
    List<DanhGia> getDanhGiaBySanPham(SanPham sanPham);
    List<DanhGia> getDanhGiaByNguoiDung(NguoiDung nguoiDung);
    List<DanhGia> findByHoaDonAndNguoiDung(HoaDon hoaDon, NguoiDung nguoiDung); // Thay đổi từ Integer sang đối tượng
    DanhGia create(DanhGia danhGia);
    boolean existsByHoaDonAndNguoiDung(Integer maHoaDon, Integer maNguoiDung);
}
