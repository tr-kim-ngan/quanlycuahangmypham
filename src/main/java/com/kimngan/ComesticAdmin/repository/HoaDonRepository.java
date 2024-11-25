package com.kimngan.ComesticAdmin.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
	HoaDon findByDonHang(DonHang donHang);

	Page<HoaDon> findAll(Pageable pageable);

	@Query("SELECT h FROM HoaDon h WHERE h.donHang.nguoiDung.tenNguoiDung = :username")
	List<HoaDon> findByCustomerUsername(@Param("username") String username);


}