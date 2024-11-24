package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.SanPham;

import java.util.List;
import java.util.Optional;

public interface ChiTietGioHangService {
    void updateQuantity(GioHang gioHang, SanPham sanPham, int soLuong);  // Cập nhật số lượng sản phẩm
    ChiTietGioHang findByGioHangAndSanPham(GioHang gioHang, SanPham sanPham);  // Tìm chi tiết sản phẩm trong giỏ hàng
    void delete(ChiTietGioHang chiTietGioHang);  // Xóa chi tiết sản phẩm khỏi giỏ hàng
    void addOrUpdateChiTietGioHang(GioHang gioHang, SanPham sanPham, int soLuong);


}
