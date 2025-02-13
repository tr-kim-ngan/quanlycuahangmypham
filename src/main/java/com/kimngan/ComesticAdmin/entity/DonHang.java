package com.kimngan.ComesticAdmin.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "DonHang")
public class DonHang {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MaDonHang")
	private Integer maDonHang;

	@Column(name = "NgayDat", nullable = false)
	private LocalDateTime ngayDat;

	@Column(name = "DiaChiGiaoHang", nullable = false, length = 255)
	private String diaChiGiaoHang;

	@Column(name = "TrangThaiDonHang", nullable = false, length = 50)
	private String trangThaiDonHang;

	@Column(name = "TrangThaiChoXacNhan", nullable = true, length = 50)
	private String trangThaiChoXacNhan;

	@Column(name = "TongGiaTriDonHang", precision = 15, scale = 2, nullable = false)
	private BigDecimal tongGiaTriDonHang;

	@Column(name = "PhiVanChuyen", precision = 8, scale = 2, nullable = false)
	private BigDecimal phiVanChuyen;
	@Column(name = "SoLanGiaoThatBai", nullable = false)
	private Integer soLanGiaoThatBai = 0;

	@Column(name = "GhiChu", length = 225)
	private String ghiChu;
	@Column(name = "HinhAnhGiaoHang", length = 255, nullable = true)
	private String hinhAnhGiaoHang;

	@Column(name = "SDTNhanHang", length = 10, nullable = false)
	private String sdtNhanHang;
	@Column(name = "lich_su_trang_thai", columnDefinition = "TEXT")
	private String lichSuTrangThai;

	// Quan hệ với bảng NguoiDung
	@ManyToOne
	@JoinColumn(name = "MaNguoiDung", nullable = false)
	private NguoiDung nguoiDung;

	// ✅ Quan hệ với shipper (mới thêm)
	@ManyToOne
	@JoinColumn(name = "ShipperId", referencedColumnName = "maNguoiDung", nullable = true)
	private NguoiDung shipper; // Thêm shipper vào đơn hàng

	// Quan hệ với ChiTietDonHang
	@OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ChiTietDonHang> chiTietDonHangs = new ArrayList<>();

	// Constructor
	public DonHang() {
	}



	public DonHang(Integer maDonHang, LocalDateTime ngayDat, String diaChiGiaoHang, String trangThaiDonHang,
			String trangThaiChoXacNhan, BigDecimal tongGiaTriDonHang, BigDecimal phiVanChuyen, Integer soLanGiaoThatBai,
			String ghiChu, String hinhAnhGiaoHang, String sdtNhanHang, String lichSuTrangThai, NguoiDung nguoiDung,
			NguoiDung shipper, List<ChiTietDonHang> chiTietDonHangs) {
		super();
		this.maDonHang = maDonHang;
		this.ngayDat = ngayDat;
		this.diaChiGiaoHang = diaChiGiaoHang;
		this.trangThaiDonHang = trangThaiDonHang;
		this.trangThaiChoXacNhan = trangThaiChoXacNhan;
		this.tongGiaTriDonHang = tongGiaTriDonHang;
		this.phiVanChuyen = phiVanChuyen;
		this.soLanGiaoThatBai = soLanGiaoThatBai;
		this.ghiChu = ghiChu;
		this.hinhAnhGiaoHang = hinhAnhGiaoHang;
		this.sdtNhanHang = sdtNhanHang;
		this.lichSuTrangThai = lichSuTrangThai;
		this.nguoiDung = nguoiDung;
		this.shipper = shipper;
		this.chiTietDonHangs = chiTietDonHangs;
	}



	// Getters và Setters
	public Integer getMaDonHang() {
		return maDonHang;
	}

	public void setMaDonHang(Integer maDonHang) {
		this.maDonHang = maDonHang;
	}

	public LocalDateTime getNgayDat() {
		return ngayDat;
	}

	public void setNgayDat(LocalDateTime ngayDat) {
		this.ngayDat = ngayDat;
	}

	public String getDiaChiGiaoHang() {
		return diaChiGiaoHang;
	}

	public void setDiaChiGiaoHang(String diaChiGiaoHang) {
		this.diaChiGiaoHang = diaChiGiaoHang;
	}

	public String getTrangThaiDonHang() {
		return trangThaiDonHang;
	}

	public void setTrangThaiDonHang(String trangThaiDonHang) {
		this.trangThaiDonHang = trangThaiDonHang;
	}

	public BigDecimal getTongGiaTriDonHang() {
		return tongGiaTriDonHang;
	}

	public void setTongGiaTriDonHang(BigDecimal tongGiaTriDonHang) {
		this.tongGiaTriDonHang = tongGiaTriDonHang;
	}

	public BigDecimal getPhiVanChuyen() {
		return phiVanChuyen;
	}

	public void setPhiVanChuyen(BigDecimal phiVanChuyen) {
		this.phiVanChuyen = phiVanChuyen;
	}

	public String getGhiChu() {
		return ghiChu;
	}

	public void setGhiChu(String ghiChu) {
		this.ghiChu = ghiChu;
	}

	public String getSdtNhanHang() {
		return sdtNhanHang;
	}

	public void setSdtNhanHang(String sdtNhanHang) {
		this.sdtNhanHang = sdtNhanHang;
	}

	public NguoiDung getNguoiDung() {
		return nguoiDung;
	}

	public void setNguoiDung(NguoiDung nguoiDung) {
		this.nguoiDung = nguoiDung;
	}

	public List<ChiTietDonHang> getChiTietDonHangs() {
		return chiTietDonHangs;
	}

	public void setChiTietDonHangs(List<ChiTietDonHang> chiTietDonHangs) {
		this.chiTietDonHangs = chiTietDonHangs;
	}

	public NguoiDung getShipper() {
		return shipper;
	}

	public void setShipper(NguoiDung shipper) {
		this.shipper = shipper;
	}

	public String getTrangThaiChoXacNhan() {
		return trangThaiChoXacNhan;
	}

	public void setTrangThaiChoXacNhan(String trangThaiChoXacNhan) {
		this.trangThaiChoXacNhan = trangThaiChoXacNhan;
	}

	public String getHinhAnhGiaoHang() {
		return hinhAnhGiaoHang;
	}

	public void setHinhAnhGiaoHang(String hinhAnhGiaoHang) {
		this.hinhAnhGiaoHang = hinhAnhGiaoHang;
	}

	public Integer getSoLanGiaoThatBai() {
		return soLanGiaoThatBai;
	}

	public void setSoLanGiaoThatBai(Integer soLanGiaoThatBai) {
		this.soLanGiaoThatBai = soLanGiaoThatBai;
	}

	public String getLichSuTrangThai() {
		return lichSuTrangThai;
	}

	public void setLichSuTrangThai(String lichSuTrangThai) {
		this.lichSuTrangThai = lichSuTrangThai;
	}
	
	

}
