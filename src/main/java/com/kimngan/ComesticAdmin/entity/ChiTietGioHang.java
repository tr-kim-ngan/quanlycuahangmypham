package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.io.Serializable;
import java.util.Objects;
@Entity
@Table(name = "ChiTietGioHang")
public class ChiTietGioHang implements Serializable {

    @EmbeddedId
    private ChiTietGioHangId id;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    // Quan hệ với GioHang
    @ManyToOne
    @MapsId("maGioHang")
    private GioHang gioHang;

    // Quan hệ với SanPham
    @ManyToOne
    @MapsId("maSanPham")
    private SanPham sanPham;

    
    public ChiTietGioHang() {}
    
    
    public ChiTietGioHang(ChiTietGioHangId id, GioHang gioHang, SanPham sanPham, Integer soLuong) {
        this.id = id;
        this.gioHang = gioHang;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
    }
    public ChiTietGioHang(GioHang gioHang, SanPham sanPham, int soLuong) {
        this.id = new ChiTietGioHangId(gioHang.getMaGioHang(), sanPham.getMaSanPham());
        this.gioHang = gioHang;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
    }


   

	// Getters và Setters
    public ChiTietGioHangId getId() {
        return id;
    }

    public void setId(ChiTietGioHangId id) {
        this.id = id;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public GioHang getGioHang() {
        return gioHang;
    }

    public void setGioHang(GioHang gioHang) {
        this.gioHang = gioHang;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }
    
 // Triển khai equals và hashCode dựa trên id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietGioHang that = (ChiTietGioHang) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
