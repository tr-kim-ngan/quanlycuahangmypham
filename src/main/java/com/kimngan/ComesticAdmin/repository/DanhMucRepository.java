package com.kimngan.ComesticAdmin.repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.DanhMuc;
public interface DanhMucRepository extends JpaRepository<DanhMuc,Integer>{
	
	 Page<DanhMuc> findByTenDanhMucContainingIgnoreCase(String tenDanhMuc, Pageable pageable);
	// Kiểm tra sự tồn tại của tên danh mục
	    boolean existsByTenDanhMuc(String tenDanhMuc);
	    DanhMuc findByTenDanhMuc(String tenDanhMuc);
		//List<DanhMuc> findByTrangThaiTrue();
	    List<DanhMuc> findAll();

}
