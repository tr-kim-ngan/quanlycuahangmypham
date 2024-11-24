package com.kimngan.ComesticAdmin.entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
@Entity
@Table(name = "KhuyenMai")
public class KhuyenMai {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maKhuyenMai;

    @Column(name = "TenKhuyenMai", length = 200, nullable = false)
    private String tenKhuyenMai;

    @Column(name = "MoTa", columnDefinition = "TEXT", length = 200, nullable = false)
    private String moTa;

    @Column(name = "PhanTramGiamGia", precision = 5, scale = 0,nullable = false)
    private BigDecimal phanTramGiamGia;

    @Column(name = "NgayBatDau",nullable = false)
    private Date ngayBatDau;

    @Column(name = "NgayKetThuc",nullable = false)
    private Date ngayKetThuc;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai; 
    
    @ManyToMany(mappedBy = "khuyenMais")
    private Set<SanPham> sanPhams;

    public KhuyenMai() {
    	
    }
    
    
	public KhuyenMai(Integer maKhuyenMai, String tenKhuyenMai, String moTa, BigDecimal phanTramGiamGia, Date ngayBatDau,
			Date ngayKetThuc, Boolean trangThai, Set<SanPham> sanPhams) {
		super();
		this.maKhuyenMai = maKhuyenMai;
		this.tenKhuyenMai = tenKhuyenMai;
		this.moTa = moTa;
		this.phanTramGiamGia = phanTramGiamGia;
		this.ngayBatDau = ngayBatDau;
		this.ngayKetThuc = ngayKetThuc;
		this.trangThai = trangThai;
		this.sanPhams = sanPhams;
	}


	public Integer getMaKhuyenMai() {
		return maKhuyenMai;
	}


	public void setMaKhuyenMai(Integer maKhuyenMai) {
		this.maKhuyenMai = maKhuyenMai;
	}


	public String getTenKhuyenMai() {
		return tenKhuyenMai;
	}


	public void setTenKhuyenMai(String tenKhuyenMai) {
		this.tenKhuyenMai = tenKhuyenMai;
	}


	public String getMoTa() {
		return moTa;
	}


	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}


	public BigDecimal getPhanTramGiamGia() {
		return phanTramGiamGia;
	}


	public void setPhanTramGiamGia(BigDecimal phanTramGiamGia) {
		this.phanTramGiamGia = phanTramGiamGia;
	}


	public Date getNgayBatDau() {
		return ngayBatDau;
	}


	public void setNgayBatDau(Date ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}


	public Date getNgayKetThuc() {
		return ngayKetThuc;
	}


	public void setNgayKetThuc(Date ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}


	public Boolean getTrangThai() {
		return trangThai;
	}


	public void setTrangThai(Boolean trangThai) {
		this.trangThai = trangThai;
	}


	public Set<SanPham> getSanPhams() {
		return sanPhams;
	}


	public void setSanPhams(Set<SanPham> sanPhams) {
		this.sanPhams = sanPhams;
	}
	
	
	

    
    
    

}
