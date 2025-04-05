package com.kimngan.ComesticAdmin.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

//	@Query("SELECT d FROM DonHang d WHERE d.ngayXacNhanXuatKho IS NOT NULL")
//	List<DonHang> findDonHangsDaXuatKho();
//	
	@Query("""
		    SELECT d FROM DonHang d
		    WHERE d.ngayXacNhanXuatKho IS NOT NULL
		       OR (d.trangThaiDonHang = 'Đã hủy' AND d.seller IS NOT NULL AND d.seller.quyenTruyCap.maQuyen = 4)
		""")
		Page<DonHang> findDonHangsDaXuatKho(Pageable pageable);




	@Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietDonHang c "
			+ "JOIN c.donHang d WHERE d.trangThaiDonHang = 'Đã hủy' " + "AND c.sanPham.maSanPham = :maSanPham")
	int tinhTongSoLuongTraHang(@Param("maSanPham") Integer maSanPham);

	Page<DonHang> findByNguoiDung_TenNguoiDungAndDiaChiGiaoHang(String username, String diaChi, Pageable pageable);

	Page<DonHang> findByNguoiDung_TenNguoiDungAndDiaChiGiaoHangNot(String username, String diaChi, Pageable pageable);

	int countByShipper(NguoiDung shipper);

	List<DonHang> findByTrangThaiDonHangAndShipper(String trangThaiDonHang, NguoiDung shipper);

	List<DonHang> findBySeller(NguoiDung seller);

	long countByNgayDatBetween(LocalDateTime fromDate, LocalDateTime toDate);

	@Query("SELECT FUNCTION('DATE', d.ngayDat), COUNT(d) FROM DonHang d "
			+ "WHERE d.ngayDat BETWEEN :fromDate AND :toDate "
			+ "GROUP BY FUNCTION('DATE', d.ngayDat) ORDER BY FUNCTION('DATE', d.ngayDat)")
	List<Object[]> getSoDonHangTheoNgay(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

//Top nhân viên bán hàng có nhiều đơn nhất

	@Query("SELECT dh.nguoiDung.tenNguoiDung, COUNT(dh) " + "FROM DonHang dh "
			+ "WHERE dh.ngayDat BETWEEN :from AND :to AND dh.nguoiDung.quyenTruyCap.tenQuyen = 'SELLER' "
			+ "GROUP BY dh.nguoiDung.tenNguoiDung " + "ORDER BY COUNT(dh) DESC")
	List<Object[]> findTopNhanVienBanHang(LocalDateTime from, LocalDateTime to);

	// Top khách hàng đặt nhiều đơn nhất
	@Query("SELECT dh.nguoiDung.tenNguoiDung, dh.nguoiDung.hoTen, dh.nguoiDung.soDienThoai, COUNT(dh) "
			+ "FROM DonHang dh "
			+ "WHERE dh.ngayDat BETWEEN :from AND :to AND dh.nguoiDung.quyenTruyCap.tenQuyen = 'CUSTOMER' "
			+ "GROUP BY dh.nguoiDung.tenNguoiDung, dh.nguoiDung.hoTen, dh.nguoiDung.soDienThoai "
			+ "ORDER BY COUNT(dh) DESC")
	List<Object[]> findTopKhachHang(LocalDateTime from, LocalDateTime to);

	// Top sản phẩm được đặt nhiều nhất

	@Query("SELECT ctdh.sanPham.tenSanPham, SUM(ctdh.soLuong) " + "FROM ChiTietDonHang ctdh "
			+ "WHERE ctdh.donHang.ngayDat BETWEEN :from AND :to " + "GROUP BY ctdh.sanPham.tenSanPham "
			+ "ORDER BY SUM(ctdh.soLuong) DESC")
	List<Object[]> findTopSanPham(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("""
			    SELECT
			        FUNCTION('DATE', dh.ngayDat) AS ngay,
			        SUM(ctdh.soLuong * ctdh.giaTaiThoiDiemDat) AS doanhThu,
			        SUM(
			            (ctdh.giaTaiThoiDiemDat - (
			                SELECT AVG(ctdnh.donGiaNhap)
			                FROM ChiTietDonNhapHang ctdnh
			                WHERE ctdnh.sanPham.maSanPham = ctdh.sanPham.maSanPham
			            )) * ctdh.soLuong
			        ) AS loiNhuan
			    FROM ChiTietDonHang ctdh
			    JOIN ctdh.donHang dh
			    WHERE dh.ngayDat BETWEEN :from AND :to
			    AND dh.trangThaiDonHang = 'Đã hoàn thành'
			    GROUP BY FUNCTION('DATE', dh.ngayDat)
			    ORDER BY FUNCTION('DATE', dh.ngayDat)
			""")
	List<Object[]> getDoanhThuVaLoiNhuanTheoNgay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("""
			    SELECT
			        ctdh.sanPham.tenSanPham,
			        SUM(ctdh.giaTaiThoiDiemDat * ctdh.soLuong),
			        SUM((ctdh.giaTaiThoiDiemDat - (
			            SELECT COALESCE(AVG(ctdnh.donGiaNhap), 0)
			            FROM ChiTietDonNhapHang ctdnh
			            WHERE ctdnh.sanPham.maSanPham = ctdh.sanPham.maSanPham
			        )) * ctdh.soLuong)
			    FROM ChiTietDonHang ctdh
			    WHERE ctdh.donHang.ngayDat BETWEEN :from AND :to
			        AND ctdh.donHang.trangThaiDonHang = 'Đã hoàn thành'
			    GROUP BY ctdh.sanPham.tenSanPham
			    ORDER BY SUM(ctdh.giaTaiThoiDiemDat * ctdh.soLuong) DESC
			""")
	List<Object[]> getDoanhThuVaLoiNhuanTheoSanPham(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("""
			    SELECT
			        ctdh.sanPham.tenSanPham,
			        SUM(ctdh.soLuong),
			        SUM(ctdh.giaTaiThoiDiemDat * ctdh.soLuong),
			        AVG(ctdh.giaTaiThoiDiemDat),
			        SUM((ctdh.giaTaiThoiDiemDat - (
			            SELECT AVG(ctn.donGiaNhap)
			            FROM ChiTietDonNhapHang ctn
			            WHERE ctn.sanPham.maSanPham = ctdh.sanPham.maSanPham
			        )) * ctdh.soLuong)
			    FROM ChiTietDonHang ctdh
			    WHERE ctdh.donHang.ngayDat BETWEEN :from AND :to
			    AND ctdh.donHang.trangThaiDonHang = 'Đã hoàn thành'
			    GROUP BY ctdh.sanPham.tenSanPham
			    ORDER BY SUM(ctdh.soLuong) DESC
			""")
	List<Object[]> thongKeSanPhamBanChayChiTiet(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT dh.trangThaiDonHang, COUNT(dh) FROM DonHang dh GROUP BY dh.trangThaiDonHang")
	List<Object[]> thongKeDonHangTheoTrangThai();

	// DonHangRepository.java

	@Query("SELECT dh.seller.hoTen, SUM(dh.tongGiaTriDonHang) " + "FROM DonHang dh "
			+ "WHERE dh.trangThaiDonHang = 'Đã hoàn thành' AND dh.seller IS NOT NULL "
			+ "AND dh.ngayDat BETWEEN :from AND :to " + "GROUP BY dh.seller.hoTen")
	List<Object[]> getDoanhThuTheoNhanVien(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT dh.seller.hoTen, SUM(dh.tongGiaTriDonHang), COUNT(DISTINCT dh), SUM(ct.soLuong) "
			+ "FROM DonHang dh JOIN dh.chiTietDonHangs ct "
			+ "WHERE dh.trangThaiDonHang = 'Đã hoàn thành' AND dh.seller IS NOT NULL AND dh.ngayDat BETWEEN :fromDate AND :toDate "
			+ "GROUP BY dh.seller.hoTen")
	List<Object[]> thongKeDoanhThuTheoNhanVien(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate);

	
	
	
	@Query("SELECT dh.nguoiDung.hoTen, dh.nguoiDung.tenNguoiDung, COUNT(dh), SUM(dh.tongGiaTriDonHang) " +
		       "FROM DonHang dh " +
		       "WHERE dh.trangThaiDonHang = 'Đã hoàn thành' AND dh.ngayDat BETWEEN :from AND :to " +
		       "GROUP BY dh.nguoiDung.hoTen, dh.nguoiDung.tenNguoiDung " +
		       "ORDER BY SUM(dh.tongGiaTriDonHang) DESC")
		List<Object[]> thongKeKhachHangMuaNhieu(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

		@Query("SELECT dh.nguoiDung.hoTen, dh.nguoiDung.tenNguoiDung, COUNT(dh) " +
		       "FROM DonHang dh " +
		       "WHERE dh.trangThaiDonHang = 'Đã huỷ' AND dh.ngayDat BETWEEN :from AND :to " +
		       "GROUP BY dh.nguoiDung.hoTen, dh.nguoiDung.tenNguoiDung " +
		       "ORDER BY COUNT(dh) DESC")
		List<Object[]> thongKeKhachHangHuyDonNhieu(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

//		@Query("SELECT COUNT(dh), SUM(dh.tongGiaTriDonHang) FROM DonHang dh WHERE dh.nguoiDung.maNguoiDung = :maNguoiDung AND LOWER(dh.trangThaiDonHang) = 'đã hoàn thành'")
//		Object[] thongKeDonMuaTheoKhach(@Param("maNguoiDung") Integer maNguoiDung);
//
//		@Query("SELECT COUNT(dh) FROM DonHang dh WHERE dh.nguoiDung.maNguoiDung = :maNguoiDung AND LOWER(dh.trangThaiDonHang) = 'đã huỷ'")
//		Long thongKeDonHuyTheoKhach(@Param("maNguoiDung") Integer maNguoiDung);

	
	
		@Query("SELECT COUNT(dh), SUM(dh.tongGiaTriDonHang) " +
			       "FROM DonHang dh " +
			       "WHERE dh.nguoiDung.maNguoiDung = :id AND dh.trangThaiDonHang = 'Đã hoàn thành'")
			List<Object[]> thongKeDonMuaTheoKhach(@Param("id") Integer id);

		
		@Query("SELECT COUNT(dh) " +
			       "FROM DonHang dh " +
			       "WHERE dh.nguoiDung.maNguoiDung = :maNguoiDung " +
			       "AND LOWER(dh.trangThaiDonHang) LIKE '%huỷ%'")
			Long thongKeDonHuyTheoKhach(@Param("maNguoiDung") Integer maNguoiDung);
	
		List<DonHang> findByTrangThaiDonHangIn(List<String> trangThaiList);

	
	
	
	
	
	
}