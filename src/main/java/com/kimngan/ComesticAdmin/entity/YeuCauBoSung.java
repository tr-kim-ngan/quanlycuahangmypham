package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;
@Entity
public class YeuCauBoSung {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ma_san_pham")
    private SanPham sanPham;

    private Integer soLuongYeuCau;

    private LocalDateTime thoiGianYeuCau;

    @ManyToOne
    @JoinColumn(name = "ma_nguoi_dung")
    private NguoiDung nguoiYeuCau;

    private Boolean daXuLy = false;

    public YeuCauBoSung() {}
    
	public YeuCauBoSung(Integer id, SanPham sanPham, Integer soLuongYeuCau, LocalDateTime thoiGianYeuCau,
			NguoiDung nguoiYeuCau, Boolean daXuLy) {
		super();
		this.id = id;
		this.sanPham = sanPham;
		this.soLuongYeuCau = soLuongYeuCau;
		this.thoiGianYeuCau = thoiGianYeuCau;
		this.nguoiYeuCau = nguoiYeuCau;
		this.daXuLy = daXuLy;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SanPham getSanPham() {
		return sanPham;
	}

	public void setSanPham(SanPham sanPham) {
		this.sanPham = sanPham;
	}

	public Integer getSoLuongYeuCau() {
		return soLuongYeuCau;
	}

	public void setSoLuongYeuCau(Integer soLuongYeuCau) {
		this.soLuongYeuCau = soLuongYeuCau;
	}

	public LocalDateTime getThoiGianYeuCau() {
		return thoiGianYeuCau;
	}

	public void setThoiGianYeuCau(LocalDateTime thoiGianYeuCau) {
		this.thoiGianYeuCau = thoiGianYeuCau;
	}

	public NguoiDung getNguoiYeuCau() {
		return nguoiYeuCau;
	}

	public void setNguoiYeuCau(NguoiDung nguoiYeuCau) {
		this.nguoiYeuCau = nguoiYeuCau;
	}

	public Boolean getDaXuLy() {
		return daXuLy;
	}

	public void setDaXuLy(Boolean daXuLy) {
		this.daXuLy = daXuLy;
	}

	
	

    
}