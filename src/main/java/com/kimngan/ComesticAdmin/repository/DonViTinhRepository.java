package com.kimngan.ComesticAdmin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.DanhMuc;
import com.kimngan.ComesticAdmin.entity.DonViTinh;
import java.util.Optional;

public interface DonViTinhRepository extends JpaRepository<DonViTinh, Integer>{

	// Phương thức tìm kiếm theo tên đơn vị với phân trang
    Page<DonViTinh> findByTenDonViContainingIgnoreCase(String tenDonVi, PageRequest pageRequest);
    boolean existsByTenDonVi(String tenDonVi);
    Optional<DonViTinh> findByTenDonVi(String tenDonVi);
}
