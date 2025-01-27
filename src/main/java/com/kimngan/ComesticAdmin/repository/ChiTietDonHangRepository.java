package com.kimngan.ComesticAdmin.repository;

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

//	@Query("SELECT ctdh.sanPham, SUM(ctdh.soLuong) as totalQuantity " + "FROM ChiTietDonHang ctdh "
//			+ "JOIN ctdh.donHang dh " + "WHERE ctdh.sanPham.thuongHieu.maThuongHieu = :maThuongHieu "
//			+ "AND dh.trangThaiDonHang = 'Đã xác nhận' " + "GROUP BY ctdh.sanPham " + "ORDER BY totalQuantity DESC")
//	List<SanPham> findTopSoldProductsByBrand(@Param("maThuongHieu") Integer maThuongHieu, Pageable pageable);
//
	@Query("SELECT ctdh.sanPham, SUM(ctdh.soLuong) as totalQuantity " +
		       "FROM ChiTietDonHang ctdh " +
		       "JOIN ctdh.donHang dh " +
		       "WHERE ctdh.sanPham.thuongHieu.maThuongHieu = :maThuongHieu " +
		       "AND dh.trangThaiDonHang = 'Đã xác nhận' " +
		       "GROUP BY ctdh.sanPham " +
		       "ORDER BY totalQuantity DESC")
		List<SanPham> findTopSoldProductsByBrand(@Param("maThuongHieu") Integer maThuongHieu, Pageable pageable);

	

}
