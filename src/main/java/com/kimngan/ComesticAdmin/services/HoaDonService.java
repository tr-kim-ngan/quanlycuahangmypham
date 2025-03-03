package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.SanPham;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HoaDonService {
    List<HoaDon> getAllHoaDons();
    HoaDon getHoaDonById(Integer id);
    HoaDon saveHoaDon(HoaDon hoaDon);
    HoaDon getHoaDonByDonHang(DonHang donHang);
    Page<HoaDon> getAllHoaDons(Pageable pageable);
    List<HoaDon> getHoaDonsByCustomer(String username);
    HoaDon findById(Integer id);
   // void completeOrder(HoaDon hoaDon);
    Page<HoaDon> searchByTenNguoiNhan(String tenNguoiNhan, Pageable pageable);
    Page<HoaDon> searchByNgayXuat(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<HoaDon> searchByTenNguoiNhanAndNgayXuatHoaDon(String tenNguoiNhan, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) ;
    void xacNhanThanhToan(Integer maHoaDon);
    Page<HoaDon> searchByStatus(String status, Pageable pageable);
    Page<HoaDon> searchByTrangThaiAndNgayXuat(String trangThai, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
    BigDecimal calculateTotalRevenue();
    long countUnconfirmedInvoices();
    List<SanPham> findTopSoldProductsByBrand(Integer maThuongHieu, int limit);
    List<SanPham> findTopSoldProductsByCategory(Integer maDanhMuc, int limit) ;
    int getTotalSoldQuantityByProduct(Integer maSanPham);
    void createHoaDon(DonHang donHang, String phuongThucThanhToan);

}


