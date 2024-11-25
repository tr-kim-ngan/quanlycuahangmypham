package com.kimngan.ComesticAdmin.services;


import com.kimngan.ComesticAdmin.entity.LoaiKhachHang;
import com.kimngan.ComesticAdmin.repository.LoaiKhachHangRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoaiKhachHangServiceImpl implements LoaiKhachHangService {

    @Autowired
    private LoaiKhachHangRepository loaiKhachHangRepository;

	@Override
	public List<LoaiKhachHang> getAllLoaiKhachHang() {
		// TODO Auto-generated method stub
		  return loaiKhachHangRepository.findAll();
	}

	@Override
	public LoaiKhachHang getLoaiKhachHangById(Integer id) {
		// TODO Auto-generated method stub
		return loaiKhachHangRepository.findById(id).orElse(null);
	}

	@Override
	public LoaiKhachHang saveLoaiKhachHang(LoaiKhachHang loaiKhachHang) {
		// TODO Auto-generated method stub
		 return loaiKhachHangRepository.save(loaiKhachHang);
	}

	@Override
	public void deleteLoaiKhachHang(Integer id) {
		// TODO Auto-generated method stub
		 loaiKhachHangRepository.deleteById(id);
	}

	@PostConstruct
	@Override
	public void initializeDefaultLoaiKhachHang() {
	    if (loaiKhachHangRepository.count() == 0) {
	        LoaiKhachHang bac = new LoaiKhachHang("Bạc", "Khách hàng mặc định khi vừa đăng ký");
	        LoaiKhachHang vang = new LoaiKhachHang("Vàng", "Khách hàng có tổng giá trị đơn hàng trên 1 triệu");
	        LoaiKhachHang kimCuong = new LoaiKhachHang("Kim cương", "Khách hàng có tổng giá trị đơn hàng trên 2 triệu");

	        loaiKhachHangRepository.save(bac);
	        loaiKhachHangRepository.save(vang);
	        loaiKhachHangRepository.save(kimCuong);
	    }
	}

	

    
}
