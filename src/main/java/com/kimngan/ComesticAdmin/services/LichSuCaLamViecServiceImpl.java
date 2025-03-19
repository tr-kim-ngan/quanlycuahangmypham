package com.kimngan.ComesticAdmin.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.kimngan.ComesticAdmin.entity.LichSuCaLamViec;
import com.kimngan.ComesticAdmin.repository.LichSuCaLamViecRepository;
@Service
public class LichSuCaLamViecServiceImpl implements LichSuCaLamViecService {
	@Autowired
	private LichSuCaLamViecRepository lichSuCaLamViecRepository;

	@Override
	public LichSuCaLamViec batDauCa(LichSuCaLamViec caLamViec) {
		// TODO Auto-generated method stub
		   caLamViec.setThoiGianBatDau(LocalDateTime.now()); // Lưu thời gian bắt đầu ca
	        return lichSuCaLamViecRepository.save(caLamViec);
	}

	@Override
	public LichSuCaLamViec ketThucCa(Integer maCa) {
		// TODO Auto-generated method stub
		 Optional<LichSuCaLamViec> optionalCa = lichSuCaLamViecRepository.findById(maCa);
	        if (optionalCa.isPresent()) {
	            LichSuCaLamViec caLamViec = optionalCa.get();
	            caLamViec.setThoiGianKetThuc(LocalDateTime.now()); // Cập nhật thời gian kết thúc ca
	            return lichSuCaLamViecRepository.save(caLamViec);
	        }
	        return null;
	}

	@Override
	public List<LichSuCaLamViec> getLichSuCaTheoNhanVien(Integer maNhanVien) {
		// TODO Auto-generated method stub
		return lichSuCaLamViecRepository.findByNhanVienMaNguoiDung(maNhanVien);
	}

	@Override
	public List<LichSuCaLamViec> getTatCaLichSu() {
		// TODO Auto-generated method stub
		return lichSuCaLamViecRepository.findAll();
	}

	@Override
	public List<LichSuCaLamViec> getAllShifts() {
		// TODO Auto-generated method stub
		return lichSuCaLamViecRepository.findAll();
	}

	@Override
	public void saveShift(LichSuCaLamViec caLamViec) {
		
		 lichSuCaLamViecRepository.save(caLamViec);
	}

	@Override
	public Optional<LichSuCaLamViec> findById(Integer id) {
		// TODO Auto-generated method stub
		return lichSuCaLamViecRepository.findById(id);
	}

	@Override
	public LichSuCaLamViec getCurrentShift(String username) {
		// TODO Auto-generated method stub
		  return lichSuCaLamViecRepository.findCurrentShiftByUsername(username);
	}

	@Override
	public Page<LichSuCaLamViec> getAllShifts(Pageable pageable) {
		// TODO Auto-generated method stub
        return lichSuCaLamViecRepository.findAll(pageable);

	}

	@Override
	public Page<LichSuCaLamViec> findCaLamChuaXetDuyet(Pageable pageable) {
		// TODO Auto-generated method stub
	    return lichSuCaLamViecRepository.findCaLamChuaXetDuyet(pageable);
	}

	@Override
	public Page<LichSuCaLamViec> getAllShiftsWithInventory(Pageable pageable) {
		// TODO Auto-generated method stub
		 return lichSuCaLamViecRepository.findAllShiftsWithInventory(pageable);
	}

}
