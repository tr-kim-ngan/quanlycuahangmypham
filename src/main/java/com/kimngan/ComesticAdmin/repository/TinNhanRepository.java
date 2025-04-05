package com.kimngan.ComesticAdmin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.TinNhan;

@Repository
public interface TinNhanRepository extends JpaRepository<TinNhan, Integer> {

    // Lấy tin nhắn giữa 2 người theo thời gian tăng dần
    @Query("SELECT t FROM TinNhan t WHERE " +
           "(t.nguoiGui.maNguoiDung = :userId1 AND t.nguoiNhan.maNguoiDung = :userId2) " +
           "OR (t.nguoiGui.maNguoiDung = :userId2 AND t.nguoiNhan.maNguoiDung = :userId1) " +
           "ORDER BY t.thoiGianGui ASC")
    List<TinNhan> findTinNhanGiuaHaiNguoi(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // Đếm tin chưa đọc
    @Query("SELECT COUNT(t) FROM TinNhan t WHERE t.nguoiNhan.maNguoiDung = :userId AND t.daDoc = false")
    Long countTinChuaDoc(@Param("userId") Integer userId);
    
    
    @Query("SELECT DISTINCT t.nguoiGui FROM TinNhan t WHERE t.nguoiNhan.maNguoiDung = :sellerId")
    List<NguoiDung> findAllCustomersWhoChattedWithSeller(@Param("sellerId") Integer sellerId);

    @Query("SELECT COUNT(DISTINCT t.nguoiGui.maNguoiDung) FROM TinNhan t WHERE t.nguoiNhan.maNguoiDung = :sellerId AND t.daDoc = false")
    long countDistinctNguoiGuiIdByNguoiNhanIdAndDaDocFalse(@Param("sellerId") Integer sellerId);

    
    @Query("SELECT COUNT(t) FROM TinNhan t WHERE t.nguoiGui.id = :nguoiGuiId AND t.nguoiNhan.id = :nguoiNhanId AND t.daDoc = false")
    long demTinChuaDocGiua(@Param("nguoiGuiId") Integer nguoiGuiId, @Param("nguoiNhanId") Integer nguoiNhanId);

    @Query("SELECT DISTINCT u FROM TinNhan t " +
    	       "JOIN NguoiDung u ON (u = t.nguoiGui OR u = t.nguoiNhan) " +
    	       "WHERE u.quyenTruyCap.maQuyen = 2 AND " +
    	       "((t.nguoiGui.quyenTruyCap.maQuyen = 5) OR (t.nguoiNhan.quyenTruyCap.maQuyen = 5))")
    	List<NguoiDung> findAllCustomersWhoChattedWithAnySeller();
   
    @Query("""
    	    SELECT t FROM TinNhan t 
    	    WHERE (t.nguoiGui.maNguoiDung = :customerId AND t.nguoiNhan.quyenTruyCap.maQuyen = 5)
    	       OR (t.nguoiNhan.maNguoiDung = :customerId AND t.nguoiGui.quyenTruyCap.maQuyen = 5)
    	    ORDER BY t.thoiGianGui ASC
    	""")
    	List<TinNhan> findTinNhanVoiTatCaNhanVien(@Param("customerId") Integer customerId);

    @Query("SELECT t FROM TinNhan t WHERE t.nguoiNhan.maNguoiDung = :maKhach AND t.daDoc = false AND t.nguoiGui.quyenTruyCap.maQuyen = 5")
    List<TinNhan> findChuaDocTuNhanVienGuiChoKhach(@Param("maKhach") Integer maKhach);
   
    
    @Modifying
    @Query("""
        UPDATE TinNhan t 
        SET t.daDoc = true 
        WHERE t.nguoiNhan.maNguoiDung = :maKhachHang 
          AND t.nguoiGui.quyenTruyCap.maQuyen = 5 
          AND t.daDoc = false
    """)
    void danhDauDaDocTuTatCaNhanVien(@Param("maKhachHang") Integer maKhachHang);

    
    
    
      
}

