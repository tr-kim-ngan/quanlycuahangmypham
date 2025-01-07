package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "DanhMuc")
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maDanhMuc")
    private Integer maDanhMuc;

    @Column(name = "tenDanhMuc", length = 100, nullable = false)
    private String tenDanhMuc;
 // Thuộc tính lưu đường dẫn hoặc tên ảnh
    @Column(name = "hinhAnh", length = 255)
    private String hinhAnh;
    
    // quan hệ với SanPham(One -to - Many)
    @OneToMany(mappedBy = "danhMuc")
    private Set<SanPham> sanPhams;
    
  
   
    public DanhMuc() {}
     
	public DanhMuc(Integer maDanhMuc, String tenDanhMuc, String hinhAnh, Set<SanPham> sanPhams) {
		super();
		this.maDanhMuc = maDanhMuc;
		this.tenDanhMuc = tenDanhMuc;
		this.hinhAnh = hinhAnh;
		this.sanPhams = sanPhams;
	}


	// Getters and Setters
    public Integer getMaDanhMuc() {
        return maDanhMuc;
    }

    public void setMaDanhMuc(Integer maDanhMuc) {
        this.maDanhMuc = maDanhMuc;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }
    
    public Set<SanPham> getSanPhams() {
		return sanPhams;
	}
    public void setSanPhams(Set<SanPham> sanPhams) {
		this.sanPhams = sanPhams;
	}

	public String getHinhAnh() {
		return hinhAnh;
	}

	public void setHinhAnh(String hinhAnh) {
		this.hinhAnh = hinhAnh;
	}
    
}
