package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.repository.ChiTietDonHangRepository;
import com.kimngan.ComesticAdmin.repository.HoaDonRepository;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HoaDonServiceImpl implements HoaDonService {

	@Autowired
	private HoaDonRepository hoaDonRepository;
	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private ChiTietDonHangRepository chiTietDonHangRepository;

	@Override
	public List<HoaDon> getAllHoaDons() {
		return hoaDonRepository.findAll();
	}

	@Override
	public HoaDon getHoaDonById(Integer id) {
		System.out.println("Lấy hóa đơn với mã: " + id);
		return hoaDonRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + id));
	}

	@Override
	public HoaDon saveHoaDon(HoaDon hoaDon) {
		return hoaDonRepository.save(hoaDon);
	}

	@Override
	public HoaDon getHoaDonByDonHang(DonHang donHang) {
		System.out.println("Lấy hóa đơn liên kết với đơn hàng: " + donHang.getMaDonHang());
		HoaDon hoaDon = hoaDonRepository.findByDonHang(donHang);
		if (hoaDon == null) {
			throw new RuntimeException("Không tìm thấy hóa đơn liên kết với đơn hàng: " + donHang.getMaDonHang());
		}
		return hoaDon;
	}

	@Override
	public Page<HoaDon> getAllHoaDons(Pageable pageable) {
		// TODO Auto-generated method stub
		return hoaDonRepository.findAll(pageable);
	}

	@Override
	public List<HoaDon> getHoaDonsByCustomer(String username) {
		// TODO Auto-generated method stub
		return hoaDonRepository.findByCustomerUsername(username);
	}

	@Override
	public HoaDon findById(Integer id) {
		// TODO Auto-generated method stub
		return hoaDonRepository.findById(id).orElse(null);
	}

	@Override
	public Page<HoaDon> searchByTenNguoiNhan(String tenNguoiNhan, Pageable pageable) {
		// TODO Auto-generated method stub
        return hoaDonRepository.findByTenNguoiNhanContainingIgnoreCase(tenNguoiNhan, pageable);
	}

	@Override
	public Page<HoaDon> searchByNgayXuat(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
		// TODO Auto-generated method stub
	    return hoaDonRepository.findByNgayXuatHoaDonBetween(startDate, endDate, pageable);
	}

	@Override
	public Page<HoaDon> searchByTenNguoiNhanAndNgayXuatHoaDon(String tenNguoiNhan, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable) {
		// TODO Auto-generated method stub
		return hoaDonRepository.findByTenNguoiNhanContainingAndNgayXuatHoaDonBetween(tenNguoiNhan, startDateTime, endDateTime, pageable);
	}

	@Override
	public void xacNhanThanhToan(Integer maHoaDon) {
		HoaDon hoaDon = hoaDonRepository.findById(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon));
        hoaDon.setTrangThaiThanhToan("Đã xác nhận");
        hoaDonRepository.save(hoaDon);
		
	}

	@Override
	public Page<HoaDon> searchByStatus(String status, Pageable pageable) {
		// TODO Auto-generated method stub
		return hoaDonRepository.findByTrangThaiThanhToan(status, pageable);
	}

	@Override
	public Page<HoaDon> searchByTrangThaiAndNgayXuat(String trangThai, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable) {
		// TODO Auto-generated method stub
	    return hoaDonRepository.findByTrangThaiThanhToanAndNgayXuatHoaDonBetween(trangThai, startDateTime, endDateTime, pageable);

	}

	@Override
	public BigDecimal calculateTotalRevenue() {
		// TODO Auto-generated method stub
        return hoaDonRepository.calculateTotalRevenueByStatus("Đã xác nhận");

	}

	@Override
	public long countUnconfirmedInvoices() {
		// TODO Auto-generated method stub
        return hoaDonRepository.countByTrangThaiThanhToan("Chưa xác nhận");

	}

	



	


	
}