package com.kimngan.ComesticAdmin.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDanhGia;

    @ManyToOne
    @JoinColumn(name = "maSanPham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "maNguoiDung")
    private NguoiDung nguoiDung;
    @ManyToOne
    @JoinColumn(name = "maHoaDon")
    private HoaDon hoaDon;

    private Integer soSao; // Số sao đánh giá, từ 1-5
    private String noiDung; // Nội dung đánh giá
    private LocalDateTime thoiGianDanhGia;
    private String adminReply; 
    
    public DanhGia() {}
	
	
	public DanhGia(Integer maDanhGia, SanPham sanPham, NguoiDung nguoiDung, HoaDon hoaDon, Integer soSao,
			String noiDung, LocalDateTime thoiGianDanhGia, String adminReply) {
		super();
		this.maDanhGia = maDanhGia;
		this.sanPham = sanPham;
		this.nguoiDung = nguoiDung;
		this.hoaDon = hoaDon;
		this.soSao = soSao;
		this.noiDung = noiDung;
		this.thoiGianDanhGia = thoiGianDanhGia;
		this.adminReply = adminReply;
	}


	public Integer getMaDanhGia() {
		return maDanhGia;
	}
	public void setMaDanhGia(Integer maDanhGia) {
		this.maDanhGia = maDanhGia;
	}
	public SanPham getSanPham() {
		return sanPham;
	}
	public void setSanPham(SanPham sanPham) {
		this.sanPham = sanPham;
	}
	public NguoiDung getNguoiDung() {
		return nguoiDung;
	}
	public void setNguoiDung(NguoiDung nguoiDung) {
		this.nguoiDung = nguoiDung;
	}
	public Integer getSoSao() {
		return soSao;
	}
	public void setSoSao(Integer soSao) {
		this.soSao = soSao;
	}
	public String getNoiDung() {
		return noiDung;
	}
	public void setNoiDung(String noiDung) {
		this.noiDung = noiDung;
	}
	public LocalDateTime getThoiGianDanhGia() {
		return thoiGianDanhGia;
	}
	public void setThoiGianDanhGia(LocalDateTime thoiGianDanhGia) {
		this.thoiGianDanhGia = thoiGianDanhGia;
	}

	public HoaDon getHoaDon() {
		return hoaDon;
	}

	public void setHoaDon(HoaDon hoaDon) {
		this.hoaDon = hoaDon;
	}


	public String getAdminReply() {
		return adminReply;
	}


	public void setAdminReply(String adminReply) {
		this.adminReply = adminReply;
	}
	
	
    
    

    // Getters và Setters
    // ...
}