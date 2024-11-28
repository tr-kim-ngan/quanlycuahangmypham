package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;
import java.util.List;

public interface ChiTietDonHangService {
    ChiTietDonHang createChiTietDonHang(ChiTietDonHang chiTietDonHang); // Thêm mới chi tiết đơn hàng
    ChiTietDonHang updateChiTietDonHang(ChiTietDonHang chiTietDonHang); // Cập nhật chi tiết đơn hàng
    void deleteChiTietDonHang(ChiTietDonHangId id); // Xóa chi tiết đơn hàng
    ChiTietDonHang getChiTietDonHangById(ChiTietDonHangId id); // Lấy thông tin chi tiết đơn hàng theo mã
    List<ChiTietDonHang> getChiTietDonHangsByDonHangId(Integer maDonHang); // Lấy tất cả chi tiết của một đơn hàng
    ChiTietDonHang save(ChiTietDonHang chiTietDonHang);
    Integer getSoldQuantityBySanPhamId(Integer sanPhamId);
    List<Object[]> getTop3BestSellingProducts();
}