package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChiTietGioHangId implements Serializable {

    @Column(name = "MaGioHang")
    private Integer maGioHang;

    @Column(name = "MaSanPham")
    private Integer maSanPham;

    // Constructors, equals, và hashCode
    public ChiTietGioHangId() {}

    public ChiTietGioHangId(Integer maGioHang, Integer maSanPham) {
        this.maGioHang = maGioHang;
        this.maSanPham = maSanPham;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietGioHangId)) return false;
        ChiTietGioHangId that = (ChiTietGioHangId) o;
        return Objects.equals(maGioHang, that.maGioHang) &&
               Objects.equals(maSanPham, that.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maGioHang, maSanPham);
    }

    // Getters và Setters
    public Integer getMaGioHang() {
        return maGioHang;
    }

    public void setMaGioHang(Integer maGioHang) {
        this.maGioHang = maGioHang;
    }

    public Integer getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(Integer maSanPham) {
        this.maSanPham = maSanPham;
    }
}
