package com.kimngan.ComesticAdmin.services;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import com.kimngan.ComesticAdmin.entity.KiemKeKho;
public interface KiemKeKhoService {
	// Lấy tất cả lịch sử kiểm kê kho
    List<KiemKeKho> getAll();
    int countAllByShift(Integer maLichSu);
    // Lấy danh sách kiểm kê theo ca làm việc
    List<KiemKeKho> getByShiftId(Integer maLichSu);

    // Lưu kiểm kê kho
    void save(KiemKeKho kiemKeKho);
    List<KiemKeKho> findByLichSuCaLamViecId(Integer maLichSu);
    int  getDeltaKiemKe(Integer maSanPham);
    Page<KiemKeKho> findByDaXetDuyet(Boolean daXetDuyet, Pageable pageable);
    
    Integer getLastApprovedStock(Integer maSanPham);
}
