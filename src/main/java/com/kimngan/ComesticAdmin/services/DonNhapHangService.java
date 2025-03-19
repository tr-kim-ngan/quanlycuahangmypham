package com.kimngan.ComesticAdmin.services;

import java.time.LocalDate;
import java.util.List;

import com.kimngan.ComesticAdmin.entity.DonNhapHang;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface DonNhapHangService {
	 // Định nghĩa các CRUD
    List<DonNhapHang> getAll();
    Boolean create(DonNhapHang donNhapHang);
    DonNhapHang findById(Integer maDonNhapHang);
    Boolean delete(Integer maDonNhapHang); // Xóa bằng cách thay đổi trạng thái
    Boolean update(DonNhapHang donNhapHang);

    // Phân trang
    Page<DonNhapHang> findByTrangThai(boolean trangThai, Pageable pageable);

    // Tìm kiếm đơn nhập hàng theo ngày nhập
   // Page<DonNhapHang> searchByDate(LocalDateTime ngayNhap, Pageable pageable);
	Page<DonNhapHang> findByNgayNhapHang(LocalDate ngayNhap, Pageable pageable);
	
	//Page<DonNhapHang> searchByDonNhapHang(String keyword, Pageable pageable) ;
	//Page<DonNhapHang> findByNgayNhapHang(LocalDate ngayNhapHang, Pageable pageable);

	Page<DonNhapHang> findByNgayNhapHangBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

	// Lấy tất cả khuyến mãi còn hoạt động (trạng thái = 1)
    Page<DonNhapHang> findAllActive(Pageable pageable);
    Page<DonNhapHang> findByNhaCungCap_Ten(String tenNhaCungCap, Pageable pageable);

    void deleteDonNhapHang(Integer maDonNhapHang) ;
	
	
}
