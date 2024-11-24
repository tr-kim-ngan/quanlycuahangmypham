package com.kimngan.ComesticAdmin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.QuyenTruyCap;
import com.kimngan.ComesticAdmin.repository.NguoiDungRepository;
import com.kimngan.ComesticAdmin.repository.QuyenTruyCapRepository;
@Service
public class NguoiDungServiceImpl implements NguoiDungService{
	@Autowired
	private NguoiDungRepository nguoiDungRepository;
	 @Autowired
	 private QuyenTruyCapRepository quyenTruyCapRepository;
	@Override
	public NguoiDung findByTenNguoiDung(String tenNguoiDung) {
		// TODO Auto-generated method stub
		return nguoiDungRepository.findByTenNguoiDung(tenNguoiDung);
	}
	@Override
	public void saveCustomer(NguoiDung nguoiDung) {
		 // Lấy quyền CUSTOMER từ cơ sở dữ liệu
        QuyenTruyCap quyenCustomer = quyenTruyCapRepository.findByTenQuyen("CUSTOMER");
        
        // Gán quyền CUSTOMER cho người dùng mới
        nguoiDung.setQuyenTruyCap(quyenCustomer);

        // Lưu người dùng vào cơ sở dữ liệu
        nguoiDungRepository.save(nguoiDung);
		
	}
	@Override
	public void save(NguoiDung nguoiDung) {
		// TODO Auto-generated method stub
		  nguoiDungRepository.save(nguoiDung);
	}
	@Override
	public NguoiDung findById(Integer id) {
		// TODO Auto-generated method stub
		 return nguoiDungRepository.findById(id).orElse(null);
	}

}
