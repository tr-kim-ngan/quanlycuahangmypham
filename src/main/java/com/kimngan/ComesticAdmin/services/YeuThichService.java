package com.kimngan.ComesticAdmin.services;

import java.util.List;
import java.util.Set;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.YeuThich;

public interface YeuThichService {

   
    boolean isYeuThich(NguoiDung nguoiDung, SanPham sanPham);

    void addYeuThich(NguoiDung nguoiDung, SanPham sanPham);

    void removeYeuThich(NguoiDung nguoiDung, SanPham sanPham);
    List<SanPham> getFavoritesByUser(NguoiDung nguoiDung);

    public Set<Integer> getFavoriteProductIdsForUser(NguoiDung currentUser) ;


}
