package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

//    List<DonHang> getOrdersByStatusAndExportStaff(String trangThai, Integer maNhanVien);
//
//    List<DonHang> getOrdersByStatusesAndExportStaff(List<String> trangThaiList, Integer maNhanVien);
//    
    List<DonHang> getDonHangsByStatus(String status);
    Page<DonHang> findDonHangsDaXuatKho(Pageable pageable);

    int getSoLuongTraHang(Integer maSanPham) ;
    void clearOfflineOrder();
    
    
    void updateOfflineOrderQuantity(Integer sanPhamId, int quantity);

    void saveOfflineOrder(String soDienThoai);

    Page<DonHang> getOrdersByUserAndDiaChi(String username, String diaChi, Pageable pageable);

    Page<DonHang> getOrdersByUserAndDiaChiNot(String username, String diaChi, Pageable pageable);

    List<DonHang> findByTrangThaiAndShipper(NguoiDung shipper, String trangThai);

    int countByShipper(NguoiDung shipper);
    List<DonHang> findBySeller(NguoiDung seller);
    long demDonHangTrongKhoangNgay(LocalDateTime fromDateTime, LocalDateTime toDateTime);
    List<Object[]> getSoDonHangTheoNgay(LocalDate fromDate, LocalDate toDate);

    //Top nhân viên bán hàng có nhiều đơn nhất
    List<Object[]> getTopNhanVienBanHang(LocalDateTime from, LocalDateTime to);

    //Top khách hàng đặt nhiều đơn nhất
    List<Object[]> getTopKhachHang(LocalDateTime from, LocalDateTime to);

    
    //Top sản phẩm được đặt nhiều nhất
    List<Object[]> getTopSanPham(LocalDateTime from, LocalDateTime to);

    List<Map<String, Object>> thongKeDoanhThuVaLoiNhuanTheoNgay(LocalDateTime fromDate, LocalDateTime toDate);
    
    List<Map<String, Object>> thongKeDoanhThuVaLoiNhuanTheoSanPham(LocalDateTime fromDate, LocalDateTime toDate);
    
    List<Object[]> thongKeSanPhamBanChay(LocalDateTime fromDate, LocalDateTime toDate);
    List<Object[]> thongKeSanPhamBanChayChiTiet(LocalDateTime fromDate, LocalDateTime toDate);

    List<Object[]> thongKeDonHangTheoTrangThai();

    List<Object[]> thongKeDoanhThuTheoNhanVien(LocalDateTime from, LocalDateTime to);
    
    List<Object[]> thongKeSanPhamBiTraNhieuNhat();
    List<Object[]> thongKeKhachHangMuaNhieu(LocalDateTime from, LocalDateTime to);
    List<Object[]> thongKeKhachHangHuyDonNhieu(LocalDateTime from, LocalDateTime to);
    List<Object[]> thongKeDonMuaTheoKhach(Integer id);
    Long thongKeDonHuyTheoKhach(Integer maNguoiDung);

    List<DonHang> getDonHangsByStatuses(List<String> trangThaiList);
    
    }