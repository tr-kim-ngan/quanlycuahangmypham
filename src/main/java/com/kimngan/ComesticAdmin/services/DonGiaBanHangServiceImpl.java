//package com.kimngan.ComesticAdmin.services;
//
//import com.kimngan.ComesticAdmin.entity.DonGiaBanHang;
//import com.kimngan.ComesticAdmin.entity.DonGiaBanHangId;
//import com.kimngan.ComesticAdmin.entity.SanPham;
//import com.kimngan.ComesticAdmin.entity.ThoiDiem;
//import com.kimngan.ComesticAdmin.repository.DonGiaBanHangRepository;
//import com.kimngan.ComesticAdmin.repository.ThoiDiemRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class DonGiaBanHangServiceImpl implements DonGiaBanHangService {
//
//	@Autowired
//	private DonGiaBanHangRepository donGiaBanHangRepository;
//	@Autowired
//	private ThoiDiemRepository thoiDiemRepository;
//
//	@Override
//	public List<DonGiaBanHang> getAll() {
//		// TODO Auto-generated method stub
//		return donGiaBanHangRepository.findAll();
//	}
//
//	@Override
//	public DonGiaBanHang create(DonGiaBanHang donGiaBanHang) {
//		// TODO Auto-generated method stub
//		return donGiaBanHangRepository.save(donGiaBanHang);
//	}
//
//	@Override
//	public DonGiaBanHang update(DonGiaBanHang donGiaBanHang) {
//		// TODO Auto-generated method stub
//		return donGiaBanHangRepository.save(donGiaBanHang);
//	}
//
//	@Override
//	public void delete(DonGiaBanHangId id) {
//		// TODO Auto-generated method stub
//		donGiaBanHangRepository.deleteById(id);
//
//	}
//
//	@Override
//	public Optional<DonGiaBanHang> findById(DonGiaBanHangId id) {
//		// TODO Auto-generated method stub
//		return donGiaBanHangRepository.findById(id);
//	}
//
//	@Override
//	public void updateDonGiaBanHangForProduct(SanPham sanPham, BigDecimal giaBan) {
//	    // Thêm đơn giá bán mới
//	    DonGiaBanHang newDonGia = new DonGiaBanHang();
//	    newDonGia.setSanPham(sanPham);
//	    newDonGia.setDonGiaBan(giaBan);
//	    newDonGia.setThoiDiem(new ThoiDiem(LocalDateTime.now()));
//	    donGiaBanHangRepository.save(newDonGia);
//	}
//
//
//}
