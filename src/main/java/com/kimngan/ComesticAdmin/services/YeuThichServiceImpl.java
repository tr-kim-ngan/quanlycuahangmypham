package com.kimngan.ComesticAdmin.services;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.YeuThich;
import com.kimngan.ComesticAdmin.repository.YeuThichRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class YeuThichServiceImpl implements YeuThichService {
	@Autowired
	private YeuThichRepository yeuThichRepository;

	@Override
	public boolean isYeuThich(NguoiDung nguoiDung, SanPham sanPham) {
		// TODO Auto-generated method stub
		return yeuThichRepository.existsByNguoiDungAndSanPham(nguoiDung, sanPham);
	}

	@Override
	public void addYeuThich(NguoiDung nguoiDung, SanPham sanPham) {
		if (!isYeuThich(nguoiDung, sanPham)) {
			YeuThich yeuThich = new YeuThich();
			yeuThich.setNguoiDung(nguoiDung);
			yeuThich.setSanPham(sanPham);
			yeuThichRepository.save(yeuThich);
			System.out.println("YeuThich saved: " + yeuThich); // Kiểm tra xem bản ghi có được lưu không
		} else {
			System.out.println("YeuThich already exists.");
		}
	}

	@Override
	@Transactional // Đảm bảo phương thức này chạy trong một transaction
	public void removeYeuThich(NguoiDung nguoiDung, SanPham sanPham) {
		if (isYeuThich(nguoiDung, sanPham)) {
			yeuThichRepository.deleteByNguoiDungAndSanPham(nguoiDung, sanPham);
			System.out.println("YeuThich removed.");
		} else {
			System.out.println("YeuThich not found.");
		}
	}

	@Override
	public List<SanPham> getFavoritesByUser(NguoiDung nguoiDung) {
		List<YeuThich> yeuThichs = yeuThichRepository
				.findByNguoiDungMaNguoiDungAndSanPhamTrangThaiTrue(nguoiDung.getMaNguoiDung());
		return yeuThichs.stream().map(YeuThich::getSanPham).filter(sanPham -> sanPham.isTrangThai()) // Chỉ lấy sản phẩm
																										// có trạng thái
																										// là true
																										// (trangThai =
																										// 1)
				.collect(Collectors.toList());
	}
//	@Override
//	public Set<Integer> getFavoriteProductIdsForUser(NguoiDung currentUser) {
//	    List<YeuThich> favorites = yeuThichRepository.findByNguoiDung(currentUser);
//	    // In ra danh sách các sản phẩm yêu thích để kiểm tra
//	    System.out.println("Favorites fetched for user " + currentUser.getTenNguoiDung() + ": " + favorites);
//	    return favorites.stream().map(favorite -> favorite.getSanPham().getMaSanPham()).collect(Collectors.toSet());
//	}
	@Override
	public Set<Integer> getFavoriteProductIdsForUser(NguoiDung currentUser) {
	    List<YeuThich> favorites = yeuThichRepository.findByNguoiDung(currentUser);
	    System.out.println("Favorites for user " + currentUser.getMaNguoiDung() + ": " + favorites);
	    return favorites.stream()
	                    .map(favorite -> favorite.getSanPham().getMaSanPham())
	                    .collect(Collectors.toSet());
	}







}
