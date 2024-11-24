//package com.kimngan.ComesticAdmin.services;
//
//import com.kimngan.ComesticAdmin.entity.ThoiDiem;
//import com.kimngan.ComesticAdmin.repository.ThoiDiemRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class ThoiDiemServiceImpl implements ThoiDiemService {
//
//	@Autowired
//	private ThoiDiemRepository thoiDiemRepository;
//
//	@Override
//	public List<ThoiDiem> getAll() {
//		// TODO Auto-generated method stub
//		return thoiDiemRepository.findAll();
//	}
//
//	@Override
//	public ThoiDiem create(ThoiDiem thoiDiem) {
//		// TODO Auto-generated method stub
//		return thoiDiemRepository.save(thoiDiem);
//	}
//
//	@Override
//	public ThoiDiem update(ThoiDiem thoiDiem) {
//		// TODO Auto-generated method stub
//		return thoiDiemRepository.save(thoiDiem);
//	}
//
//	@Override
//	public void delete(LocalDateTime ngayGio) {
//		// TODO Auto-generated method stub
//		thoiDiemRepository.deleteById(ngayGio);
//	}
//
//	@Override
//	public Optional<ThoiDiem> findById(LocalDateTime ngayGio) {
//		// TODO Auto-generated method stub
//		return thoiDiemRepository.findById(ngayGio);
//	}
//
//	@Override
//	public ThoiDiem findByNgayGio(LocalDateTime ngayGio) {
//		// TODO Auto-generated method stub
//		return thoiDiemRepository.findByNgayGio(ngayGio);
//	}
//
//}
