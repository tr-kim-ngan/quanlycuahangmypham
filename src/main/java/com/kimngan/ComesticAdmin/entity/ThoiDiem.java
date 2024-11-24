//package com.kimngan.ComesticAdmin.entity;
//
//import java.time.LocalDateTime;
//import java.util.Set;
//
//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.Id;
//import jakarta.persistence.OneToMany;
//import jakarta.persistence.Table;
//
//@Entity
//@Table(name = "ThoiDiem")
//public class ThoiDiem {
//
//    @Id
//    @Column(name = "NgayGio", nullable = false)
//    private LocalDateTime ngayGio;
//    
//    @OneToMany(mappedBy = "thoiDiem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Set<DonGiaBanHang> donGiaBanHangs;
//
//    
//    // xâu dựng
//    public  ThoiDiem() {
//	
//		
//	}
//    public ThoiDiem(LocalDateTime ngayGio) {
//        this.ngayGio = ngayGio;
//    }
//	public ThoiDiem(LocalDateTime ngayGio, Set<DonGiaBanHang> donGiaBanHangs) {
//		super();
//		this.ngayGio = ngayGio;
//		this.donGiaBanHangs = donGiaBanHangs;
//	}
//    
//    // getters and setters
//	public LocalDateTime getNgayGio() {
//        return ngayGio;
//    }
//
//    public void setNgayGio(LocalDateTime ngayGio) {
//        this.ngayGio = ngayGio;
//    }
//
//    public Set<DonGiaBanHang> getDonGiaBanHangs() {
//        return donGiaBanHangs;
//    }
//
//    public void setDonGiaBanHangs(Set<DonGiaBanHang> donGiaBanHangs) {
//        this.donGiaBanHangs = donGiaBanHangs;
//    }
//    
//
//    
//    
//}
