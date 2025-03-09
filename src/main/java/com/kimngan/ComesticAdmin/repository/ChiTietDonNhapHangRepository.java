package com.kimngan.ComesticAdmin.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHangId;
import com.kimngan.ComesticAdmin.entity.DonNhapHang;
import com.kimngan.ComesticAdmin.entity.SanPham;

public interface ChiTietDonNhapHangRepository extends JpaRepository<ChiTietDonNhapHang, ChiTietDonNhapHangId> {

	Page<ChiTietDonNhapHang> findBySanPham_TenSanPhamContainingIgnoreCase(String tenSanPham, Pageable pageable);

	List<ChiTietDonNhapHang> findByDonNhapHang(DonNhapHang donNhapHang);

	List<ChiTietDonNhapHang> findBySanPham(SanPham sanPham);

	boolean existsBySanPham(SanPham sanPham);

	Page<ChiTietDonNhapHang> findByDonNhapHang(DonNhapHang donNhapHang, Pageable pageable);
	
	
	//  Tìm thời điểm gần nhất kho hết hàng
    @Query("SELECT MAX(dn.ngayNhapHang) FROM DonNhapHang dn " +
           "JOIN ChiTietDonNhapHang ct ON dn.maDonNhapHang = ct.donNhapHang.maDonNhapHang " +
           "WHERE ct.sanPham.maSanPham = :maSanPham AND ct.sanPham.soLuong = 0")
    LocalDate findLastTimeStockEmpty(@Param("maSanPham") Integer maSanPham);

    //  Tổng số lượng nhập của sản phẩm từ lần kho hết hàng gần nhất
    @Query("SELECT COALESCE(SUM(ct.soLuongNhap), 0) FROM ChiTietDonNhapHang ct " +
    	       "JOIN DonNhapHang dn ON ct.donNhapHang.maDonNhapHang = dn.maDonNhapHang " +
    	       "WHERE ct.sanPham.maSanPham = :maSanPham AND dn.ngayNhapHang > :lastStockEmptyTime")
    	Integer getTotalImportedQuantityAfterStockEmpty(@Param("maSanPham") Integer maSanPham,
    	                                                @Param("lastStockEmptyTime") LocalDate lastStockEmptyTime);

    //  Tổng số lượng nhập của sản phẩm từ trước đến nay
    @Query("SELECT SUM(ct.soLuongNhap) FROM ChiTietDonNhapHang ct " +
           "WHERE ct.sanPham.maSanPham = :maSanPham")
    Integer getTotalImportedQuantityBySanPhamId(@Param("maSanPham") Integer maSanPham);

	
}