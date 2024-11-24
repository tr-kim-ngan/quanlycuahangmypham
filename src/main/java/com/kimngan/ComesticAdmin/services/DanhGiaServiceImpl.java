package com.kimngan.ComesticAdmin.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.DanhGiaRepository;

@Service
public class DanhGiaServiceImpl implements DanhGiaService {

	@Autowired
	private DanhGiaRepository danhGiaRepository;
	@Autowired
	private HoaDonService hoaDonService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@Override
	public DanhGia saveDanhGia(DanhGia danhGia) {
		return danhGiaRepository.save(danhGia);
	}

	@Override
	public List<DanhGia> getDanhGiaBySanPham(SanPham sanPham) {
		return danhGiaRepository.findBySanPham(sanPham);
	}

	@Override
	public List<DanhGia> getDanhGiaByNguoiDung(NguoiDung nguoiDung) {
		return danhGiaRepository.findByNguoiDung(nguoiDung);
	}

	@Override
	public List<DanhGia> findByHoaDonAndNguoiDung(HoaDon hoaDon, NguoiDung nguoiDung) {
		return danhGiaRepository.findByHoaDonAndNguoiDung(hoaDon, nguoiDung);
	}

	@Override
	public DanhGia create(DanhGia danhGia) {
		return danhGiaRepository.save(danhGia);
	}

	@Override
	public boolean existsByHoaDonAndNguoiDung(Integer maHoaDon, Integer maNguoiDung) {
		HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
		NguoiDung nguoiDung = nguoiDungService.findById(maNguoiDung);
		return danhGiaRepository.existsByHoaDonAndNguoiDung(hoaDon, nguoiDung);
	}

}
