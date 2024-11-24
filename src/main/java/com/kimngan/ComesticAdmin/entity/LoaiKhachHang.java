package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "LoaiKhachHang")
public class LoaiKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maLoaiKhachHang")  // Đặt tên cho cột ID
    private Integer maLoaiKhachHang;

    @Column(name = "tenLoaiKH", nullable = false, length = 100) // Đặt tên cho cột Tên loại khách hàng
    private String tenLoaiKH;

    @Column(name = "moTa", nullable = true, columnDefinition = "TEXT") // Đặt tên cho cột Mô tả
    private String moTa;

    @Column(name = "trangThai", nullable = false) // Đặt tên cho cột Trạng thái
    private Boolean trangThai;

    // One-to-One relationship with PhieuGiamGia
    @OneToOne(mappedBy = "loaiKhachHang", cascade = CascadeType.ALL)
    private PhieuGiamGia phieuGiamGia;

    // Constructors, Getters, and Setters
    public LoaiKhachHang() {}

  

    public LoaiKhachHang(Integer maLoaiKhachHang, String tenLoaiKH, String moTa, Boolean trangThai,
			PhieuGiamGia phieuGiamGia) {
		super();
		this.maLoaiKhachHang = maLoaiKhachHang;
		this.tenLoaiKH = tenLoaiKH;
		this.moTa = moTa;
		this.trangThai = trangThai;
		this.phieuGiamGia = phieuGiamGia;
	}



	public Integer getMaLoaiKhachHang() {
        return maLoaiKhachHang;
    }

    public void setMaLoaiKhachHang(Integer maLoaiKhachHang) {
        this.maLoaiKhachHang = maLoaiKhachHang;
    }

    public String getTenLoaiKH() {
        return tenLoaiKH;
    }

    public void setTenLoaiKH(String tenLoaiKH) {
        this.tenLoaiKH = tenLoaiKH;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public PhieuGiamGia getPhieuGiamGia() {
        return phieuGiamGia;
    }

    public void setPhieuGiamGia(PhieuGiamGia phieuGiamGia) {
        this.phieuGiamGia = phieuGiamGia;
    }
}
