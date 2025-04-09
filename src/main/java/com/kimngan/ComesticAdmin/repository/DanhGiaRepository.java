package com.kimngan.ComesticAdmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {
	List<DanhGia> findBySanPham(SanPham sanPham);

	List<DanhGia> findByNguoiDung(NguoiDung nguoiDung);

	List<DanhGia> findByHoaDonAndNguoiDung(HoaDon maHoaDon, NguoiDung maNguoiDung);

	// Thay đổi từ Integer sang các đối tượng liên quan
	boolean existsByHoaDonAndNguoiDung(HoaDon hoaDon, NguoiDung nguoiDung);

	Optional<DanhGia> findByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung);

	boolean existsByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung);

	// Thêm phương thức findAll với Pageable
	Page<DanhGia> findAll(Pageable pageable);

	@Query("SELECT AVG(d.soSao) FROM DanhGia d WHERE d.sanPham.maSanPham = :maSanPham")
	Double findAverageRatingBySanPhamId(Integer maSanPham);

	@Query("SELECT d.sanPham.tenSanPham, d.sanPham.maSanPham, AVG(d.soSao), COUNT(d), d.sanPham.hinhAnh "
			+ "FROM DanhGia d WHERE d.thoiGianDanhGia BETWEEN :from AND :to "
			+ "GROUP BY d.sanPham.maSanPham, d.sanPham.tenSanPham, d.sanPham.hinhAnh " + "ORDER BY AVG(d.soSao) DESC")
	List<Object[]> thongKeSanPhamDanhGiaCao(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT d FROM DanhGia d WHERE d.sanPham.maSanPham = :maSanPham ORDER BY d.thoiGianDanhGia DESC")
	List<DanhGia> findDanhGiaBySanPhamId(@Param("maSanPham") Integer maSanPham);
	
	@Query("SELECT dg FROM DanhGia dg JOIN FETCH dg.sanPham JOIN FETCH dg.nguoiDung WHERE dg.sanPham.trangThai = true")
	Page<DanhGia> findAllWithSanPhamAndNguoiDung(Pageable pageable);

	@Query("SELECT dg FROM DanhGia dg JOIN FETCH dg.sanPham JOIN FETCH dg.nguoiDung WHERE dg.sanPham.trangThai = true AND dg.soSao = :soSao")
	Page<DanhGia> findAllWithSanPhamAndNguoiDungBySoSao(@Param("soSao") int soSao, Pageable pageable);

	@Query("SELECT dg FROM DanhGia dg JOIN FETCH dg.sanPham sp JOIN FETCH dg.nguoiDung nd WHERE sp.maSanPham = :maSanPham")
	Page<DanhGia> findBySanPham_MaSanPham(@Param("maSanPham") String maSanPham, Pageable pageable);

	
}
