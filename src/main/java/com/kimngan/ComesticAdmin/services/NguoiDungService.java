package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.NguoiDung;

public interface NguoiDungService {
	NguoiDung findByTenNguoiDung(String tenNguoiDung);
	void saveCustomer(NguoiDung nguoiDung);
	void save(NguoiDung nguoiDung);
	NguoiDung findById(Integer id);
	void updateLoaiKhachHangBasedOnTotalOrders(Integer maNguoiDung);
	long countCustomers();
}
