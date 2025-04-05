package com.kimngan.ComesticAdmin.services;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.TinNhan;



import java.util.List;
public interface TinNhanService {

    List<TinNhan> getTinNhanGiuaHaiNguoi(Integer userId1, Integer userId2);

    TinNhan guiTinNhan(TinNhan tinNhan);

    void danhDauDaDocTheoCuocHoiThoai(Integer nguoiGuiId, Integer nguoiNhanId);
    List<NguoiDung> findAllCustomersWhoChattedWithSeller(Integer sellerId);

    Long demTinChuaDoc(Integer userId);
    
    long demSoKhachHangChuaDoc(Integer sellerId);
    long demTinChuaDocGiua(Integer nguoiGuiId, Integer nguoiNhanId);
    List<NguoiDung> findAllCustomersWhoChattedWithAnySeller();
    
    List<TinNhan> getTinNhanGiuaKhachVaTatCaNhanVien(Integer customerId);
    List<TinNhan> getTatCaTinNhanVoiKhach(Integer maKhachHang);
    void danhDauDaDocTuTatCaNhanVienChoKhach(Integer maKhach);
    
}
