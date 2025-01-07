package com.kimngan.ComesticAdmin.repository;
import com.kimngan.ComesticAdmin.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    boolean existsByTenThuongHieu(String tenThuongHieu);
    Page<ThuongHieu> findByTenThuongHieuContainingIgnoreCase(String tenThuongHieu, Pageable pageable);
}