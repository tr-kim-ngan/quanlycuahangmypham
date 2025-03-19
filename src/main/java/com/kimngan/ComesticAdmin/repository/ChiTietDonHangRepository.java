package com.kimngan.ComesticAdmin.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;
import com.kimngan.ComesticAdmin.entity.SanPham;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, ChiTietDonHangId> {
	// Nếu cần thêm các query tùy chỉnh, có thể thêm ở đây
	List<ChiTietDonHang> findByDonHangMaDonHang(Integer maDonHang);

	// @Query("SELECT c FROM ChiTietDonHang c WHERE c.sanPham.maSanPham = :sanPhamId
	// AND c.donHang.trangThai = :trangThai")
	List<ChiTietDonHang> findBySanPhamMaSanPhamAndDonHangTrangThaiDonHangIn(Integer sanPhamId,
			List<String> trangThaiDonHang);

	@Query("SELECT c.sanPham.maSanPham, SUM(c.soLuong) AS totalQuantity " + "FROM ChiTietDonHang c "
			+ "JOIN c.donHang d " + "JOIN HoaDon h ON h.donHang = d "
			+ "WHERE d.trangThaiDonHang = 'Đã hoàn thành' AND h.trangThaiThanhToan = 'Đã xác nhận' "
			+ "GROUP BY c.sanPham.maSanPham " + "ORDER BY totalQuantity DESC")
	List<Object[]> findTop3BestSellingProducts();

	@Query("SELECT ctdh.sanPham, SUM(ctdh.soLuong) as totalQuantity " + "FROM ChiTietDonHang ctdh "
			+ "JOIN ctdh.donHang dh " + "WHERE ctdh.sanPham.thuongHieu.maThuongHieu = :maThuongHieu "
			+ "AND dh.trangThaiDonHang = 'Đã xác nhận' " + "GROUP BY ctdh.sanPham " + "ORDER BY totalQuantity DESC")
	List<SanPham> findTopSoldProductsByBrand(@Param("maThuongHieu") Integer maThuongHieu, Pageable pageable);

	@Query("SELECT c FROM ChiTietDonHang c JOIN FETCH c.sanPham s LEFT JOIN FETCH s.khuyenMais WHERE c.donHang IS NULL")
	List<ChiTietDonHang> findOfflineOrderWithKhuyenMai();

	@Query("SELECT SUM(ctdh.soLuong) FROM ChiTietDonHang ctdh " + "JOIN ctdh.donHang dh "
			+ "JOIN HoaDon hd ON hd.donHang = dh " + "WHERE ctdh.sanPham.maSanPham = :sanPhamId "
			+ "AND hd.trangThaiThanhToan = 'Đã hoàn thành'")
	Integer getSoldQuantityFromOfflineOrders(@Param("sanPhamId") Integer sanPhamId);

	@Query("SELECT SUM(ctdh.soLuong) FROM ChiTietDonHang ctdh " + "JOIN ctdh.donHang dh "
			+ "JOIN HoaDon hd ON hd.donHang = dh " + "WHERE ctdh.sanPham.maSanPham = :sanPhamId "
			+ "AND hd.trangThaiThanhToan = 'Đã xác nhận'")
	Integer getSoldQuantityFromCompletedInvoices(@Param("sanPhamId") Integer sanPhamId);

	@Query("SELECT COALESCE(SUM(ctdh.soLuong), 0) FROM ChiTietDonHang ctdh "
			+ "WHERE ctdh.sanPham.maSanPham = :maSanPham")
	Integer getTotalQuantityBySanPhamId(@Param("maSanPham") Integer maSanPham);

	@Query("SELECT c.sanPham.tenSanPham, SUM(c.soLuong) " + "FROM ChiTietDonHang c "
			+ "WHERE c.donHang.ngayDat BETWEEN :fromDate AND :toDate " + "GROUP BY c.sanPham.tenSanPham "
			+ "ORDER BY SUM(c.soLuong) DESC")
	List<Object[]> getExportStatistics(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

	@Query("SELECT c.sanPham.tenSanPham, SUM(c.soLuong) " + "FROM ChiTietDonHang c "
			+ "WHERE c.donHang.ngayDat BETWEEN :fromDate AND :toDate " + "GROUP BY c.sanPham.tenSanPham "
			+ "ORDER BY SUM(c.soLuong) DESC")
	List<Object[]> getTopExportedProducts(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

	@Query("SELECT dh.sanPham.tenSanPham, SUM(dh.soLuong), dh.donHang.ngayDat, dh.donHang.nguoiDung.tenNguoiDung "
			+ "FROM ChiTietDonHang dh " + "WHERE dh.donHang.ngayDat BETWEEN :fromDate AND :toDate "
			+ "GROUP BY dh.sanPham.tenSanPham, dh.donHang.ngayDat, dh.donHang.nguoiDung.tenNguoiDung "
			+ "ORDER BY dh.donHang.ngayDat DESC")
	List<Object[]> getBaoCaoXuatKhoChiTiet(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

	@Query("SELECT dh.donHang.nguoiDung.tenNguoiDung, COUNT(dh.donHang) " + "FROM ChiTietDonHang dh "
			+ "WHERE dh.donHang.ngayDat BETWEEN :fromDate AND :toDate " + "GROUP BY dh.donHang.nguoiDung.tenNguoiDung "
			+ "ORDER BY COUNT(dh.donHang) DESC")
	List<Object[]> getTopCustomers(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);


	
}
