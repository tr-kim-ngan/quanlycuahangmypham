package com.kimngan.ComesticAdmin.entity;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="PhieuGiamGia")
public class PhieuGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maPhieuGiamGia;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal soTienGiam;

    @Column(nullable = false)
    private Boolean trangThai;

    @OneToMany(mappedBy ="phieuGiamGia")
    private Set<NguoiDung> nguoiDungs;
    
 // One-to-One relationship with LoaiKhachHang
    @OneToOne
    @JoinColumn(name = "maLoaiKhachHang",referencedColumnName = "maLoaiKhachHang") // Foreign key column in PhieuGiamGia
    private LoaiKhachHang loaiKhachHang;
    
    
    public PhieuGiamGia() {}
    

	


	public PhieuGiamGia(Long maPhieuGiamGia, BigDecimal soTienGiam, Boolean trangThai, Set<NguoiDung> nguoiDungs,
			LoaiKhachHang loaiKhachHang) {
		super();
		this.maPhieuGiamGia = maPhieuGiamGia;
		this.soTienGiam = soTienGiam;
		this.trangThai = trangThai;
		this.nguoiDungs = nguoiDungs;
		this.loaiKhachHang = loaiKhachHang;
	}





	public Long getMaPhieuGiamGia() {
		return maPhieuGiamGia;
	}


	public void setMaPhieuGiamGia(Long maPhieuGiamGia) {
		this.maPhieuGiamGia = maPhieuGiamGia;
	}


	public BigDecimal getSoTienGiam() {
		return soTienGiam;
	}


	public void setSoTienGiam(BigDecimal soTienGiam) {
		this.soTienGiam = soTienGiam;
	}


	public Boolean getTrangThai() {
		return trangThai;
	}


	public void setTrangThai(Boolean trangThai) {
		this.trangThai = trangThai;
	}


	public Set<NguoiDung> getNguoiDungs() {
		return nguoiDungs;
	}

	public void setNguoiDungs(Set<NguoiDung> nguoiDungs) {
		this.nguoiDungs = nguoiDungs;
	}
	public LoaiKhachHang getLoaiKhachHang() {
		return loaiKhachHang;
	}

	public void setLoaiKhachHang(LoaiKhachHang loaiKhachHang) {
		this.loaiKhachHang = loaiKhachHang;
	}
    
    
    
    
    
    
}
