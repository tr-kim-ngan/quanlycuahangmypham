package com.kimngan.ComesticAdmin.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maHoaDon;

    @OneToOne
    @JoinColumn(name = "maDonHang", nullable = false)
    private DonHang donHang;

    @Column(nullable = false)
    private LocalDateTime ngayXuatHoaDon;

    @Column(nullable = false)
    private BigDecimal tongTien;

    @Column(nullable = false)
    private String tenNguoiNhan;

    @Column(nullable = false)
    private String diaChiGiaoHang;

    @Column(nullable = false)
    private String soDienThoaiNhanHang;
    @Column(nullable = false)
    private String trangThaiThanhToan; 
    
    @Column(nullable = false) // Thêm phương thức thanh toán ✅
    private String phuongThucThanhToan;

    
    // Constructors
    public HoaDon() {
    }

    



	public HoaDon(Integer maHoaDon, DonHang donHang, LocalDateTime ngayXuatHoaDon, BigDecimal tongTien,
			String tenNguoiNhan, String diaChiGiaoHang, String soDienThoaiNhanHang, String trangThaiThanhToan,
			String phuongThucThanhToan) {
		super();
		this.maHoaDon = maHoaDon;
		this.donHang = donHang;
		this.ngayXuatHoaDon = ngayXuatHoaDon;
		this.tongTien = tongTien;
		this.tenNguoiNhan = tenNguoiNhan;
		this.diaChiGiaoHang = diaChiGiaoHang;
		this.soDienThoaiNhanHang = soDienThoaiNhanHang;
		this.trangThaiThanhToan = trangThaiThanhToan;
		this.phuongThucThanhToan = phuongThucThanhToan;
	}





	public Integer getMaHoaDon() {
		return maHoaDon;
	}

	public void setMaHoaDon(Integer maHoaDon) {
		this.maHoaDon = maHoaDon;
	}

	public DonHang getDonHang() {
		return donHang;
	}

	public void setDonHang(DonHang donHang) {
		this.donHang = donHang;
	}

	public LocalDateTime getNgayXuatHoaDon() {
		return ngayXuatHoaDon;
	}

	public void setNgayXuatHoaDon(LocalDateTime ngayXuatHoaDon) {
		this.ngayXuatHoaDon = ngayXuatHoaDon;
	}

	public BigDecimal getTongTien() {
		return tongTien;
	}

	public void setTongTien(BigDecimal tongTien) {
		this.tongTien = tongTien;
	}

	public String getTenNguoiNhan() {
		return tenNguoiNhan;
	}

	public void setTenNguoiNhan(String tenNguoiNhan) {
		this.tenNguoiNhan = tenNguoiNhan;
	}

	public String getDiaChiGiaoHang() {
		return diaChiGiaoHang;
	}

	public void setDiaChiGiaoHang(String diaChiGiaoHang) {
		this.diaChiGiaoHang = diaChiGiaoHang;
	}

	public String getSoDienThoaiNhanHang() {
		return soDienThoaiNhanHang;
	}

	public void setSoDienThoaiNhanHang(String soDienThoaiNhanHang) {
		this.soDienThoaiNhanHang = soDienThoaiNhanHang;
	}
	// toString method
    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon=" + maHoaDon +
                ", donHang=" + donHang +
                ", ngayXuatHoaDon=" + ngayXuatHoaDon +
                ", tongTien=" + tongTien +
                ", tenNguoiNhan='" + tenNguoiNhan + '\'' +
                ", diaChiGiaoHang='" + diaChiGiaoHang + '\'' +
                ", soDienThoaiNhanHang='" + soDienThoaiNhanHang + '\'' +
                '}';
    }

	public String getTrangThaiThanhToan() {
		return trangThaiThanhToan;
	}

	public void setTrangThaiThanhToan(String trangThaiThanhToan) {
		this.trangThaiThanhToan = trangThaiThanhToan;
	}

	public String getPhuongThucThanhToan() {
		return phuongThucThanhToan;
	}

	public void setPhuongThucThanhToan(String phuongThucThanhToan) {
		this.phuongThucThanhToan = phuongThucThanhToan;
	}
	
    

    
}

