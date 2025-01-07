package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThuongHieuService {
    // Định nghĩa các CRUD
    List<ThuongHieu> getAll();
    Boolean create(ThuongHieu thuongHieu);
    ThuongHieu findById(Integer maThuongHieu);
    Boolean delete(Integer maThuongHieu);
    Boolean update(ThuongHieu thuongHieu);
    Boolean existsByTenThuongHieu(String tenThuongHieu);

    // Phân trang
    Page<ThuongHieu> findAll(Pageable pageable);

    // Tìm kiếm thương hiệu theo tên
    Page<ThuongHieu> searchByName(String tenThuongHieu, Pageable pageable);

    // Kiểm tra xem thương hiệu có sản phẩm không
    boolean hasProducts(Integer brandId);
}
