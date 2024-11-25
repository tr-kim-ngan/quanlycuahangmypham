package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHangId;
import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietGioHangRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChiTietGioHangServiceImpl implements ChiTietGioHangService {

	@Autowired
	private ChiTietGioHangRepository chiTietGioHangRepository;

	@Override
	public void updateQuantity(GioHang gioHang, SanPham sanPham, int soLuong) {
		ChiTietGioHang chiTiet = findByGioHangAndSanPham(gioHang, sanPham);
		if (chiTiet != null) {
			chiTiet.setSoLuong(soLuong);
			chiTietGioHangRepository.save(chiTiet);
		}

	}

	@Override
	public ChiTietGioHang findByGioHangAndSanPham(GioHang gioHang, SanPham sanPham) {
		// TODO Auto-generated method stub
		return chiTietGioHangRepository.findByGioHangAndSanPham(gioHang, sanPham).orElse(null);
	}

	@Override
	public void delete(ChiTietGioHang chiTietGioHang) {
		chiTietGioHangRepository.delete(chiTietGioHang);

	}

//	@Override
//	public void addOrUpdateChiTietGioHang(GioHang gioHang, SanPham sanPham, int soLuongThem) {
//		ChiTietGioHang chiTietGioHang = findByGioHangAndSanPham(gioHang, sanPham);
//		if (chiTietGioHang != null) {
//			int soLuongHienTai = chiTietGioHang.getSoLuong();
//			int soLuongSauKhiThem = soLuongHienTai + soLuongThem;
//
//			System.out.println("Số lượng hiện tại trong chi tiết giỏ hàng của sản phẩm có mã " + sanPham.getMaSanPham()
//					+ ": " + soLuongHienTai);
//			System.out.println("Số lượng thêm vào: " + soLuongThem);
//			System.out.println("Tổng số lượng sau khi thêm: " + soLuongSauKhiThem);
//
//			chiTietGioHang.setSoLuong(soLuongSauKhiThem);
//			chiTietGioHangRepository.save(chiTietGioHang);
//		} else {
//			// Nếu sản phẩm chưa có trong chi tiết giỏ hàng, thêm mới
//			chiTietGioHang = new ChiTietGioHang(gioHang, sanPham, soLuongThem);
//			System.out.println("Thêm sản phẩm mới vào chi tiết giỏ hàng với mã sản phẩm " + sanPham.getMaSanPham()
//					+ " và số lượng: " + soLuongThem);
//			chiTietGioHangRepository.save(chiTietGioHang);
//		}
//	}
	@Override
	public void addOrUpdateChiTietGioHang(GioHang gioHang, SanPham sanPham, int soLuong) {
        // Kiểm tra sản phẩm có trong giỏ hàng chưa
        ChiTietGioHang chiTiet = chiTietGioHangRepository.findByGioHangAndSanPham(gioHang, sanPham).orElse(null);

        if (chiTiet != null) {
            // Sản phẩm đã có trong giỏ hàng, cộng thêm số lượng mới vào số lượng hiện tại
            int soLuongMoi = chiTiet.getSoLuong() + soLuong;
            System.out.println("Sản phẩm (ID: " + sanPham.getMaSanPham() + ") đã có trong giỏ. Số lượng hiện tại: "
                    + chiTiet.getSoLuong() + ", Số lượng mới thêm: " + soLuong + ", Tổng cộng: " + soLuongMoi);
            chiTiet.setSoLuong(soLuongMoi);
        } else {
            // Sản phẩm chưa có trong giỏ hàng, tạo mới
            chiTiet = new ChiTietGioHang(gioHang, sanPham, soLuong);
            System.out.println("Sản phẩm mới (ID: " + sanPham.getMaSanPham() + ") được thêm vào giỏ với số lượng: " + soLuong);
        }

        // Lưu thông tin cập nhật vào cơ sở dữ liệu
        chiTietGioHangRepository.save(chiTiet);
    }

}
