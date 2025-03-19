package com.kimngan.ComesticAdmin.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name = "KiemKeKho")
public class KiemKeKho {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maKiemKe;

    @ManyToOne
    @JoinColumn(name = "maSanPham", nullable = false)
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "maLichSu", nullable = false)
    private LichSuCaLamViec lichSuCaLamViec;

    @Column(nullable = false)
    private Integer soLuongTruocKiemKe;

    @Column(nullable = false)
    private Integer soLuongSauKiemKe;

    @Column(nullable = false)
    private String lyDoDieuChinh;
    @Column(nullable = false)
    private boolean coThayDoi;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean daXetDuyet = false; // Admin xét duyệt hay chưa
    @Column(nullable = false)
    private Integer soLuongHeThongLucKiemKe;
    @ManyToOne
    @JoinColumn(name = "maNguoiKiemKe", nullable = false)
    private NguoiDung nguoiKiemKe; // Người kiểm kê

    @Column(nullable = false)
    private LocalDateTime thoiGianKiemKe = LocalDateTime.now();
    
    

   
    public KiemKeKho() {
    	
    }
   
	




	public KiemKeKho(Integer maKiemKe, SanPham sanPham, LichSuCaLamViec lichSuCaLamViec, Integer soLuongTruocKiemKe,
			Integer soLuongSauKiemKe, String lyDoDieuChinh, boolean coThayDoi, Boolean daXetDuyet,
			Integer soLuongHeThongLucKiemKe, NguoiDung nguoiKiemKe, LocalDateTime thoiGianKiemKe) {
		super();
		this.maKiemKe = maKiemKe;
		this.sanPham = sanPham;
		this.lichSuCaLamViec = lichSuCaLamViec;
		this.soLuongTruocKiemKe = soLuongTruocKiemKe;
		this.soLuongSauKiemKe = soLuongSauKiemKe;
		this.lyDoDieuChinh = lyDoDieuChinh;
		this.coThayDoi = coThayDoi;
		this.daXetDuyet = daXetDuyet;
		this.soLuongHeThongLucKiemKe = soLuongHeThongLucKiemKe;
		this.nguoiKiemKe = nguoiKiemKe;
		this.thoiGianKiemKe = thoiGianKiemKe;
	}






	public Integer getMaKiemKe() {
		return maKiemKe;
	}

	public void setMaKiemKe(Integer maKiemKe) {
		this.maKiemKe = maKiemKe;
	}

	public SanPham getSanPham() {
		return sanPham;
	}

	public void setSanPham(SanPham sanPham) {
		this.sanPham = sanPham;
	}

	public LichSuCaLamViec getLichSuCaLamViec() {
		return lichSuCaLamViec;
	}

	public void setLichSuCaLamViec(LichSuCaLamViec lichSuCaLamViec) {
		this.lichSuCaLamViec = lichSuCaLamViec;
	}

	public Integer getSoLuongTruocKiemKe() {
		return soLuongTruocKiemKe;
	}

	public void setSoLuongTruocKiemKe(Integer soLuongTruocKiemKe) {
		this.soLuongTruocKiemKe = soLuongTruocKiemKe;
	}

	public Integer getSoLuongSauKiemKe() {
		return soLuongSauKiemKe;
	}

	public void setSoLuongSauKiemKe(Integer soLuongSauKiemKe) {
		this.soLuongSauKiemKe = soLuongSauKiemKe;
	}

	public String getLyDoDieuChinh() {
		return lyDoDieuChinh;
	}

	public void setLyDoDieuChinh(String lyDoDieuChinh) {
		this.lyDoDieuChinh = lyDoDieuChinh;
	}

	public Boolean getDaXetDuyet() {
		return daXetDuyet;
	}

	public void setDaXetDuyet(Boolean daXetDuyet) {
		this.daXetDuyet = daXetDuyet;
	}

	public NguoiDung getNguoiKiemKe() {
		return nguoiKiemKe;
	}

	public void setNguoiKiemKe(NguoiDung nguoiKiemKe) {
		this.nguoiKiemKe = nguoiKiemKe;
	}

	public LocalDateTime getThoiGianKiemKe() {
		return thoiGianKiemKe;
	}

	public void setThoiGianKiemKe(LocalDateTime thoiGianKiemKe) {
		this.thoiGianKiemKe = thoiGianKiemKe;
	}

	public boolean isCoThayDoi() {
		return coThayDoi;
	}

	public void setCoThayDoi(boolean coThayDoi) {
		this.coThayDoi = coThayDoi;
	}



	public Integer getSoLuongHeThongLucKiemKe() {
		return soLuongHeThongLucKiemKe;
	}



	public void setSoLuongHeThongLucKiemKe(Integer soLuongHeThongLucKiemKe) {
		this.soLuongHeThongLucKiemKe = soLuongHeThongLucKiemKe;
	}


 
    
}
