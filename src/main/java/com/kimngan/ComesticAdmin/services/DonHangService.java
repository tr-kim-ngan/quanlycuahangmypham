package com.kimngan.ComesticAdmin.services;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.kimngan.ComesticAdmin.entity.DonHang;

public interface DonHangService {
    DonHang createDonHang(DonHang donHang); // Thêm mới đơn hàng
    DonHang updateDonHang(DonHang donHang); // Cập nhật đơn hàng
    void deleteDonHang(Integer maDonHang); // Xóa đơn hàng
    DonHang getDonHangById(Integer maDonHang); // Lấy thông tin đơn hàng theo mã
    List<DonHang> getAllDonHangs(); // Lấy tất cả đơn hàng
    List<DonHang> getOrdersByUser(String username);

    DonHang createOrderFromCart(String username, String address, String phone);
    DonHang save(DonHang donHang);
    DonHang createTemporaryOrder(String username, List<Integer> productIds);
    void confirmOrder(Integer maDonHang, String address, String phone);
    Page<DonHang> getAllDonHangs(Pageable pageable);
    void completeOrder(DonHang donHang);
    List<DonHang> getAllOrdersSortedByNgayDat();
    //void updateOrderStatus(Integer maDonHang, String trangThaiMoi) ;
    
}