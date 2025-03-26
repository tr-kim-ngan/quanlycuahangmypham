package com.kimngan.ComesticAdmin.entity;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name="NguoiDung")
public class NguoiDung {

    @Id
    @Column(name="maNguoiDung")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNguoiDung;

    private String tenNguoiDung;
    private String hoTen;

    private String matKhau;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private String avatar;
    // quan hệ với bảng Quyền Truy Cập
    @ManyToOne
    @JoinColumn(name = "maQuyen", referencedColumnName ="maQuyen") // Khóa ngoại trỏ đến bảng QuyenTruyCap
    private QuyenTruyCap quyenTruyCap;
    @ManyToOne
    @JoinColumn(name = "maLoaiKhachHang", referencedColumnName = "maLoaiKhachHang")
    private LoaiKhachHang loaiKhachHang;
    
 // Quan hệ n-1 với PhieuGiamGia
    @ManyToOne
    @JoinColumn(name = "maPhieuGiamGia", referencedColumnName = "maPhieuGiamGia",nullable = true)
    private PhieuGiamGia phieuGiamGia;
    // Quan hệ Một-Nhiều với Giỏ Hàng
//    @OneToMany(mappedBy = "nguoiDung", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Set<GioHang> gioHangs;
    
    // Quan hệ với GioHang (Một người dùng có một giỏ hàng)
    @OneToOne(mappedBy = "nguoiDung", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GioHang gioHang;

    // Constructors, Getters and Setters
    public NguoiDung() {}    
  

	public NguoiDung(Integer maNguoiDung, String tenNguoiDung, String hoTen, String matKhau, String email,
			String soDienThoai, String diaChi, String avatar, QuyenTruyCap quyenTruyCap, LoaiKhachHang loaiKhachHang,
			PhieuGiamGia phieuGiamGia, GioHang gioHang) {
		super();
		this.maNguoiDung = maNguoiDung;
		this.tenNguoiDung = tenNguoiDung;
		this.hoTen = hoTen;
		this.matKhau = matKhau;
		this.email = email;
		this.soDienThoai = soDienThoai;
		this.diaChi = diaChi;
		this.avatar = avatar;
		this.quyenTruyCap = quyenTruyCap;
		this.loaiKhachHang = loaiKhachHang;
		this.phieuGiamGia = phieuGiamGia;
		this.gioHang = gioHang;
	}



	public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTenNguoiDung() {
        return tenNguoiDung;
    }

    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }
    public QuyenTruyCap getQuyenTruyCap() {
        return quyenTruyCap;
    }

    public void setQuyenTruyCap(QuyenTruyCap quyenTruyCap) {
        this.quyenTruyCap = quyenTruyCap;
    }
	public PhieuGiamGia getPhieuGiamGia() {
		return phieuGiamGia;
	}
	public void setPhieuGiamGia(PhieuGiamGia phieuGiamGia) {
		this.phieuGiamGia = phieuGiamGia;
	}

	public GioHang getGioHang() {
		return gioHang;
	}

	public void setGioHang(GioHang gioHang) {
		this.gioHang = gioHang;
	}

	public String getDiaChi() {
		return diaChi;
	}

	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}

	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public LoaiKhachHang getLoaiKhachHang() {
		return loaiKhachHang;
	}

	public void setLoaiKhachHang(LoaiKhachHang loaiKhachHang) {
		this.loaiKhachHang = loaiKhachHang;
	}


	public String getHoTen() {
		return hoTen;
	}


	public void setHoTen(String hoTen) {
		this.hoTen = hoTen;
	}
	
}
