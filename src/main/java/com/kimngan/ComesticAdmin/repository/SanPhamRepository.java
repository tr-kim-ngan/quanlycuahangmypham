package com.kimngan.ComesticAdmin.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kimngan.ComesticAdmin.entity.SanPham;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
	// Tìm sản phẩm theo tên
	Page<SanPham> findByTenSanPhamContainingIgnoreCaseAndTrangThai(String tenSanPham, Boolean trangThai,
			Pageable pageable);

	// Kiểm tra xem sản phẩm có tồn tại và đang hoạt động không
	boolean existsByTenSanPhamAndTrangThai(String tenSanPham, boolean b);
	@Query("SELECT sp FROM SanPham sp "
		     + "LEFT JOIN FETCH sp.danhMuc "
		     + "LEFT JOIN FETCH sp.thuongHieu "
		     + "LEFT JOIN FETCH sp.khuyenMais "
		     + "LEFT JOIN FETCH sp.donViTinh "
		     + "WHERE sp.trangThai = true")
		List<SanPham> findAllActiveProductsWithAllInfo();
	List<SanPham> findByTenSanPhamContainingIgnoreCaseAndTrangThai(String tenSanPham, Boolean trangThai);

	// Tìm tất cả sản phẩm với trạng thái đang hoạt động
	Page<SanPham> findByTrangThaiTrue(Pageable pageable);

	// Tìm tất cả sản phẩm có trạng thái active với phân trang
	// Page<SanPham> findByTrangThaiTrue(Pageable pageable);
	Page<SanPham> findByTrangThai(Boolean trangThai, Pageable pageable);

	@Query("SELECT sp FROM SanPham sp WHERE sp.trangThai = true AND CAST(sp.maSanPham AS string) LIKE %:maSanPham%")
	Page<SanPham> searchActiveByMaSanPham(@Param("maSanPham") String maSanPham, Pageable pageable);

	List<SanPham> findByTrangThai(Boolean trangThai);

	List<SanPham> findByMaSanPhamInAndTrangThai(List<Integer> maSanPham, Boolean trangThai);

	Page<SanPham> findByDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai, Pageable pageable);

	List<SanPham> findByTrangThaiTrue();

	@Query("SELECT sp FROM SanPham sp " + "LEFT JOIN FETCH sp.danhMuc " + "LEFT JOIN FETCH sp.thuongHieu "
			+ "LEFT JOIN FETCH sp.khuyenMais " + "LEFT JOIN FETCH sp.donViTinh " + "WHERE sp.trangThai = true")
	List<SanPham> findAllActiveProducts();

	Page<SanPham> findByThuongHieu_MaThuongHieuAndTrangThai(Integer maThuongHieu, boolean trangThai, Pageable pageable);

	// Tìm sản phẩm hoạt động, có hàng trong kho và có trong đơn nhập hàng
	@Query("SELECT DISTINCT sp FROM SanPham sp JOIN sp.chiTietDonNhapHangs ctdnh WHERE sp.trangThai = true AND ctdnh.soLuongNhap > 0 AND ctdnh.trangThai = true")

	Page<SanPham> findActiveProductsInOrderDetails(Pageable pageable);

	// Lấy sản phẩm active theo danh mục, có trong chi tiết đơn nhập hàng với số
	// lượng nhập > 0

	@Query("SELECT DISTINCT sp FROM SanPham sp " + "JOIN sp.chiTietDonNhapHangs ctdnh " + "JOIN ctdnh.donNhapHang dnh "
			+ "WHERE sp.trangThai = true " + "AND sp.danhMuc.maDanhMuc =  ?1 " + "AND ctdnh.soLuongNhap > 0 " + // Đảm
																												// bảo
																												// có số
																												// lượng
																												// nhập
			"AND ctdnh.donGiaNhap > 0 " + "AND ctdnh.trangThai = true " + // Đảm bảo chi tiết đơn nhập hàng có trạng
																			// thái true
			"AND dnh.tongGiaTriNhapHang > 0 ") // Lọc theo danh mục
	Page<SanPham> findActiveProductsInOrderDetailsByCategory(Integer maDanhMuc, Pageable pageable);

	List<SanPham> findByDanhMuc_MaDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai);

	Page<SanPham> findByDanhMuc_MaDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai, Pageable pageable);

	Page<SanPham> findByDanhMuc_MaDanhMucAndTenSanPhamContainingAndTrangThai(Integer maDanhMuc, String keyword,
			Boolean trangThai, Pageable pageable);

	List<SanPham> findByTenSanPhamContaining(String keyword, Pageable pageable);

	List<SanPham> findByDanhMuc_MaDanhMucAndTenSanPhamContaining(Integer categoryId, String keyword, Pageable pageable);

	// Tìm kiếm sản phẩm theo danh mục và trạng thái active với từ khóa
	@Query("SELECT sp FROM SanPham sp WHERE sp.danhMuc.maDanhMuc = :categoryId AND sp.trangThai = true AND sp.tenSanPham LIKE %:keyword%")
	List<SanPham> searchActiveProductsByCategoryAndKeyword(@Param("categoryId") Integer categoryId,
			@Param("keyword") String keyword, Pageable pageable);

	// Tìm kiếm tất cả sản phẩm theo từ khóa, có trạng thái và có chi tiết đơn nhập
	// hàng
	@Query("SELECT sp FROM SanPham sp JOIN sp.chiTietDonNhapHangs ctdh WHERE sp.trangThai = true AND ctdh.soLuongNhap > 0 AND sp.tenSanPham LIKE %:keyword%")
	Page<SanPham> searchAllActiveProductsWithOrderDetails(@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT sp FROM SanPham sp JOIN sp.chiTietDonNhapHangs ctdh WHERE sp.trangThai = true AND sp.danhMuc.maDanhMuc = :categoryId AND ctdh.soLuongNhap > 0 AND (:keyword IS NULL OR :keyword = '' OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<SanPham> searchByCategoryWithOrderDetails(@Param("categoryId") Integer categoryId,
			@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT DISTINCT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias")
	List<SanPham> findAllWithDanhGias();

	@Query("SELECT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias WHERE sp.trangThai = true")
	List<SanPham> findAllWithDanhGiasAndTrangThaiTrue();

	@Query("SELECT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias dg WHERE sp.trangThai = true AND dg.soSao = :soSao")
	List<SanPham> findAllWithDanhGiasAndTrangThaiTrueBySoSao(@Param("soSao") int soSao);

	@Query(value = "SELECT DISTINCT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias dg WHERE sp.trangThai = true", countQuery = "SELECT COUNT(sp) FROM SanPham sp WHERE sp.trangThai = true")
	Page<SanPham> findAllWithDanhGiasAndTrangThaiTrue(Pageable pageable);

	@Query(value = "SELECT DISTINCT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias dg WHERE sp.trangThai = true AND dg.soSao = :soSao", countQuery = "SELECT COUNT(DISTINCT sp) FROM SanPham sp LEFT JOIN sp.danhGias dg WHERE sp.trangThai = true AND dg.soSao = :soSao")
	Page<SanPham> findAllWithDanhGiasAndTrangThaiTrueBySoSao(@Param("soSao") int soSao, Pageable pageable);

	long countByTrangThaiTrue();

	boolean existsByDonViTinhMaDonVi(Integer maDonVi);

	@Query("SELECT s FROM SanPham s WHERE s.trangThai = true AND s.donGiaBan BETWEEN :minPrice AND :maxPrice")
	Page<SanPham> findAllActiveByPriceRange(@Param("minPrice") BigDecimal minPrice,
			@Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

	@Query("SELECT s FROM SanPham s WHERE s.trangThai = true AND s.danhMuc.maDanhMuc = :maDanhMuc AND s.donGiaBan BETWEEN :minPrice AND :maxPrice")
	Page<SanPham> findActiveProductsByCategoryAndPrice(@Param("maDanhMuc") Integer maDanhMuc,
			@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

	List<SanPham> findByTenSanPhamContainingIgnoreCase(String tenSanPham);

	List<SanPham> findByMaSanPhamIn(List<Integer> maSanPhams);

	@Query("SELECT sp FROM SanPham sp LEFT JOIN FETCH sp.khuyenMais WHERE sp.maSanPham IN :productIds")
	List<SanPham> findByIdInWithKhuyenMai(@Param("productIds") List<Integer> productIds);

	@Query("SELECT sp FROM SanPham sp WHERE sp.trangThai = true AND sp.soLuongTonKho > 0")
	Page<SanPham> findAllActiveWithStock(Pageable pageable);

	@Query("SELECT s.tenSanPham, "
			+ "(COALESCE((SELECT SUM(nh.soLuongNhap) FROM ChiTietDonNhapHang nh WHERE nh.sanPham = s), 0) - "
			+ " COALESCE((SELECT SUM(ch.soLuong) FROM ChiTietDonHang ch WHERE ch.sanPham = s), 0) - s.soLuong) AS tonKho "
			+ "FROM SanPham s "
			+ "WHERE (COALESCE((SELECT SUM(nh.soLuongNhap) FROM ChiTietDonNhapHang nh WHERE nh.sanPham = s), 0) - "
			+ " COALESCE((SELECT SUM(ch.soLuong) FROM ChiTietDonHang ch WHERE ch.sanPham = s), 0) - s.soLuong) > 0 "
			+ "ORDER BY tonKho DESC")
	List<Object[]> getStockStatistics();

	// Tổng số lượng nhập kho
	@Query("SELECT COALESCE(SUM(nh.soLuongNhap), 0) FROM ChiTietDonNhapHang nh WHERE nh.sanPham.maSanPham = :maSanPham")
	Integer getTotalImportedQuantityBySanPhamId(@Param("maSanPham") Integer maSanPham);

	// Tổng số lượng bán ra
	@Query("SELECT COALESCE(SUM(ch.soLuong), 0) FROM ChiTietDonHang ch WHERE ch.sanPham.maSanPham = :maSanPham")
	Integer getTotalSoldQuantityBySanPhamId(@Param("maSanPham") Integer maSanPham);

	@Query("SELECT COALESCE(SUM(s.soLuong), 0) FROM SanPham s WHERE s.maSanPham = :maSanPham")
	Integer getSoLuongTrenKe(@Param("maSanPham") Integer maSanPham);

	List<SanPham> findByTenSanPhamContainingIgnoreCaseOrMoTaContainingIgnoreCaseAndTrangThai(
		    String tenSanPham, String moTa, Boolean trangThai);

	@Query("SELECT sp FROM SanPham sp WHERE sp.trangThai = :trangThai AND " +
		       "(LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword1, '%')) OR " +
		       "LOWER(sp.moTa) LIKE LOWER(CONCAT('%', :keyword2, '%')))")
		List<SanPham> searchByKeyword(@Param("keyword1") String keyword1,
		                               @Param("keyword2") String keyword2,
		                               @Param("trangThai") boolean trangThai);
	@Query("SELECT sp FROM SanPham sp WHERE sp.trangThai = true")
	List<SanPham> findAllWithTenMoTaTrangThaiTrue();
	
	
	@Query("SELECT DISTINCT sp FROM SanPham sp " +
		       "JOIN sp.khuyenMais km " +
		       "WHERE sp.trangThai = true " +
		       "AND km.trangThai = true " +
		       "AND CURRENT_DATE < km.ngayBatDau")
		List<SanPham> findAllActiveWithUpcomingPromotions();

	
	List<SanPham> findByThuongHieu_MaThuongHieuAndTrangThai(Integer maThuongHieu, Boolean trangThai);

	
	
	
	
	
	
	
}
