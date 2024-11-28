package com.kimngan.ComesticAdmin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kimngan.ComesticAdmin.entity.SanPham;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
	// Tìm sản phẩm theo tên
	Page<SanPham> findByTenSanPhamContainingIgnoreCaseAndTrangThai(String tenSanPham, Boolean trangThai, Pageable pageable);

	// Kiểm tra xem sản phẩm có tồn tại và đang hoạt động không
	boolean existsByTenSanPhamAndTrangThai(String tenSanPham, boolean b);

	// Tìm tất cả sản phẩm với trạng thái đang hoạt động
	Page<SanPham> findByTrangThaiTrue(Pageable pageable);
	// Tìm tất cả sản phẩm có trạng thái active với phân trang
   // Page<SanPham> findByTrangThaiTrue(Pageable pageable);

	//List<SanPham> findByTrangThai();
	//List<SanPham> findByTrangThai(Boolean trangThai);
	List<SanPham> findByTrangThai(Boolean trangThai);
	List<SanPham> findByMaSanPhamInAndTrangThai(List<Integer> maSanPham, Boolean trangThai);

	Page<SanPham> findByDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai, Pageable pageable);
	
	List<SanPham> findByTrangThaiTrue();
	
	
	// Tìm sản phẩm hoạt động, có hàng trong kho và có trong đơn nhập hàng
	@Query("SELECT DISTINCT sp FROM SanPham sp JOIN sp.chiTietDonNhapHangs ctdnh WHERE sp.trangThai = true AND ctdnh.soLuongNhap > 0 AND ctdnh.trangThai = true")

    Page<SanPham> findActiveProductsInOrderDetails(Pageable pageable);
    
	// Lấy sản phẩm active theo danh mục, có trong chi tiết đơn nhập hàng với số lượng nhập > 0
	
	@Query("SELECT DISTINCT sp FROM SanPham sp " +
		       "JOIN sp.chiTietDonNhapHangs ctdnh " +
		       "JOIN ctdnh.donNhapHang dnh " +   
		       "WHERE sp.trangThai = true " +
		       "AND sp.danhMuc.maDanhMuc =  ?1 " +
		       "AND ctdnh.soLuongNhap > 0 " +           // Đảm bảo có số lượng nhập
		       "AND ctdnh.donGiaNhap > 0 " +
		       "AND ctdnh.trangThai = true " +          // Đảm bảo chi tiết đơn nhập hàng có trạng thái true
		       "AND dnh.tongGiaTriNhapHang > 0 " 
				)         // Lọc theo danh mục
		Page<SanPham> findActiveProductsInOrderDetailsByCategory(Integer maDanhMuc, Pageable pageable);

	List<SanPham> findByDanhMuc_MaDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai);

    Page<SanPham> findByDanhMuc_MaDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai, Pageable pageable);
    Page<SanPham> findByDanhMuc_MaDanhMucAndTenSanPhamContainingAndTrangThai(Integer maDanhMuc, String keyword, Boolean trangThai, Pageable pageable);

    List<SanPham> findByTenSanPhamContaining(String keyword, Pageable pageable);
	    
    List<SanPham> findByDanhMuc_MaDanhMucAndTenSanPhamContaining(Integer  categoryId, String keyword, Pageable pageable);

 // Tìm kiếm sản phẩm theo danh mục và trạng thái active với từ khóa
    @Query("SELECT sp FROM SanPham sp WHERE sp.danhMuc.maDanhMuc = :categoryId AND sp.trangThai = true AND sp.tenSanPham LIKE %:keyword%")
    List<SanPham> searchActiveProductsByCategoryAndKeyword(@Param("categoryId") Integer categoryId, @Param("keyword") String keyword, Pageable pageable);

 // Tìm kiếm tất cả sản phẩm theo từ khóa, có trạng thái và có chi tiết đơn nhập hàng
    @Query("SELECT sp FROM SanPham sp JOIN sp.chiTietDonNhapHangs ctdh WHERE sp.trangThai = true AND ctdh.soLuongNhap > 0 AND sp.tenSanPham LIKE %:keyword%")
    Page<SanPham> searchAllActiveProductsWithOrderDetails(@Param("keyword") String keyword, Pageable pageable);

 
    @Query("SELECT sp FROM SanPham sp JOIN sp.chiTietDonNhapHangs ctdh WHERE sp.trangThai = true AND sp.danhMuc.maDanhMuc = :categoryId AND ctdh.soLuongNhap > 0 AND (:keyword IS NULL OR :keyword = '' OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SanPham> searchByCategoryWithOrderDetails(@Param("categoryId") Integer categoryId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias")
    List<SanPham> findAllWithDanhGias();
    @Query("SELECT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias WHERE sp.trangThai = true")
    List<SanPham> findAllWithDanhGiasAndTrangThaiTrue();

    @Query("SELECT sp FROM SanPham sp LEFT JOIN FETCH sp.danhGias dg WHERE sp.trangThai = true AND dg.soSao = :soSao")
    List<SanPham> findAllWithDanhGiasAndTrangThaiTrueBySoSao(@Param("soSao") int soSao);

    long countByTrangThaiTrue();
    
}
