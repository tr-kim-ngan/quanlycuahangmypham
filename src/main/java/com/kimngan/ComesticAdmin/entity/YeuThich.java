package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "YeuThich")
public class YeuThich {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "maNguoiDung", nullable = false)
	private NguoiDung nguoiDung;

	@ManyToOne
	@JoinColumn(name = "maSanPham", nullable = false)
	private SanPham sanPham;

	public YeuThich() {
	}

	public YeuThich(Integer id, NguoiDung nguoiDung, SanPham sanPham) {
		super();
		this.id = id;
		this.nguoiDung = nguoiDung;
		this.sanPham = sanPham;
	}

	// Getters và setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public NguoiDung getNguoiDung() {
		return nguoiDung;
	}

	public void setNguoiDung(NguoiDung nguoiDung) {
		this.nguoiDung = nguoiDung;
	}

	public SanPham getSanPham() {
		return sanPham;
	}

	public void setSanPham(SanPham sanPham) {
		this.sanPham = sanPham;
	}
}
