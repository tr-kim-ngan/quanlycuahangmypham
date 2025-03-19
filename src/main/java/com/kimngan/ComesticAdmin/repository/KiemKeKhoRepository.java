package com.kimngan.ComesticAdmin.repository;
import org.springframework.data.domain.Pageable;
import com.kimngan.ComesticAdmin.entity.KiemKeKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.domain.Page;

public interface KiemKeKhoRepository extends JpaRepository<KiemKeKho, Integer> {
    List<KiemKeKho> findByLichSuCaLamViec_MaLichSu(Integer maLichSu);
    
    Page<KiemKeKho> findByDaXetDuyet(Boolean daXetDuyet, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(k.soLuongSauKiemKe - k.soLuongTruocKiemKe), 0) FROM KiemKeKho k WHERE k.sanPham.maSanPham = :maSanPham")
    Integer getDeltaKiemKe(@Param("maSanPham") Integer maSanPham);

    

    @Query("SELECT k.soLuongSauKiemKe FROM KiemKeKho k " +
           "WHERE k.sanPham.maSanPham = :maSanPham AND k.daXetDuyet = true " +
           "ORDER BY k.thoiGianKiemKe DESC LIMIT 1")
    Integer getLastApprovedStock(@Param("maSanPham") Integer maSanPham);
    
    @Query("SELECT COUNT(k) FROM KiemKeKho k WHERE k.lichSuCaLamViec.maLichSu = :maLichSu")
    int countAllByShift(@Param("maLichSu") Integer maLichSu);

  
}