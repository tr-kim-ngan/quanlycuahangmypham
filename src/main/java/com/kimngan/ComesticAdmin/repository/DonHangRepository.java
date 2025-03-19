package com.kimngan.ComesticAdmin.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

	List<DonHang> findByShipper(NguoiDung shipper);

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

	Page<DonHang> findByNguoiDungTenNguoiDung(String username, Pageable pageable);

	Page<DonHang> findByNguoiDungTenNguoiDungAndTrangThaiDonHang(String username, String status, Pageable pageable);

	Page<DonHang> findTopByNguoiDungTenNguoiDungOrderByNgayDatDesc(String username, Pageable pageable);

	List<DonHang> findByNguoiDungAndTrangThaiDonHang(NguoiDung nguoiDung, String trangThaiDonHang);

	@Query("SELECT d FROM DonHang d WHERE d.shipper = :shipper")
	List<DonHang> findOrdersByShipper(@Param("shipper") NguoiDung shipper);

//	@Query("SELECT d FROM DonHang d WHERE d.trangThaiDonHang = :trangThai AND d.nhanVienXuatKho.maNguoiDung = :maNhanVien")
//	List<DonHang> findByTrangThaiAndNhanVienXuatKho(@Param("trangThai") String trangThai,
//			@Param("maNhanVien") Integer maNhanVien);
//
//	@Query("SELECT d FROM DonHang d WHERE d.trangThaiDonHang IN :trangThaiList AND d.nhanVienXuatKho.maNguoiDung = :maNhanVien")
//	List<DonHang> findByTrangThaiDonHangInAndNhanVienXuatKho(@Param("trangThaiList") List<String> trangThaiList,
//			@Param("maNhanVien") Integer maNhanVien);
	
	@Query("SELECT d FROM DonHang d WHERE d.trangThaiDonHang = :status")
	List<DonHang> findByTrangThaiDonHang(@Param("status") String status);
	
	
	@Query("SELECT d FROM DonHang d WHERE d.ngayXacNhanXuatKho IS NOT NULL")
	List<DonHang> findDonHangsDaXuatKho();

	@Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietDonHang c " +
		       "JOIN c.donHang d WHERE d.trangThaiDonHang = 'Đã hủy' " +
		       "AND c.sanPham.maSanPham = :maSanPham")
		int tinhTongSoLuongTraHang(@Param("maSanPham") Integer maSanPham);


}