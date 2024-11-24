package com.kimngan.ComesticAdmin.entity;

import java.util.List;
import java.util.Set;

import jakarta.persistence.*;


@Entity
@Table
public class DonViTinh {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonVi")
    private Integer maDonVi;

    @Column(name = "TenDonVi")
    private String tenDonVi;

    @OneToMany(mappedBy = "donViTinh", cascade = CascadeType.ALL)
    private Set<SanPham> sanPhams;

    
    public DonViTinh() {}
    

	public DonViTinh(Integer maDonVi, String tenDonVi, Set<SanPham> sanPhams) {
		super();
		this.maDonVi = maDonVi;
		this.tenDonVi = tenDonVi;
		this.sanPhams = sanPhams;
	}


	public Integer getMaDonVi() {
		return maDonVi;
	}


	public void setMaDonVi(Integer maDonVi) {
		this.maDonVi = maDonVi;
	}


	public String getTenDonVi() {
		return tenDonVi;
	}


	public void setTenDonVi(String tenDonVi) {
		this.tenDonVi = tenDonVi;
	}


	public Set<SanPham> getSanPhams() {
		return sanPhams;
	}


	public void setSanPhams(Set<SanPham> sanPhams) {
		this.sanPhams = sanPhams;
	}
	
    
    
}
