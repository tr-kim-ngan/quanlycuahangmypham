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

	HoaDon findByDonHang(DonHang donHang);

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

}