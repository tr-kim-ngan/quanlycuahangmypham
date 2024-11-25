package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.LoaiKhachHang;


import java.util.List;

public interface LoaiKhachHangService {
    
    List<LoaiKhachHang> getAllLoaiKhachHang();
    LoaiKhachHang getLoaiKhachHangById(Integer id);

    LoaiKhachHang saveLoaiKhachHang(LoaiKhachHang loaiKhachHang);

    void deleteLoaiKhachHang(Integer id);

    void initializeDefaultLoaiKhachHang(); // Khởi tạo loại khách hàng mặc định
}

