package com.kimngan.ComesticAdmin.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class NguoiDungDetails implements UserDetails{
	private NguoiDung nguoiDung;
	private Collection<? extends GrantedAuthority> authorities;
	
	// tạo hàm xây dựng
	public NguoiDungDetails() {
		// TODO Auto-generated constructor stub
	}
	
	
	public NguoiDungDetails(NguoiDung nguoiDung, Collection<? extends GrantedAuthority> authorities) {
		super();
		this.nguoiDung = nguoiDung;
		this.authorities = authorities;
	}
	// Getter cho NguoiDung
	public NguoiDung getNguoiDung() {
		return nguoiDung;
	}

	// Getter cho QuyenTruyCap từ NguoiDung
	public QuyenTruyCap getQuyenTruyCap() {
		return nguoiDung.getQuyenTruyCap();
	}



	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return authorities;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return nguoiDung.getMatKhau();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return nguoiDung.getTenNguoiDung();
	}
	
	@Override
    public boolean isAccountNonExpired() {
        return true;  // Giả định tài khoản không bao giờ hết hạn
    }
	
	 @Override
	    public boolean isAccountNonLocked() {
	        return true;  // Giả định tài khoản không bị khóa
	 }
	
	 @Override
	    public boolean isCredentialsNonExpired() {
	        return true;  // Giả định thông tin đăng nhập không hết hạn
	   }
	 
	@Override
    public boolean isEnabled() {
        return true;  // Giả định tài khoản được kích hoạt
    }

}
