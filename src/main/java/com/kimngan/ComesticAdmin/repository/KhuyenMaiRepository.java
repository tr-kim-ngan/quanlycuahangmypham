package com.kimngan.ComesticAdmin.repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;

public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Integer>{
	// Tìm tất cả khuyến mãi với trạng thái còn hoạt động
    Page<KhuyenMai> findByTenKhuyenMaiContainingIgnoreCaseAndTrangThai(String tenKhuyenMai,Boolean trangThai, Pageable pageable);

    // Lấy tất cả khuyến mãi có trạng thái = 1
    Page<KhuyenMai> findByTrangThaiTrue(Pageable pageable);

}
