//package com.kimngan.ComesticAdmin.entity;
//
//import jakarta.persistence.*;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "DonGiaBanHang")
//public class DonGiaBanHang {
//
//	@EmbeddedId
//	private DonGiaBanHangId id;
//
//	@ManyToOne
//	@MapsId("maSanPham")
//   // @JoinColumn(name = "MaSanPham", nullable = false, insertable = false, updatable = false)
//	private SanPham sanPham;
//
//	@ManyToOne
//	@MapsId("ngayGio")
//  //  @JoinColumn(name = "NgayGio", nullable = false, insertable = false, updatable = false)
//	private ThoiDiem thoiDiem;
//
//	@Column(name = "DonGiaBan", precision = 8, scale = 2, nullable = false)
//	private BigDecimal donGiaBan;
//
//	public DonGiaBanHang() {
//	}
//
//	public DonGiaBanHang(DonGiaBanHangId id, SanPham sanPham, ThoiDiem thoiDiem, BigDecimal donGiaBan) {
//		super();
//		this.id = id;
//		this.sanPham = sanPham;
//		this.thoiDiem = thoiDiem;
//		this.donGiaBan = donGiaBan;
//	}
//
//	public DonGiaBanHangId getId() {
//		return id;
//	}
//
//	public void setId(DonGiaBanHangId id) {
//		this.id = id;
//	}
//
//	public SanPham getSanPham() {
//		return sanPham;
//	}
//
//	public void setSanPham(SanPham sanPham) {
//		this.sanPham = sanPham;
//	}
//
//	public ThoiDiem getThoiDiem() {
//		return thoiDiem;
//	}
//
//	public void setThoiDiem(ThoiDiem thoiDiem) {
//		this.thoiDiem = thoiDiem;
//	}
//
//	public BigDecimal getDonGiaBan() {
//		return donGiaBan;
//	}
//
//	public void setDonGiaBan(BigDecimal donGiaBan) {
//		this.donGiaBan = donGiaBan;
//	}
//
//}
