package com.kimngan.ComesticAdmin.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kimngan.ComesticAdmin.entity.LichSuCaLamViec;
public interface LichSuCaLamViecRepository extends JpaRepository<LichSuCaLamViec, Integer> {
    List<LichSuCaLamViec> findByNhanVienMaNguoiDung(Integer maNhanVien);
  
    @Query("SELECT c FROM LichSuCaLamViec c WHERE c.nhanVien.tenNguoiDung = :username AND c.thoiGianKetThuc IS NULL")
    LichSuCaLamViec findCurrentShiftByUsername(@Param("username") String username);

    @Query("SELECT l FROM LichSuCaLamViec l WHERE EXISTS (SELECT k FROM KiemKeKho k WHERE k.lichSuCaLamViec = l AND k.daXetDuyet = false)")
    Page<LichSuCaLamViec> findCaLamChuaXetDuyet(Pageable pageable);

    
    @Query("SELECT DISTINCT ls FROM LichSuCaLamViec ls " +
    	       "JOIN KiemKeKho kk ON ls.maLichSu = kk.lichSuCaLamViec.maLichSu " +
    	       "ORDER BY ls.thoiGianBatDau DESC")
    	Page<LichSuCaLamViec> findAllShiftsWithInventory(Pageable pageable);

}