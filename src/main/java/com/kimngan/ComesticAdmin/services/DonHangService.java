package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;

public interface DonHangService {
    DonHang createDonHang(DonHang donHang); // Thêm mới đơn hàng
    DonHang updateDonHang(DonHang donHang); // Cập nhật đơn hàng
    void deleteDonHang(Integer maDonHang); // Xóa đơn hàng
    DonHang getDonHangById(Integer maDonHang); // Lấy thông tin đơn hàng theo mã
    List<DonHang> getAllDonHangs(); // Lấy tất cả đơn hàng
   // List<DonHang> getOrdersByUser(String username);
    Page<DonHang> getOrdersByUser(String username, Pageable pageable);
    DonHang createOrderFromCart(String username, String address, String phone);
    DonHang save(DonHang donHang);
    DonHang createTemporaryOrder(String username, List<Integer> productIds);
    void confirmOrder(Integer maDonHang, String address, String phone);
    Page<DonHang> getAllDonHangs(Pageable pageable);
    void completeOrder(DonHang donHang);
    List<DonHang> getAllOrdersSortedByNgayDat();
    //void updateOrderStatus(Integer maDonHang, String trangThaiMoi) ;
    Page<DonHang> getDonHangsByStatus(String status, Pageable pageable);
    long countOrders();
    long countByTrangThaiDonHang(String trangThaiDonHang);
    Page<DonHang> getOrdersByUserAndStatus(String username, String status, Pageable pageable);
    Page<DonHang> getLatestOrdersByUser(String username, Pageable pageable);
    List<DonHang> findOrdersByShipper(NguoiDung shipper);
  //  List<DonHang> findOrdersByShipper(NguoiDung shipper, String trangThai);
    List<DonHang> findOrdersByShipperAndStatus(NguoiDung shipper, String status);
    List<String> getDisplayedStatuses(DonHang donHang);
    void capNhatTrangThai(DonHang donHang, String trangThaiMoi);
   // void capNhatTrangThai(DonHang donHang, String trangThaiMoi, boolean isFromShipper);
    void updateOrderStatus(Integer maDonHang, String trangThaiMoi);
    void updatePaymentStatus(Integer maDonHang, String trangThaiThanhToan);
    void addOrderStatusHistory(Integer maDonHang, String trangThaiMoi);
   
    void updateDonHangVNPay(Integer maDonHang);
    List<ChiTietDonHang> getCurrentOfflineOrder();
    void addToOfflineOrder(SanPham sanPham, int quantity);
    void removeFromOfflineOrder(Integer sanPhamId);
    BigDecimal calculateTotalPrice();
    boolean confirmOfflineOrder();
    
    
    void processOfflineOrder(List<Integer> productIds, List<Integer> quantities);
    List<ChiTietDonHang> getOfflineOrderItems();
    Collection<ChiTietDonHang> getOfflineOrder();
    boolean processAndGenerateInvoiceForOfflineOrder(String soDienThoaiKhach);

    
    }