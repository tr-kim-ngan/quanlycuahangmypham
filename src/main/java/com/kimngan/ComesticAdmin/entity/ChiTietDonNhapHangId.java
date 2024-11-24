package com.kimngan.ComesticAdmin.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ChiTietDonNhapHangId implements Serializable {

	@Column(name = "MaDonNhapHang")
	private Integer maDonNhapHang;
	@Column(name = "MaSanPham")
	private Integer maSanPham;

	public ChiTietDonNhapHangId() {

	}

	public ChiTietDonNhapHangId(Integer maDonNhapHang, Integer maSanPham) {
		super();
		this.maDonNhapHang = maDonNhapHang;
		this.maSanPham = maSanPham;
	}

	public Integer getMaDonNhapHang() {
		return maDonNhapHang;
	}

	public void setMaDonNhapHang(Integer maDonNhapHang) {
		this.maDonNhapHang = maDonNhapHang;
	}

	public Integer getMaSanPham() {
		return maSanPham;
	}

	public void setMaSanPham(Integer maSanPham) {
		this.maSanPham = maSanPham;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maDonNhapHang, maSanPham);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ChiTietDonNhapHangId that = (ChiTietDonNhapHangId) o;
		return Objects.equals(maDonNhapHang, that.maDonNhapHang) && Objects.equals(maSanPham, that.maSanPham);
	}

	// Constructors, Getters, Setters, hashCode, equals

}
