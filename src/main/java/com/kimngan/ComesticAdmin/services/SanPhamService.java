package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.kimngan.ComesticAdmin.entity.SanPham;

public interface SanPhamService {
	// định nghĩa các CRUD
	List<SanPham> getAll();

	List<SanPham> findByTrangThai(Boolean trangThai);

	SanPham create(SanPham sanPham);

	SanPham findById(Integer maSanPham);

	Boolean delete(Integer maSanPham);

	Boolean update(SanPham SanPham);
	Page<SanPham> searchActiveByMaSanPham(String maSanPham, Pageable pageable);

	Page<SanPham> findAll(Pageable pageable);

	Page<SanPham> searchActiveByName(String tenSanPham, Pageable pageable);

	Page<SanPham> findAllActive(Pageable pageable); // Tìm sản phẩm đang hoạt động
	// Tìm sản phẩm theo ID với Optional

	Optional<SanPham> findByIdOptional(Integer maSanPham);

	Boolean existsByTenSanPham(String tenSanPham);
	List<SanPham> findByMaSanPhamInAndTrangThai(List<Integer> maSanPham, Boolean trangThai) ;
	
	// Thêm phương thức tìm sản phẩm theo danh mục và trạng thái active
	Page<SanPham> findByDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai, Pageable pageable);

	Page<SanPham> getAllActiveProducts(Pageable pageable);
	
	Page<SanPham> getProductsInOrderDetails(Pageable pageable);
	// chỉ hiển thị những sản phẩm trong danh mục có trạng thái true và có trong chi tiết đơn nhập hàng
	//Page<SanPham> findActiveProductsInCategoryWithOrderDetails(Integer maDanhMuc, Pageable pageable);
	
	Page<SanPham> findActiveProductsInOrderDetailsByCategory(Integer maDanhMuc, Pageable pageable);
	List<SanPham> findByDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai);

	List<SanPham> searchAllCategories(String keyword, Pageable pageable);

	List<SanPham> searchByCategory(Integer  categoryId, String keyword, Pageable pageable);

	Page<SanPham> searchAllActiveProductsWithOrderDetails(String keyword, Pageable pageable);

	Page<SanPham> searchByCategoryWithOrderDetails(Integer categoryId, String keyword, Pageable pageable);
	 Double getAverageRatingForProduct(Integer maSanPham);
	 Page<SanPham> findByDanhMucAndTrangThaiWithPagination(Integer maDanhMuc, Boolean trangThai, Pageable pageable);
	 Page<SanPham> searchByCategoryAndName(Integer maDanhMuc, String keyword, Pageable pageable);

	 List<SanPham> findAllWithDanhGiasAndTrangThaiTrue();

	 List<SanPham> findAllWithDanhGiasAndTrangThaiTrueBySoSao(int soSao);
	 Page<SanPham> findAllWithDanhGiasAndTrangThaiTrue(Pageable pageable);

	 Page<SanPham> findAllWithDanhGiasAndTrangThaiTrueBySoSao(int soSao, Pageable pageable);

	 long countActiveProducts();
	 public Page<SanPham> findActiveProductsByBrand(Integer maThuongHieu, Pageable pageable);
	 Page<SanPham> findAllActiveByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

	 Page<SanPham> findActiveProductsByCategoryAndPrice(Integer maDanhMuc, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
	 List<SanPham> findByIdIn(List<Integer> maSanPhams);

	 Page<SanPham> findAllActiveWithStock(Pageable pageable);
	 void capNhatSoLuongTonKho(Integer maSanPham);
	 Page<SanPham> findByTrangThai(Boolean trangThai, Pageable pageable);
	 Integer getTotalImportedQuantity(Integer maSanPham);
	 Integer getSoLuongTrenKe(Integer maSanPham) ;
//	 Integer getSoLuongTonKho(Integer maSanPham) ;
//
//	 Boolean updateSoLuongTonKho(Integer maSanPham, Integer soLuongMoi);
	 void capNhatSoLuongTonKhoHienThi(Integer maSanPham, int soLuongMoi);
	 
	 List<Object[]> getStockStatistics() ;
	 
	 int getSoLuongTonKho(Integer maSanPham) ;
	 
	 List<SanPham> getSanPhamGanHetHang(int nguongCanhBao);
	 List<SanPham> getSanPhamsCoTrongChiTietNhapVaDangHoatDong();

	 List<SanPham> findByThuongHieuAndTrangThai(Integer maThuongHieu, Boolean trangThai);

	 
}
