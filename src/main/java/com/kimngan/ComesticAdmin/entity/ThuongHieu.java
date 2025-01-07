package com.kimngan.ComesticAdmin.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "ThuongHieu")
public class ThuongHieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maThuongHieu")
    private Integer maThuongHieu;

    @Column(name = "tenThuongHieu", length = 100, nullable = false)
    private String tenThuongHieu;

    // Thuộc tính lưu đường dẫn hoặc tên ảnh
    @Column(name = "hinhAnh", length = 255)
    private String hinhAnh;

    // Quan hệ với SanPham (One-to-Many)
    @OneToMany(mappedBy = "thuongHieu")
    private Set<SanPham> sanPhams;

    public ThuongHieu() {}

    public ThuongHieu(Integer maThuongHieu, String tenThuongHieu, String hinhAnh, Set<SanPham> sanPhams) {
        this.maThuongHieu = maThuongHieu;
        this.tenThuongHieu = tenThuongHieu;
        this.hinhAnh = hinhAnh;
        this.sanPhams = sanPhams;
    }

    // Getters and Setters
    public Integer getMaThuongHieu() {
        return maThuongHieu;
    }

    public void setMaThuongHieu(Integer maThuongHieu) {
        this.maThuongHieu = maThuongHieu;
    }

    public String getTenThuongHieu() {
        return tenThuongHieu;
    }

    public void setTenThuongHieu(String tenThuongHieu) {
        this.tenThuongHieu = tenThuongHieu;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public Set<SanPham> getSanPhams() {
        return sanPhams;
    }

    public void setSanPhams(Set<SanPham> sanPhams) {
        this.sanPhams = sanPhams;
    }
}
