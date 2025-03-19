package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "GioHang")
public class GioHang {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MaGioHang")
	private Integer maGioHang;
	@Column(name = "ngayTao", nullable = false)
	private LocalDate ngayTao;
	
//    @ManyToOne
//    @JoinColumn(name = "maNguoiDung", nullable = false)
//    private NguoiDung nguoiDung;
	
	 // Quan hệ với NguoiDung (Một giỏ hàng thuộc về một người dùng)
    @OneToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;
    
 // Quan hệ với ChiTietGioHang
    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChiTietGioHang> chiTietGioHangs;
   
    
    
    
    public GioHang() {}
	
	public GioHang(Integer maGioHang, LocalDate ngayTao, NguoiDung nguoiDung, Set<ChiTietGioHang> chiTietGioHangs) {
		super();
		this.maGioHang = maGioHang;
		this.ngayTao = ngayTao;
		this.nguoiDung = nguoiDung;
		this.chiTietGioHangs = chiTietGioHangs;
	}

	public Integer getMaGioHang() {
		return maGioHang;
	}
	public void setMaGioHang(Integer maGioHang) {
		this.maGioHang = maGioHang;
	}
	public LocalDate getNgayTao() {
		return ngayTao;
	}
	public void setNgayTao(LocalDate ngayTao) {
		this.ngayTao = ngayTao;
	}
	
	public NguoiDung getNguoiDung() {
		return nguoiDung;
	}

	public void setNguoiDung(NguoiDung nguoiDung) {
		this.nguoiDung = nguoiDung;
	}

	public Set<ChiTietGioHang> getChiTietGioHangs() {
		return chiTietGioHangs;
	}
	public void setChiTietGioHangs(Set<ChiTietGioHang> chiTietGioHangs) {
		this.chiTietGioHangs = chiTietGioHangs;
	}
    
}
