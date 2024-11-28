package com.kimngan.ComesticAdmin.services;

import java.time.LocalDateTime;
import java.util.List;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
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
   
}