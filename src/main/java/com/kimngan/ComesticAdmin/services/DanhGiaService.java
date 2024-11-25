package com.kimngan.ComesticAdmin.services;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
public interface DanhGiaService {
	DanhGia saveDanhGia(DanhGia danhGia);

	List<DanhGia> getDanhGiaBySanPham(SanPham sanPham);

	List<DanhGia> getDanhGiaByNguoiDung(NguoiDung nguoiDung);

	List<DanhGia> findByHoaDonAndNguoiDung(HoaDon hoaDon, NguoiDung nguoiDung); // Thay đổi từ Integer sang đối tượng

	DanhGia create(DanhGia danhGia);

	boolean existsByHoaDonAndNguoiDung(HoaDon hoaDon, NguoiDung nguoiDung);
	
	DanhGia findByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung);
	boolean existsByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung);
	List<DanhGia> findBySanPham(SanPham sanPham);
	// Các phương thức mới thêm vào để phục vụ quản lý của admin
    
    DanhGia findById(Integer maDanhGia); // Tìm đánh giá theo mã
    
    
    
    DanhGia save(DanhGia danhGia);
    Page<DanhGia> getAllDanhGias(Pageable pageable);


}
