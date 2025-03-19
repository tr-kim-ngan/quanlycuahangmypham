package com.kimngan.ComesticAdmin.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "ChiTietDonHang")
public class ChiTietDonHang implements Serializable {

    @EmbeddedId
    private ChiTietDonHangId id;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "GiaTaiThoiDiemDat", precision = 10, scale = 2, nullable = false)
    private BigDecimal giaTaiThoiDiemDat;

    @Column(name = "KhuyenMai", precision = 10, scale = 2)
    private BigDecimal khuyenMai;

    // Quan hệ với DonHang
    @ManyToOne
    @MapsId("maDonHang")
    private DonHang donHang;

    // Quan hệ với SanPham
    @ManyToOne
    @MapsId("maSanPham")
    private SanPham sanPham;

    public ChiTietDonHang() {}

    public ChiTietDonHang(ChiTietDonHangId id, DonHang donHang, SanPham sanPham, Integer soLuong,
                          BigDecimal giaTaiThoiDiemDat, BigDecimal khuyenMai) {
        this.id = id;
        this.donHang = donHang;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.giaTaiThoiDiemDat = giaTaiThoiDiemDat;
        this.khuyenMai = khuyenMai;
    }

    // Constructors, Getters, Setters
    public ChiTietDonHang(DonHang donHang, SanPham sanPham, Integer soLuong,
                          BigDecimal giaTaiThoiDiemDat, BigDecimal khuyenMai) {
        this.id = new ChiTietDonHangId(donHang.getMaDonHang(), sanPham.getMaSanPham());
        this.donHang = donHang;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.giaTaiThoiDiemDat = giaTaiThoiDiemDat;
        this.khuyenMai = khuyenMai;
    }

    public ChiTietDonHangId getId() {
        return id;
    }

    public void setId(ChiTietDonHangId id) {
        this.id = id;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getGiaTaiThoiDiemDat() {
        return giaTaiThoiDiemDat;
    }

    public void setGiaTaiThoiDiemDat(BigDecimal giaTaiThoiDiemDat) {
        this.giaTaiThoiDiemDat = giaTaiThoiDiemDat;
    }
    public BigDecimal getThanhTien() {
        return giaTaiThoiDiemDat.multiply(BigDecimal.valueOf(soLuong));
    }

    public BigDecimal getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(BigDecimal khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public DonHang getDonHang() {
        return donHang;
    }

    public void setDonHang(DonHang donHang) {
        this.donHang = donHang;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietDonHang)) return false;
        ChiTietDonHang that = (ChiTietDonHang) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}