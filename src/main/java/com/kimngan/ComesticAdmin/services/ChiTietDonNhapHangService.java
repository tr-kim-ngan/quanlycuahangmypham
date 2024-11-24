package com.kimngan.ComesticAdmin.services;

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
	
	
}
