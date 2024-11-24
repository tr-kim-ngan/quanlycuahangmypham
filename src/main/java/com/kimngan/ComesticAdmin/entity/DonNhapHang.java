package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
//import java.time.LocalDateTime;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "DonNhapHang")
public class DonNhapHang {

	@Id
	@Column(name = "MaDonNhapHang")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer maDonNhapHang;

	@Column(name = "NgayNhapHang", nullable = false)
	private LocalDate  ngayNhapHang;

	@Column(name = "TongGiaTriNhapHang", precision = 19, scale = 2)
	private BigDecimal tongGiaTriNhapHang= BigDecimal.ZERO; ;

	@Column(name = "TrangThai", nullable = false)
	private boolean trangThai = true;

	@ManyToOne
	@JoinColumn(name = "MaNhaCungCap", referencedColumnName = "MaNhaCungCap", nullable = false)
	private NhaCungCap nhaCungCap;

	@OneToMany(mappedBy = "donNhapHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ChiTietDonNhapHang> chiTietDonNhapHangs=  new ArrayList<>();

	public DonNhapHang() {
	}

	

	public DonNhapHang(Integer maDonNhapHang, LocalDate ngayNhapHang, BigDecimal tongGiaTriNhapHang, boolean trangThai,
			NhaCungCap nhaCungCap, List<ChiTietDonNhapHang> chiTietDonNhapHangs) {
		super();
		this.maDonNhapHang = maDonNhapHang;
		this.ngayNhapHang = ngayNhapHang;
		this.tongGiaTriNhapHang = tongGiaTriNhapHang;
		this.trangThai = trangThai;
		this.nhaCungCap = nhaCungCap;
		this.chiTietDonNhapHangs = chiTietDonNhapHangs;
	}

	public Integer getMaDonNhapHang() {
		return maDonNhapHang;
	}

	public void setMaDonNhapHang(Integer maDonNhapHang) {
		this.maDonNhapHang = maDonNhapHang;
	}

	

	public LocalDate getNgayNhapHang() {
		return ngayNhapHang;
	}



	public void setNgayNhapHang(LocalDate ngayNhapHang) {
		this.ngayNhapHang = ngayNhapHang;
	}



	public BigDecimal getTongGiaTriNhapHang() {
		return tongGiaTriNhapHang;
	}

	public void setTongGiaTriNhapHang(BigDecimal tongGiaTriNhapHang) {
		this.tongGiaTriNhapHang = tongGiaTriNhapHang;
	}

	public NhaCungCap getNhaCungCap() {
		return nhaCungCap;
	}

	public void setNhaCungCap(NhaCungCap nhaCungCap) {
		this.nhaCungCap = nhaCungCap;
	}

	

	public List<ChiTietDonNhapHang> getChiTietDonNhapHangs() {
		return chiTietDonNhapHangs;
	}

	public void setChiTietDonNhapHangs(List<ChiTietDonNhapHang> chiTietDonNhapHangs) {
		this.chiTietDonNhapHangs = chiTietDonNhapHangs;
	}

	public boolean isTrangThai() {
		return trangThai;
	}

	public void setTrangThai(boolean trangThai) {
		this.trangThai = trangThai;
	}

	// Getters and Setters

}
