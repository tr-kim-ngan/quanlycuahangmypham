package com.kimngan.ComesticAdmin.services;

import java.util.List;

import com.kimngan.ComesticAdmin.entity.NguoiDung;

public interface NguoiDungService {
	NguoiDung findByTenNguoiDung(String tenNguoiDung);

	void saveCustomer(NguoiDung nguoiDung);

	void save(NguoiDung nguoiDung);

	NguoiDung findById(Integer id);

	void updateLoaiKhachHangBasedOnTotalOrders(Integer maNguoiDung);

	long countCustomers();

	List<NguoiDung> findByRole(String role);

	void deleteById(Integer id);
	boolean existsByEmail(String email);
	boolean existsBySoDienThoai(String soDienThoai) ;
	boolean existsByEmailAndNotId(String email, Integer id);
	
	boolean existsBySoDienThoaiAndNotId(String soDienThoai, Integer id);
}
