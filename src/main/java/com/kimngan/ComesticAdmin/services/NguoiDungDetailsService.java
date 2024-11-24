package com.kimngan.ComesticAdmin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class NguoiDungDetailsService implements UserDetailsService {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm người dùng trong cơ sở dữ liệu
        NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);
        if (nguoiDung == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng: " + username);
        }

        // Lấy các quyền truy cập (roles) của người dùng
        Set<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        
        grantedAuthoritySet.add(new SimpleGrantedAuthority(nguoiDung.getQuyenTruyCap().getTenQuyen()));

        // Trả về đối tượng CustomUserDetails
        return new NguoiDungDetails(nguoiDung, grantedAuthoritySet);
    }
}
