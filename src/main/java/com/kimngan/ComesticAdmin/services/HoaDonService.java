package com.kimngan.ComesticAdmin.services;

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
   
}