//package com.kimngan.ComesticAdmin.entity;
//
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.util.Objects;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Embeddable;
//
//@Embeddable
//
//public class DonGiaBanHangId implements Serializable {
//
//	@Column(name = "MaSanPham")
//	private Integer maSanPham;
//	
//	@Column(name = "NgayGio")
//	private LocalDateTime ngayGio;
//
//	public DonGiaBanHangId() {
//
//	}
//
//	public DonGiaBanHangId(Integer maSanPham, LocalDateTime ngayGio) {
//		super();
//		this.maSanPham = maSanPham;
//		this.ngayGio = ngayGio;
//	}
//
//	public Integer getMaSanPham() {
//		return maSanPham;
//	}
//
//	public void setMaSanPham(Integer maSanPham) {
//		this.maSanPham = maSanPham;
//	}
//
//	public LocalDateTime getNgayGio() {
//		return ngayGio;
//	}
//
//	public void setNgayGio(LocalDateTime ngayGio) {
//		this.ngayGio = ngayGio;
//	}
//
//	 @Override
//	    public boolean equals(Object o) {
//	        if (this == o) return true;
//	        if (!(o instanceof DonGiaBanHangId)) return false;
//	        DonGiaBanHangId that = (DonGiaBanHangId) o;
//	        return Objects.equals(maSanPham, that.maSanPham) &&
//	               Objects.equals(ngayGio, that.ngayGio);
//	    }
//
//	    @Override
//	    public int hashCode() {
//	        return Objects.hash(maSanPham, ngayGio);
//	    }
//
//}
