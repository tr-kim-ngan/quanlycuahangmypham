package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KhuyenMaiService {
	
	// Tạo mới một khuyến mãi
    KhuyenMai create(KhuyenMai khuyenMai);

    // Cập nhật khuyến mãi
    KhuyenMai update(KhuyenMai khuyenMai);

    // Xóa khuyến mãi (thực chất là chuyển trạng thái)
    Boolean delete(Integer maKhuyenMai);
 // Phân trang và tìm khuyến mãi theo tên và trạng thái
    Page<KhuyenMai> searchByName(String tenKhuyenMai, Pageable pageable);

    // Lấy tất cả khuyến mãi còn hoạt động (trạng thái = 1)
    Page<KhuyenMai> findAllActive(Pageable pageable);

    // Tìm khuyến mãi theo ID
    KhuyenMai findById(Integer maKhuyenMai);
    
    // Lấy khuyến mãi theo trạng thái
    List<KhuyenMai> findByTrangThai(Boolean trangThai);

}
