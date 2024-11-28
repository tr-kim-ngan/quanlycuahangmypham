package com.kimngan.ComesticAdmin.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

	List<DonHang> findByNguoiDung(NguoiDung nguoiDung);

	// Nếu cần thêm các query tùy chỉnh, có thể thêm ở đây
	List<DonHang> findByNguoiDungTenNguoiDung(String username);

	Optional<DonHang> findById(Integer maDonHang);

	Page<DonHang> findAll(Pageable pageable);

	@Query("SELECT SUM(d.tongGiaTriDonHang) FROM DonHang d WHERE d.nguoiDung.maNguoiDung = :maNguoiDung AND d.trangThaiDonHang = 'Đã hoàn thành'")
	BigDecimal findTotalOrderValueByNguoiDung(@Param("maNguoiDung") Integer maNguoiDung);
	List<DonHang> findAllByOrderByNgayDatAsc();
	Page<DonHang> findByTrangThaiDonHang(String trangThaiDonHang, Pageable pageable);
	long countByTrangThaiDonHang(String trangThaiDonHang);


}