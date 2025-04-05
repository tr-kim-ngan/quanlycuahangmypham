package com.kimngan.ComesticAdmin.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kimngan.ComesticAdmin.entity.NguoiDung;

public interface NguoiDungService {
	NguoiDung findByTenNguoiDung(String tenNguoiDung);
	NguoiDung findByEmail(String email);
	boolean existsByTenNguoiDung(String tenNguoiDung);
	boolean existsByTenNguoiDungAndNotId(String tenNguoiDung, Integer id);

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
	 Optional<NguoiDung> findBySoDienThoai(String phone);
	 NguoiDung findFirstByRole(int maQuyen);
	 List<NguoiDung> findByRole(int maQuyen);


}




