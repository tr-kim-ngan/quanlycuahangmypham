package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.DanhMuc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DanhMucService {
    // định nghĩa các CRUD
    List<DanhMuc> getAll();
    Boolean create(DanhMuc danhMuc);
    DanhMuc findById (Integer maDanhMuc);
    Boolean delete(Integer maDanhMuc);
    Boolean update (DanhMuc danhMuc);
//    Boolean existsByTenDanhMuc(String tenDanhMuc);
    Boolean existsByTenDanhMuc(String tenDanhMuc);

 // Phân trang
    Page<DanhMuc> findAll(Pageable pageable);

    // Tìm kiếm danh mục theo tên
    Page<DanhMuc> searchByName(String tenDanhMuc, Pageable pageable);
	//List<DanhMuc> findByTrangThaiTrue();
    boolean hasProducts(Integer categoryId);
}
