package com.kimngan.ComesticAdmin.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

	Page<HoaDon> findByTenNguoiNhanContainingAndNgayXuatHoaDonBetween(String tenNguoiNhan, LocalDateTime startDate,
			LocalDateTime endDate, Pageable pageable);
	//HoaDon findByDonHang(DonHang donHang);
	@Query("SELECT h FROM HoaDon h WHERE h.donHang = :donHang")
	HoaDon findByDonHang(@Param("donHang") DonHang donHang);

	Page<HoaDon> findByTenNguoiNhanContainingIgnoreCase(String tenNguoiNhan, Pageable pageable);

	Page<HoaDon> findAll(Pageable pageable);

	@Query("SELECT h FROM HoaDon h WHERE h.donHang.nguoiDung.tenNguoiDung = :username")
	List<HoaDon> findByCustomerUsername(@Param("username") String username);

	@Query("SELECT hd FROM HoaDon hd WHERE hd.ngayXuatHoaDon BETWEEN :startDate AND :endDate")
	Page<HoaDon> findByNgayXuatHoaDonBetween(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate, Pageable pageable);

	Page<HoaDon> findByTrangThaiThanhToan(String status, Pageable pageable);

	Page<HoaDon> findByTrangThaiThanhToanAndNgayXuatHoaDonBetween(String trangThaiThanhToan,
			LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

	@Query("SELECT COALESCE(SUM(h.tongTien), 0) FROM HoaDon h WHERE h.trangThaiThanhToan = :status")
	BigDecimal calculateTotalRevenueByStatus(@Param("status") String status);
    long countByTrangThaiThanhToan(String trangThai);
    
    
    
    List<HoaDon> findByTrangThaiThanhToan(String trangThai);
    List<HoaDon> findByTrangThaiThanhToanIn(List<String> trangThai);

    
    @Query("SELECT DATE(h.ngayXuatHoaDon), SUM(h.tongTien) " +
            "FROM HoaDon h WHERE h.trangThaiThanhToan = 'Đã xác nhận' OR h.trangThaiThanhToan = 'Đã hoàn thành' " +
            "GROUP BY DATE(h.ngayXuatHoaDon) ORDER BY DATE(h.ngayXuatHoaDon)")
     List<Object[]> getRevenueByDate();

     @Query("SELECT YEARWEEK(h.ngayXuatHoaDon), SUM(h.tongTien) " +
            "FROM HoaDon h WHERE h.trangThaiThanhToan = 'Đã xác nhận' OR h.trangThaiThanhToan = 'Đã hoàn thành' " +
            "GROUP BY YEARWEEK(h.ngayXuatHoaDon) ORDER BY YEARWEEK(h.ngayXuatHoaDon)")
     List<Object[]> getRevenueByWeek();

     @Query("SELECT YEAR(h.ngayXuatHoaDon), MONTH(h.ngayXuatHoaDon), SUM(h.tongTien) " +
            "FROM HoaDon h WHERE h.trangThaiThanhToan = 'Đã xác nhận' OR h.trangThaiThanhToan = 'Đã hoàn thành' " +
            "GROUP BY YEAR(h.ngayXuatHoaDon), MONTH(h.ngayXuatHoaDon) " +
            "ORDER BY YEAR(h.ngayXuatHoaDon), MONTH(h.ngayXuatHoaDon)")
     List<Object[]> getRevenueByMonth();


}