package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.LoaiKhachHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.QuyenTruyCap;
import com.kimngan.ComesticAdmin.repository.DonHangRepository;
import com.kimngan.ComesticAdmin.repository.HoaDonRepository;
import com.kimngan.ComesticAdmin.repository.LoaiKhachHangRepository;
import com.kimngan.ComesticAdmin.repository.NguoiDungRepository;
import com.kimngan.ComesticAdmin.repository.QuyenTruyCapRepository;

@Service
public class NguoiDungServiceImpl implements NguoiDungService {
	
	@Autowired
	private NguoiDungRepository nguoiDungRepository;
	@Autowired
	private QuyenTruyCapRepository quyenTruyCapRepository;
	@Autowired
	private HoaDonRepository hoaDonRepository;

	@Autowired
	private LoaiKhachHangRepository loaiKhachHangRepository;
	@Autowired
	private DonHangRepository donHangRepository;


	@Override
	public NguoiDung findByTenNguoiDung(String tenNguoiDung) {
		// TODO Auto-generated method stub
		return nguoiDungRepository.findByTenNguoiDung(tenNguoiDung);
	}


	@Override
	public void saveCustomer(NguoiDung nguoiDung) {
	    // Chỉ gán quyền CUSTOMER nếu người dùng chưa có quyền nào
	    if (nguoiDung.getQuyenTruyCap() == null) {
	        QuyenTruyCap quyenCustomer = quyenTruyCapRepository.findByTenQuyen("CUSTOMER");
	        nguoiDung.setQuyenTruyCap(quyenCustomer);
	    }

	    // Lưu người dùng vào cơ sở dữ liệu
	    nguoiDungRepository.save(nguoiDung);
	}


	@Override
	public void save(NguoiDung nguoiDung) {
		// TODO Auto-generated method stub
		nguoiDungRepository.save(nguoiDung);
	}

	@Override
	public NguoiDung findById(Integer id) {
		// TODO Auto-generated method stub
		return nguoiDungRepository.findById(id).orElse(null);
	}

	@Override
	public void updateLoaiKhachHangBasedOnTotalOrders(Integer maNguoiDung) {
	    // Tính tổng giá trị các đơn hàng đã hoàn thành của người dùng
	    BigDecimal totalOrderValue = donHangRepository.findTotalOrderValueByNguoiDung(maNguoiDung);

	    // Kiểm tra tổng giá trị và cập nhật loại khách hàng
	    NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung).orElse(null);

	    if (nguoiDung != null && totalOrderValue != null) {
	        if (totalOrderValue.compareTo(new BigDecimal(2000000)) >= 0) {
	            // Khách hàng có tổng đơn hàng >= 2 triệu -> Kim cương
	            nguoiDung.setLoaiKhachHang(loaiKhachHangRepository.findByTenLoaiKH("Kim cương").orElse(null));
	            System.out.println("Updated to Kim cương for user: " + maNguoiDung);
	        } else if (totalOrderValue.compareTo(new BigDecimal(1000000)) >= 0) {
	            // Khách hàng có tổng đơn hàng >= 1 triệu -> Vàng
	            nguoiDung.setLoaiKhachHang(loaiKhachHangRepository.findByTenLoaiKH("Vàng").orElse(null));
	            System.out.println("Updated to Vàng for user: " + maNguoiDung);
	        } else {
	            // Mặc định -> Đồng
	            nguoiDung.setLoaiKhachHang(loaiKhachHangRepository.findByTenLoaiKH("Đồng").orElse(null));
	            System.out.println("Updated to Đồng for user: " + maNguoiDung);
	        }
	        nguoiDungRepository.save(nguoiDung);
	        System.out.println("User type updated successfully in database for user: " + maNguoiDung);
	    } else {
	        System.out.println("User not found or total order value is null for user: " + maNguoiDung);
	    }
	}

	@Override
	public long countCustomers() {
		// TODO Auto-generated method stub
		 return nguoiDungRepository.countByQuyenTruyCap_TenQuyen("CUSTOMER");
	}

	@Override
	public List<NguoiDung> findByRole(String role) {
		
		 return nguoiDungRepository.findByQuyenTruyCap_TenQuyen(role);
	}

	@Override
	public void deleteById(Integer id) {
		 nguoiDungRepository.deleteById(id);
		
	}


	@Override
	public boolean existsByEmail(String email) {
		// TODO Auto-generated method stub
		  return nguoiDungRepository.existsByEmail(email);
	}


	@Override
	public boolean existsBySoDienThoai(String soDienThoai) {
		// TODO Auto-generated method stub
		 return nguoiDungRepository.existsBySoDienThoai(soDienThoai);
	}


	@Override
	public boolean existsByEmailAndNotId(String email, Integer id) {
		// TODO Auto-generated method stub
		return nguoiDungRepository.existsByEmailAndMaNguoiDungNot(email, id);
	}


	@Override
	public boolean existsBySoDienThoaiAndNotId(String soDienThoai, Integer id) {
		// TODO Auto-generated method stub
		return nguoiDungRepository.existsBySoDienThoaiAndMaNguoiDungNot(soDienThoai, id);
	}







}
