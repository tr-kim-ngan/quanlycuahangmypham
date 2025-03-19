package com.kimngan.ComesticAdmin.entity;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
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
@Table(name = "LichSuCaLamViec")
public class LichSuCaLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maLichSu;

    @ManyToOne
    @JoinColumn(name = "maNhanVien", nullable = false)
    private NguoiDung nhanVien;

    @Column(nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(nullable = true)
    private LocalDateTime thoiGianKetThuc;
    @Column(nullable = true)
    private String ghiChu;

    @OneToMany(mappedBy = "lichSuCaLamViec", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KiemKeKho> danhSachKiemKe;
  
    public LichSuCaLamViec() {}
    
    
	
	public LichSuCaLamViec(Integer maLichSu, NguoiDung nhanVien, LocalDateTime thoiGianBatDau,
			LocalDateTime thoiGianKetThuc, String ghiChu, List<KiemKeKho> danhSachKiemKe) {
		super();
		this.maLichSu = maLichSu;
		this.nhanVien = nhanVien;
		this.thoiGianBatDau = thoiGianBatDau;
		this.thoiGianKetThuc = thoiGianKetThuc;
		this.ghiChu = ghiChu;
		this.danhSachKiemKe = danhSachKiemKe;
	}



	public Integer getMaLichSu() {
		return maLichSu;
	}

	public void setMaLichSu(Integer maLichSu) {
		this.maLichSu = maLichSu;
	}

	public NguoiDung getNhanVien() {
		return nhanVien;
	}

	public void setNhanVien(NguoiDung nhanVien) {
		this.nhanVien = nhanVien;
	}

	public LocalDateTime getThoiGianBatDau() {
		return thoiGianBatDau;
	}

	public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
		this.thoiGianBatDau = thoiGianBatDau;
	}

	public LocalDateTime getThoiGianKetThuc() {
		return thoiGianKetThuc;
	}

	public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
		this.thoiGianKetThuc = thoiGianKetThuc;
	}

	public List<KiemKeKho> getDanhSachKiemKe() {
		return danhSachKiemKe;
	}

	public void setDanhSachKiemKe(List<KiemKeKho> danhSachKiemKe) {
		this.danhSachKiemKe = danhSachKiemKe;
	}



	public String getGhiChu() {
		return ghiChu;
	}



	public void setGhiChu(String ghiChu) {
		this.ghiChu = ghiChu;
	}

    
   
    
    
    
    
}

