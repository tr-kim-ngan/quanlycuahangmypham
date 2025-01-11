package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.DonNhapHang;
import com.kimngan.ComesticAdmin.repository.DonNhapHangRepository;

@Service
public class DonNhapHangServiceImpl implements DonNhapHangService {
	@Autowired
	private DonNhapHangRepository donNhapHangRepository;

	@Override
	public List<DonNhapHang> getAll() {
		// TODO Auto-generated method stub
		return donNhapHangRepository.findAll();
	}

	@Override
	public Boolean create(DonNhapHang donNhapHang) {
		// TODO Auto-generated method stub
		try {
			donNhapHang.setTrangThai(true); // Đảm bảo trạng thái được đặt là hoạt động khi tạo mới
			donNhapHangRepository.save(donNhapHang);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public DonNhapHang findById(Integer maDonNhapHang) {
		// TODO Auto-generated method stub
		return donNhapHangRepository.findById(maDonNhapHang).orElse(null);
	}

	@Override
	public Boolean delete(Integer maDonNhapHang) {
		Optional<DonNhapHang> donNhapHangOpt = donNhapHangRepository.findById(maDonNhapHang);
		if (donNhapHangOpt.isPresent()) {
			DonNhapHang donNhapHang = donNhapHangOpt.get();

			// Kiểm tra tổng giá trị nhập hàng
			if (donNhapHang.getTongGiaTriNhapHang().compareTo(BigDecimal.ZERO) == 0) {
				// Nếu tổng giá trị bằng 0, xóa hoàn toàn khỏi cơ sở dữ liệu
				donNhapHangRepository.deleteById(maDonNhapHang);
			} else {
				// Nếu tổng giá trị lớn hơn 0, chỉ đổi trạng thái
				donNhapHang.setTrangThai(false); // Đánh dấu trạng thái là ngừng hoạt động
				donNhapHangRepository.save(donNhapHang);
			}
			return true;
		}
		return false;
	}

	@Override
	public Boolean update(DonNhapHang donNhapHang) {
		// TODO Auto-generated method stub
		if (donNhapHangRepository.existsById(donNhapHang.getMaDonNhapHang())) {
			donNhapHangRepository.save(donNhapHang);
			return true;
		}
		return false;
	}

	@Override
	public Page<DonNhapHang> findByTrangThai(boolean trangThai, Pageable pageable) {
		return donNhapHangRepository.findByTrangThai(trangThai, pageable);
	}

	@Override
	public Page<DonNhapHang> findByNgayNhapHang(LocalDate ngayNhap, Pageable pageable) {
		// TODO Auto-generated method stub
		return donNhapHangRepository.findByNgayNhapHang(ngayNhap, pageable);
	}

//	@Override
//	public Page<DonNhapHang> searchByDonNhapHang(String keyword, Pageable pageable) {
//		// TODO Auto-generated method stub
//		 return donNhapHangRepository.findByNgayNhapHangContainingIgnoreCaseAndTrangThaiTrue(keyword, pageable);
//	}

	@Override
	public Page<DonNhapHang> findAllActive(Pageable pageable) {
		// TODO Auto-generated method stub
		return donNhapHangRepository.findByTrangThaiTrue(pageable);
	}

	@Override
	public Page<DonNhapHang> findByNgayNhapHangBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
		// TODO Auto-generated method stub
		return donNhapHangRepository.findByNgayNhapHangBetween(startDate, endDate, pageable);
	}

	@Override
	public Page<DonNhapHang> findByNhaCungCap_Ten(String tenNhaCungCap, Pageable pageable) {
		// TODO Auto-generated method stub
		return donNhapHangRepository.findByNhaCungCap_TenNhaCungCapContainingIgnoreCaseAndTrangThaiTrue(tenNhaCungCap,
				pageable);
	}

}
