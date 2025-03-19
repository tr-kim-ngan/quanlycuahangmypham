package com.kimngan.ComesticAdmin.services;

import java.time.LocalDate;
import java.util.List;

import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHangId;
import com.kimngan.ComesticAdmin.entity.DonNhapHang;
import com.kimngan.ComesticAdmin.entity.SanPham;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChiTietDonNhapHangService {
	// Định nghĩa các CRUD
	List<ChiTietDonNhapHang> getAll();

	List<ChiTietDonNhapHang> findByDonNhapHang(DonNhapHang donNhapHang);

	ChiTietDonNhapHang findById(ChiTietDonNhapHangId id);

	Boolean create(ChiTietDonNhapHang chiTietDonNhapHang);

	Boolean update(ChiTietDonNhapHang chiTietDonNhapHang);

	Boolean delete(ChiTietDonNhapHangId id); // Xóa bằng cách thay đổi trạng thái

	// Phân trang
	Page<ChiTietDonNhapHang> findAll(Pageable pageable);

	// Tìm kiếm theo tên sản phẩm trong chi tiết đơn nhập hàng
	Page<ChiTietDonNhapHang> searchBySanPhamName(String tenSanPham, Pageable pageable);

	List<ChiTietDonNhapHang> findBySanPham(SanPham sanPham);

	void updateChiTietDonNhapHangForProduct(SanPham sanPham);

	public boolean existsBySanPham(SanPham sanPham);

	// Thêm hỗ trợ phân trang cho findByDonNhapHang
	Page<ChiTietDonNhapHang> findByDonNhapHang(DonNhapHang donNhapHang, Pageable pageable);

	Integer getTotalImportedQuantityBySanPhamId(Integer sanPhamId);

	LocalDate findLastTimeStockEmpty(Integer maSanPham);
	Integer getTotalImportedQuantityAfterStockEmpty(Integer maSanPham, LocalDate lastStockEmptyTime);
	 List<Object[]> getImportStatistics(LocalDate fromDate, LocalDate toDate);
	 List<Object[]> getBaoCaoChiTiet(LocalDate fromDate, LocalDate toDate);
	 List<Object[]> getTopSuppliers(LocalDate fromDate, LocalDate toDate);
	 List<Object[]> getTotalImportValue(LocalDate fromDate, LocalDate toDate);
	 List<Object[]> getTotalImportReport(LocalDate fromDate, LocalDate toDate);
	 List<Object[]> getImportTrend(LocalDate fromDate, LocalDate toDate);
	 List<Object[]> getImportTrendDetail(LocalDate fromDate, LocalDate toDate);
	 List<Object[]> getTopImportedProducts(LocalDate fromDate, LocalDate toDate);

}
