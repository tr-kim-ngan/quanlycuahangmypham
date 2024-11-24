package com.kimngan.ComesticAdmin.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "ChiTietDonNhapHang")
public class ChiTietDonNhapHang {

	@EmbeddedId
	private ChiTietDonNhapHangId id;
	
	@Column(name = "SoLuongNhap", nullable = false)
	private Integer soLuongNhap;

	@Column(name = "DonGiaNhap", nullable = false)
	private BigDecimal donGiaNhap;

	@Column(name = "TrangThai", nullable = false)
	private boolean trangThai = true;

	// Quan hệ với DonNhapHang, ánh xạ khóa ngoại maDonNhapHang
	@ManyToOne
	@MapsId("maDonNhapHang") // ánh xạ trường maDonNhapHang trong ChiTietDonNhapHangId
	// @JoinColumn(name = "maDonNhapHang", nullable = false)
	private DonNhapHang donNhapHang;

	// Quan hệ với SanPham, ánh xạ khóa ngoại maSanPham
	@ManyToOne
	@MapsId("maSanPham") // ánh xạ trường maSanPham trong ChiTietDonNhapHangId
	// @JoinColumn(name = "maSanPham", nullable = false)
	private SanPham sanPham;

	public ChiTietDonNhapHang() {
	}

	

	public ChiTietDonNhapHang(ChiTietDonNhapHangId id, Integer soLuongNhap, BigDecimal donGiaNhap, boolean trangThai,
			DonNhapHang donNhapHang, SanPham sanPham) {
		super();
		this.id = id;
		this.soLuongNhap = soLuongNhap;
		this.donGiaNhap = donGiaNhap;
		this.trangThai = trangThai;
		this.donNhapHang = donNhapHang;
		this.sanPham = sanPham;
	}



	public ChiTietDonNhapHangId getId() {
		return id;
	}

	public void setId(ChiTietDonNhapHangId id) {
		this.id = id;
	}

	public Integer getSoLuongNhap() {
		return soLuongNhap;
	}

	public void setSoLuongNhap(Integer soLuongNhap) {
		this.soLuongNhap = soLuongNhap;
	}

	public BigDecimal getDonGiaNhap() {
		return donGiaNhap;
	}

	public void setDonGiaNhap(BigDecimal donGiaNhap) {
		this.donGiaNhap = donGiaNhap;
	}

	public DonNhapHang getDonNhapHang() {
		return donNhapHang;
	}

	public void setDonNhapHang(DonNhapHang donNhapHang) {
		this.donNhapHang = donNhapHang;
	}

	public SanPham getSanPham() {
		return sanPham;
	}

	public void setSanPham(SanPham sanPham) {
		this.sanPham = sanPham;
	}



	public boolean isTrangThai() {
		return trangThai;
	}



	public void setTrangThai(boolean trangThai) {
		this.trangThai = trangThai;
	}

	// Constructors, Getters, Setters

}
