package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import java.util.Set;

@Entity
@Table(name = "SanPham")
public class SanPham {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MaSanPham")
	private Integer maSanPham;

	@Column(name = "TenSanPham", length = 200, nullable = false)
	private String tenSanPham;

	@Column(name = "MoTa", columnDefinition = "TEXT",nullable = false)
	private String moTa;

	@Column(name = "HinhAnh", length = 255,nullable = false)
	private String hinhAnh;

	@Column(name = "SoLuong",nullable = false)
	private Integer soLuong;
	
	
	@Column(name = "TrangThai", nullable = false)
    private boolean trangThai = true; 
	
	
	@Column(name = "DonGiaBan", precision = 20, scale = 2, nullable = false)
	private BigDecimal donGiaBan;
	
	@OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL)
	private List<DanhGia> danhGias;

	// quan hệ với bảng Danh Mục
	@ManyToOne
	@JoinColumn(name = "maDanhMuc", referencedColumnName = "maDanhMuc")
	private DanhMuc danhMuc;
	// quan hệ với Yêu thích

	@OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<YeuThich> yeuThichs;

	
	// quan hệ với bảng Chi tiết đơn nhập hàng
	@OneToMany(mappedBy = "sanPham",cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
	private Set<ChiTietDonNhapHang> chiTietDonNhapHangs;

	
	
	
	

	// Nhà cung cấp
	@ManyToMany
	@JoinTable(name = "SanPham_NhaCungCap", // Tên của bảng trung gian
			joinColumns = @JoinColumn(name = "maSanPham"), // Khóa ngoại từ bảng SanPham
			inverseJoinColumns = @JoinColumn(name = "maNhaCungCap") // Khóa ngoại từ bảng NhaCungCap
	)
	private Set<NhaCungCap> nhaCungCaps;

	@ManyToMany
	@JoinTable(name = "SanPhamCoKhuyenMai", // Tên của bảng trung gian
	        joinColumns = @JoinColumn(name = "maSanPham"), // Khóa ngoại từ bảng SanPham
	        inverseJoinColumns = @JoinColumn(name = "maKhuyenMai") // Khóa ngoại từ bảng KhuyenMai
	)
	private Set<KhuyenMai> khuyenMais;

	
	// quan hệ với bảng đơn vị tính
	@ManyToOne
    @JoinColumn(name = "MaDonVi", nullable = false)
    private DonViTinh donViTinh;
 
	@OneToMany(mappedBy = "sanPham")
	private Set<ChiTietGioHang> chiTietGioHangs;


	public SanPham() {

	}

	


	public SanPham(Integer maSanPham, String tenSanPham, String moTa, String hinhAnh, Integer soLuong,
			boolean trangThai, BigDecimal donGiaBan, List<DanhGia> danhGias, DanhMuc danhMuc, Set<YeuThich> yeuThichs,
			Set<ChiTietDonNhapHang> chiTietDonNhapHangs, Set<NhaCungCap> nhaCungCaps, Set<KhuyenMai> khuyenMais,
			DonViTinh donViTinh, Set<ChiTietGioHang> chiTietGioHangs) {
		super();
		this.maSanPham = maSanPham;
		this.tenSanPham = tenSanPham;
		this.moTa = moTa;
		this.hinhAnh = hinhAnh;
		this.soLuong = soLuong;
		this.trangThai = trangThai;
		this.donGiaBan = donGiaBan;
		this.danhGias = danhGias;
		this.danhMuc = danhMuc;
		this.yeuThichs = yeuThichs;
		this.chiTietDonNhapHangs = chiTietDonNhapHangs;
		this.nhaCungCaps = nhaCungCaps;
		this.khuyenMais = khuyenMais;
		this.donViTinh = donViTinh;
		this.chiTietGioHangs = chiTietGioHangs;
	}




	public Integer getMaSanPham() {
		return maSanPham;
	}


	public void setMaSanPham(Integer maSanPham) {
		this.maSanPham = maSanPham;
	}

	public String getTenSanPham() {
		return tenSanPham;
	}

	public void setTenSanPham(String tenSanPham) {
		this.tenSanPham = tenSanPham;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}

	public String getHinhAnh() {
		return hinhAnh;
	}

	public void setHinhAnh(String hinhAnh) {
		this.hinhAnh = hinhAnh;
	}

	public Integer getSoLuong() {
		return soLuong;
	}

	public void setSoLuong(Integer soLuong) {
		this.soLuong = soLuong;
	}

	public DanhMuc getDanhMuc() {
		return danhMuc;
	}

	public void setDanhMuc(DanhMuc danhMuc) {
		this.danhMuc = danhMuc;
	}



	public BigDecimal getDonGiaBan() {
		return donGiaBan;
	}



	public void setDonGiaBan(BigDecimal donGiaBan) {
		this.donGiaBan = donGiaBan;
	}



	public Set<NhaCungCap> getNhaCungCaps() {
		return nhaCungCaps;
	}

	public void setNhaCungCaps(Set<NhaCungCap> nhaCungCaps) {
		this.nhaCungCaps = nhaCungCaps;
	}

	public Set<ChiTietDonNhapHang> getChiTietDonNhapHangs() {
		return chiTietDonNhapHangs;
	}

	public void setChiTietDonNhapHangs(Set<ChiTietDonNhapHang> chiTietDonNhapHangs) {
		this.chiTietDonNhapHangs = chiTietDonNhapHangs;
	}

	public DonViTinh getDonViTinh() {
		return donViTinh;
	}

	public void setDonViTinh(DonViTinh donViTinh) {
		this.donViTinh = donViTinh;
	}

	public boolean isTrangThai() {
		return trangThai;
	}

	public void setTrangThai(boolean trangThai) {
		this.trangThai = trangThai;
	}



	public Set<KhuyenMai> getKhuyenMais() {
		return khuyenMais;
	}

	public void setKhuyenMais(Set<KhuyenMai> khuyenMais) {
		this.khuyenMais = khuyenMais;
	}
	public Set<YeuThich> getYeuThichs() {
		return yeuThichs;
	}

	public void setYeuThichs(Set<YeuThich> yeuThichs) {
		this.yeuThichs = yeuThichs;
	}

	public Set<ChiTietGioHang> getChiTietGioHangs() {
		return chiTietGioHangs;
	}

	public void setChiTietGioHangs(Set<ChiTietGioHang> chiTietGioHangs) {
		this.chiTietGioHangs = chiTietGioHangs;
	}

	public List<DanhGia> getDanhGias() {
		return danhGias;
	}

	public void setDanhGias(List<DanhGia> danhGias) {
		this.danhGias = danhGias;
	}

}
