package com.kimngan.ComesticAdmin.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="QuyenTruyCap")
public class QuyenTruyCap {

    @Id
    @Column(name="maQuyen")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maQuyen;

    private String tenQuyen;
    
    // quan hệ với bảng Người dùng
    @OneToMany(mappedBy = "quyenTruyCap")
	private Set<NguoiDung> nguoiDungs;

    // Constructors, Getters and Setters
    public QuyenTruyCap() {}
    

    public QuyenTruyCap(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }

    public Integer getMaQuyen() {
        return maQuyen;
    }

    public void setMaQuyen(Integer maQuyen) {
        this.maQuyen = maQuyen;
    }

    public String getTenQuyen() {
        return tenQuyen;
    }

    public void setTenQuyen(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }
}
