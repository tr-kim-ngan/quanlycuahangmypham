package com.kimngan.ComesticAdmin.services;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import com.kimngan.ComesticAdmin.entity.LichSuCaLamViec;

public interface LichSuCaLamViecService {
    LichSuCaLamViec batDauCa(LichSuCaLamViec caLamViec); // Bắt đầu ca làm việc
    LichSuCaLamViec ketThucCa(Integer maCa); // Kết thúc ca làm việc
    List<LichSuCaLamViec> getLichSuCaTheoNhanVien(Integer maNhanVien); // Lấy lịch sử ca của 1 nhân viên
    List<LichSuCaLamViec> getTatCaLichSu(); // Lấy toàn bộ lịch sử ca làm việc

    List<LichSuCaLamViec> getAllShifts();
    void saveShift(LichSuCaLamViec caLamViec);
    Optional<LichSuCaLamViec> findById(Integer id);
    LichSuCaLamViec getCurrentShift(String username);
    Page<LichSuCaLamViec> getAllShifts(Pageable pageable);
    
    Page<LichSuCaLamViec> findCaLamChuaXetDuyet(Pageable pageable);
    Page<LichSuCaLamViec> getAllShiftsWithInventory(Pageable pageable);
    
}