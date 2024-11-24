package com.kimngan.ComesticAdmin.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHangId;
import com.kimngan.ComesticAdmin.entity.DonNhapHang;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietDonNhapHangRepository;

@Service
public class ChiTietDonNhapHangServiceImpl implements ChiTietDonNhapHangService {
	@Autowired
	private ChiTietDonNhapHangRepository chiTietDonNhapHangRepository;

	@Override
	public List<ChiTietDonNhapHang> getAll() {
		// TODO Auto-generated method stub
		return chiTietDonNhapHangRepository.findAll();
	}

	@Override
	public ChiTietDonNhapHang findById(ChiTietDonNhapHangId id) {
		// TODO Auto-generated method stub
		return chiTietDonNhapHangRepository.findById(id).orElse(null);
	}

	@Override
	public Boolean create(ChiTietDonNhapHang chiTietDonNhapHang) {
		try {
			chiTietDonNhapHangRepository.save(chiTietDonNhapHang);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Boolean update(ChiTietDonNhapHang chiTietDonNhapHang) {
		// TODO Auto-generated method stub
		try {
			chiTietDonNhapHangRepository.save(chiTietDonNhapHang);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Boolean delete(ChiTietDonNhapHangId id) {
		// TODO Auto-generated method stub
		ChiTietDonNhapHang chiTiet = chiTietDonNhapHangRepository.findById(id).orElse(null);
		if (chiTiet != null) {
			chiTiet.setTrangThai(false);
			chiTietDonNhapHangRepository.save(chiTiet);
			return true;
		}
		return false;
	}

	@Override
	public Page<ChiTietDonNhapHang> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return chiTietDonNhapHangRepository.findAll(pageable);
	}

	@Override
	public Page<ChiTietDonNhapHang> searchBySanPhamName(String tenSanPham, Pageable pageable) {
		// TODO Auto-generated method stub
		return chiTietDonNhapHangRepository.findBySanPham_TenSanPhamContainingIgnoreCase(tenSanPham, pageable);
	}

	@Override
	public List<ChiTietDonNhapHang> findByDonNhapHang(DonNhapHang donNhapHang) {
		// TODO Auto-generated method stub
		return chiTietDonNhapHangRepository.findByDonNhapHang(donNhapHang);
	}

	// Phương thức tìm chi tiết đơn nhập hàng theo sản phẩm
	@Override
	public List<ChiTietDonNhapHang> findBySanPham(SanPham sanPham) {
		return chiTietDonNhapHangRepository.findBySanPham(sanPham);
	}

	@Override
	public void updateChiTietDonNhapHangForProduct(SanPham sanPham) {
		  List<ChiTietDonNhapHang> chiTietDonNhapHangList = chiTietDonNhapHangRepository.findBySanPham(sanPham);

		    for (ChiTietDonNhapHang chiTiet : chiTietDonNhapHangList) {
		        chiTiet.setSanPham(sanPham); // Cập nhật thông tin sản phẩm liên quan
		        chiTietDonNhapHangRepository.save(chiTiet);
		    }
	}

}
