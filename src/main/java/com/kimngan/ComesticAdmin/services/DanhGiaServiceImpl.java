package com.kimngan.ComesticAdmin.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

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
	public boolean existsByHoaDonAndNguoiDung(HoaDon hoaDon, NguoiDung nguoiDung) {
		// TODO Auto-generated method stub
        return danhGiaRepository.existsByHoaDonAndNguoiDung(hoaDon, nguoiDung);
	}

	@Override
	public DanhGia findByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung) {
		// TODO Auto-generated method stub
		return danhGiaRepository.findByHoaDonAndSanPhamAndNguoiDung(hoaDon, sanPham, nguoiDung)
	            .orElse(null);
	}

	@Override
	public boolean existsByHoaDonAndSanPhamAndNguoiDung(HoaDon hoaDon, SanPham sanPham, NguoiDung nguoiDung) {
		// TODO Auto-generated method stub
        return danhGiaRepository.existsByHoaDonAndSanPhamAndNguoiDung(hoaDon, sanPham, nguoiDung);
	}

	@Override
	public List<DanhGia> findBySanPham(SanPham sanPham) {
		// TODO Auto-generated method stub
		 return danhGiaRepository.findBySanPham(sanPham);
	}

	@Override
	public DanhGia findById(Integer maDanhGia) {
		// TODO Auto-generated method stub
		return danhGiaRepository.findById(maDanhGia).orElse(null);
	}

	
	@Override
	public Page<DanhGia> getAllDanhGias(Pageable pageable) {
	    return danhGiaRepository.findAll(pageable);
	}

	@Override
	public DanhGia save(DanhGia danhGia) {
		// TODO Auto-generated method stub
		return danhGiaRepository.save(danhGia);
	}

	@Override
	public void delete(Integer maDanhGia) {
		// TODO Auto-generated method stub
		 danhGiaRepository.deleteById(maDanhGia);
	}

	@Override
    public List<Object[]> thongKeSanPhamDanhGiaCao(LocalDateTime from, LocalDateTime to) {
        return danhGiaRepository.thongKeSanPhamDanhGiaCao(from, to);
    }

    @Override
    public List<DanhGia> getDanhGiaBySanPhamId(Integer maSanPham) {
        return danhGiaRepository.findDanhGiaBySanPhamId(maSanPham);
    }

	@Override
	public Page<DanhGia> findAllWithSanPhamAndNguoiDung(Pageable pageable) {
		// TODO Auto-generated method stub
		 return danhGiaRepository.findAllWithSanPhamAndNguoiDung(pageable);
	}

	@Override
	public Page<DanhGia> findAllWithSanPhamAndNguoiDungBySoSao(int soSao, Pageable pageable) {
		// TODO Auto-generated method stub
		  return danhGiaRepository.findAllWithSanPhamAndNguoiDungBySoSao(soSao, pageable);
	}

	@Override
	public Page<DanhGia> findAllWithSanPhamAndNguoiDungByMaSanPham(String maSanPham, Pageable pageable) {
		// TODO Auto-generated method stub
		 return danhGiaRepository.findBySanPham_MaSanPham(maSanPham, pageable);
    
	}

	
    
    

}
