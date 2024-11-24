package com.kimngan.ComesticAdmin.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "NhaCungCap")
public class NhaCungCap {

	@Id
	@Column(name = "MaNhaCungCap")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer maNhaCungCap;

	@Column(name = "TenNhaCungCap", length = 100, nullable = false)
	private String tenNhaCungCap;

	@Column(name = "sdtNhaCungCap", length = 10, nullable = false)
	private String sdtNhaCungCap;

	@Column(name = "diaChiNhaCungCap", length = 255, nullable = false)
	private String diaChiNhaCungCap;

	@Column(name = "EmailNhaCungCap", length = 100, nullable = false)
	private String emailNhaCungCap;

	@Column(name = "TrangThai", nullable = false)
	private boolean trangThai = true;

	@OneToMany(mappedBy = "nhaCungCap")
	private Set<DonNhapHang> donNhapHangs;

	@ManyToMany(mappedBy = "nhaCungCaps")
	private Set<SanPham> sanPhams;

	public NhaCungCap() {
	}

	public NhaCungCap(Integer maNhaCungCap, String tenNhaCungCap, String sdtNhaCungCap, String diaChiNhaCungCap,
			String emailNhaCungCap, boolean trangThai, Set<DonNhapHang> donNhapHangs, Set<SanPham> sanPhams) {
		super();
		this.maNhaCungCap = maNhaCungCap;
		this.tenNhaCungCap = tenNhaCungCap;
		this.sdtNhaCungCap = sdtNhaCungCap;
		this.diaChiNhaCungCap = diaChiNhaCungCap;
		this.emailNhaCungCap = emailNhaCungCap;
		this.trangThai = trangThai;
		this.donNhapHangs = donNhapHangs;
		this.sanPhams = sanPhams;
	}

	public Integer getMaNhaCungCap() {
		return maNhaCungCap;
	}

	public void setMaNhaCungCap(Integer maNhaCungCap) {
		this.maNhaCungCap = maNhaCungCap;
	}

	public String getTenNhaCungCap() {
		return tenNhaCungCap;
	}

	public void setTenNhaCungCap(String tenNhaCungCap) {
		this.tenNhaCungCap = tenNhaCungCap;
	}

	public String getSdtNhaCungCap() {
		return sdtNhaCungCap;
	}

	public void setSdtNhaCungCap(String sdtNhaCungCap) {
		this.sdtNhaCungCap = sdtNhaCungCap;
	}

	public String getDiaChiNhaCungCap() {
		return diaChiNhaCungCap;
	}

	public void setDiaChiNhaCungCap(String diaChiNhaCungCap) {
		this.diaChiNhaCungCap = diaChiNhaCungCap;
	}

	public String getEmailNhaCungCap() {
		return emailNhaCungCap;
	}

	public void setEmailNhaCungCap(String emailNhaCungCap) {
		this.emailNhaCungCap = emailNhaCungCap;
	}

	public Set<DonNhapHang> getDonNhapHangs() {
		return donNhapHangs;
	}

	public void setDonNhapHangs(Set<DonNhapHang> donNhapHangs) {
		this.donNhapHangs = donNhapHangs;
	}

	public Set<SanPham> getSanPhams() {
		return sanPhams;
	}

	public void setSanPhams(Set<SanPham> sanPhams) {
		this.sanPhams = sanPhams;
	}

	public boolean isTrangThai() {
		return trangThai;
	}

	public void setTrangThai(boolean trangThai) {
		this.trangThai = trangThai;
	}

	// Getters and Setters

}
