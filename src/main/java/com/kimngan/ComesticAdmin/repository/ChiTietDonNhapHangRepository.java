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

	// Tìm thời điểm gần nhất kho hết hàng
	@Query("SELECT MAX(dn.ngayNhapHang) FROM DonNhapHang dn "
			+ "JOIN ChiTietDonNhapHang ct ON dn.maDonNhapHang = ct.donNhapHang.maDonNhapHang "
			+ "WHERE ct.sanPham.maSanPham = :maSanPham AND ct.sanPham.soLuong = 0")
	LocalDate findLastTimeStockEmpty(@Param("maSanPham") Integer maSanPham);

	// Tổng số lượng nhập của sản phẩm từ lần kho hết hàng gần nhất
	@Query("SELECT COALESCE(SUM(ct.soLuongNhap), 0) FROM ChiTietDonNhapHang ct "
			+ "JOIN DonNhapHang dn ON ct.donNhapHang.maDonNhapHang = dn.maDonNhapHang "
			+ "WHERE ct.sanPham.maSanPham = :maSanPham AND dn.ngayNhapHang > :lastStockEmptyTime")
	Integer getTotalImportedQuantityAfterStockEmpty(@Param("maSanPham") Integer maSanPham,
			@Param("lastStockEmptyTime") LocalDate lastStockEmptyTime);

	// Tổng số lượng nhập của sản phẩm từ trước đến nay
	@Query("SELECT SUM(ct.soLuongNhap) FROM ChiTietDonNhapHang ct " + "WHERE ct.sanPham.maSanPham = :maSanPham")
	Integer getTotalImportedQuantityBySanPhamId(@Param("maSanPham") Integer maSanPham);

// vẽ sơ đồ
	@Query("SELECT sp.tenSanPham, SUM(ct.soLuongNhap) FROM ChiTietDonNhapHang ct " + "JOIN ct.sanPham sp "
			+ "JOIN ct.donNhapHang dnh " + "WHERE dnh.ngayNhapHang BETWEEN :fromDate AND :toDate "
			+ "GROUP BY sp.tenSanPham")
	List<Object[]> findImportStatistics(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("SELECT c.sanPham.tenSanPham, c.soLuongNhap, d.ngayNhapHang, d.nhaCungCap.tenNhaCungCap "
			+ "FROM ChiTietDonNhapHang c JOIN c.donNhapHang d " + "WHERE d.ngayNhapHang BETWEEN :fromDate AND :toDate")
	List<Object[]> getBaoCaoChiTiet(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("SELECT d.nhaCungCap.tenNhaCungCap, SUM(c.soLuongNhap) " + "FROM ChiTietDonNhapHang c "
			+ "JOIN c.donNhapHang d " + "WHERE d.ngayNhapHang BETWEEN :fromDate AND :toDate "
			+ "GROUP BY d.nhaCungCap.tenNhaCungCap " + "ORDER BY SUM(c.soLuongNhap) DESC")
	List<Object[]> findTopSuppliers(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("SELECT c.donNhapHang.nhaCungCap.tenNhaCungCap, SUM(c.donGiaNhap * c.soLuongNhap) "
			+ "FROM ChiTietDonNhapHang c " + "WHERE c.donNhapHang.ngayNhapHang BETWEEN :fromDate AND :toDate "
			+ "GROUP BY c.donNhapHang.nhaCungCap.tenNhaCungCap " + "ORDER BY SUM(c.donGiaNhap * c.soLuongNhap) DESC")
	List<Object[]> getTotalImportValue(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("SELECT c.sanPham.tenSanPham, SUM(c.soLuongNhap), SUM(c.donGiaNhap * c.soLuongNhap), d.ngayNhapHang, d.nhaCungCap.tenNhaCungCap "
			+ "FROM ChiTietDonNhapHang c " + "JOIN c.donNhapHang d "
			+ "WHERE d.ngayNhapHang BETWEEN :fromDate AND :toDate "
			+ "GROUP BY c.sanPham.tenSanPham, d.ngayNhapHang, d.nhaCungCap.tenNhaCungCap "
			+ "ORDER BY d.ngayNhapHang DESC")
	List<Object[]> findTotalImportReport(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	@Query("SELECT d.ngayNhapHang, SUM(c.soLuongNhap) " + "FROM ChiTietDonNhapHang c " + "JOIN c.donNhapHang d "
			+ "WHERE d.ngayNhapHang BETWEEN :fromDate AND :toDate " + "GROUP BY d.ngayNhapHang "
			+ "ORDER BY d.ngayNhapHang ASC")
	List<Object[]> getImportTrend(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

}