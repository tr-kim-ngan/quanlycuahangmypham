//package com.kimngan.ComesticAdmin.entity;
//
//import java.time.LocalDateTime;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//
//@Entity
//@Table(name = "DieuChinhKho")
//public class DieuChinhKho {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer maDieuChinh;
//
//    @ManyToOne
//    @JoinColumn(name = "maSanPham", nullable = false)
//    private SanPham sanPham;
//
//    @ManyToOne
//    @JoinColumn(name = "maNhanVien", nullable = false)
//    private NguoiDung nhanVien;
//
//    @Column(nullable = false)
//    private String loaiDieuChinh; // Không dùng Enum, lưu String: "TANG" hoặc "GIAM"
//
//    @Column(nullable = false)
//    private Integer soLuongDieuChinh;
//
//    @Column(nullable = false)
//    private LocalDateTime thoiGianDieuChinh;
//
//    @Column(nullable = false)
//    private String ghiChu; // Lý do điều chỉnh
//
//    public DieuChinhKho() {}
//    
//	public DieuChinhKho(Integer maDieuChinh, SanPham sanPham, NguoiDung nhanVien, String loaiDieuChinh,
//			Integer soLuongDieuChinh, LocalDateTime thoiGianDieuChinh, String ghiChu) {
//		super();
//		this.maDieuChinh = maDieuChinh;
//		this.sanPham = sanPham;
//		this.nhanVien = nhanVien;
//		this.loaiDieuChinh = loaiDieuChinh;
//		this.soLuongDieuChinh = soLuongDieuChinh;
//		this.thoiGianDieuChinh = thoiGianDieuChinh;
//		this.ghiChu = ghiChu;
//	}
//
//	public Integer getMaDieuChinh() {
//		return maDieuChinh;
//	}
//
//	public void setMaDieuChinh(Integer maDieuChinh) {
//		this.maDieuChinh = maDieuChinh;
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
//	public NguoiDung getNhanVien() {
//		return nhanVien;
//	}
//
//	public void setNhanVien(NguoiDung nhanVien) {
//		this.nhanVien = nhanVien;
//	}
//
//	public String getLoaiDieuChinh() {
//		return loaiDieuChinh;
//	}
//
//	public void setLoaiDieuChinh(String loaiDieuChinh) {
//		this.loaiDieuChinh = loaiDieuChinh;
//	}
//
//	public Integer getSoLuongDieuChinh() {
//		return soLuongDieuChinh;
//	}
//
//	public void setSoLuongDieuChinh(Integer soLuongDieuChinh) {
//		this.soLuongDieuChinh = soLuongDieuChinh;
//	}
//
//	public LocalDateTime getThoiGianDieuChinh() {
//		return thoiGianDieuChinh;
//	}
//
//	public void setThoiGianDieuChinh(LocalDateTime thoiGianDieuChinh) {
//		this.thoiGianDieuChinh = thoiGianDieuChinh;
//	}
//
//	public String getGhiChu() {
//		return ghiChu;
//	}
//
//	public void setGhiChu(String ghiChu) {
//		this.ghiChu = ghiChu;
//	}
//
//
//
//    // Getters và Setters
//    
//    
//    
//    
//    
//}
