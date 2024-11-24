package com.kimngan.ComesticAdmin.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ChiTietDonHangId implements Serializable {
    @Column(name = "MaDonHang")
    private Integer maDonHang;

    @Column(name = "MaSanPham")
    private Integer maSanPham;

    public ChiTietDonHangId() {}

    public ChiTietDonHangId(Integer maDonHang, Integer maSanPham) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
    }

    // Getters và Setters
    public Integer getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(Integer maDonHang) {
        this.maDonHang = maDonHang;
    }

    public Integer getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(Integer maSanPham) {
        this.maSanPham = maSanPham;
    }

    // Triển khai equals và hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietDonHangId that = (ChiTietDonHangId) o;
        return Objects.equals(maDonHang, that.maDonHang) &&
               Objects.equals(maSanPham, that.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDonHang, maSanPham);
    }
}
