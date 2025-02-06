package com.kimngan.ComesticAdmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kimngan.ComesticAdmin.entity.NguoiDung;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {

	NguoiDung findByTenNguoiDung(String tenNguoiDung);

	// Optional<NguoiDung> findByTenNguoiDung(String tenNguoiDung);
	long countByQuyenTruyCap_TenQuyen(String tenQuyen);

	List<NguoiDung> findByQuyenTruyCap_TenQuyen(String tenQuyen);
	boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);
    boolean existsByEmailAndMaNguoiDungNot(String email, Integer id);

    boolean existsBySoDienThoaiAndMaNguoiDungNot(String soDienThoai, Integer id);
}
