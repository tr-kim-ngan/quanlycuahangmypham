package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietDonHangRepository;
import com.kimngan.ComesticAdmin.repository.HoaDonRepository;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HoaDonServiceImpl implements HoaDonService {

	@Autowired
	private HoaDonRepository hoaDonRepository;
	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private ChiTietDonHangRepository chiTietDonHangRepository;

	@Override
	public List<HoaDon> getAllHoaDons() {
		return hoaDonRepository.findAll();
	}

	@Override
	public HoaDon getHoaDonById(Integer id) {
		System.out.println("Lấy hóa đơn với mã: " + id);
		return hoaDonRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + id));
	}

	@Override
	public HoaDon saveHoaDon(HoaDon hoaDon) {
		return hoaDonRepository.save(hoaDon);
	}

	@Override
	public HoaDon getHoaDonByDonHang(DonHang donHang) {
		System.out.println("Lấy hóa đơn liên kết với đơn hàng: " + donHang.getMaDonHang());
		HoaDon hoaDon = hoaDonRepository.findByDonHang(donHang);
		if (hoaDon == null) {
			throw new RuntimeException("Không tìm thấy hóa đơn liên kết với đơn hàng: " + donHang.getMaDonHang());
		}
		return hoaDon;
	}

	@Override
	public Page<HoaDon> getAllHoaDons(Pageable pageable) {
		// 
		return hoaDonRepository.findAll(pageable);
	}

	@Override
	public List<HoaDon> getHoaDonsByCustomer(String username) {
		// 
		return hoaDonRepository.findByCustomerUsername(username);
	}

	@Override
	public HoaDon findById(Integer id) {
		// 
		return hoaDonRepository.findById(id).orElse(null);
	}

	@Override
	public Page<HoaDon> searchByTenNguoiNhan(String tenNguoiNhan, Pageable pageable) {
		// 
        return hoaDonRepository.findByTenNguoiNhanContainingIgnoreCase(tenNguoiNhan, pageable);
	}

	@Override
	public Page<HoaDon> searchByNgayXuat(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
		// 
	    return hoaDonRepository.findByNgayXuatHoaDonBetween(startDate, endDate, pageable);
	}

	@Override
	public Page<HoaDon> searchByTenNguoiNhanAndNgayXuatHoaDon(String tenNguoiNhan, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable) {
		// 
		return hoaDonRepository.findByTenNguoiNhanContainingAndNgayXuatHoaDonBetween(tenNguoiNhan, startDateTime, endDateTime, pageable);
	}

	@Override
	public void xacNhanThanhToan(Integer maHoaDon) {
		HoaDon hoaDon = hoaDonRepository.findById(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon));
        hoaDon.setTrangThaiThanhToan("Đã xác nhận");
        hoaDonRepository.save(hoaDon);
		
	}

	@Override
	public Page<HoaDon> searchByStatus(String status, Pageable pageable) {
		// 
		return hoaDonRepository.findByTrangThaiThanhToan(status, pageable);
	}

	@Override
	public Page<HoaDon> searchByTrangThaiAndNgayXuat(String trangThai, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Pageable pageable) {
		// 
	    return hoaDonRepository.findByTrangThaiThanhToanAndNgayXuatHoaDonBetween(trangThai, startDateTime, endDateTime, pageable);

	}

	@Override
	public BigDecimal calculateTotalRevenue() {
		// 
        return hoaDonRepository.calculateTotalRevenueByStatus("Đã xác nhận");

	}

	@Override
	public long countUnconfirmedInvoices() {
		// 
        return hoaDonRepository.countByTrangThaiThanhToan("Chưa xác nhận");

	}

	@Override
	public List<SanPham> findTopSoldProductsByBrand(Integer maThuongHieu, int limit) {
	    // Lấy tất cả hóa đơn có trạng thái thanh toán thành công
	    List<HoaDon> successfulInvoices = hoaDonRepository.findByTrangThaiThanhToan("Đã xác nhận");

	    // Tạo Map để lưu số lượng bán của từng sản phẩm
	    Map<SanPham, Integer> productSalesMap = new HashMap<>();

	    // Lặp qua từng hóa đơn thành công
	    for (HoaDon hoaDon : successfulInvoices) {
	        DonHang donHang = hoaDon.getDonHang();
	        if (donHang != null) {
	            // Lặp qua từng ChiTietDonHang trong đơn hàng
	            for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
	                SanPham sanPham = chiTiet.getSanPham();
	                if (sanPham != null && sanPham.getThuongHieu().getMaThuongHieu().equals(maThuongHieu)) {
	                    // Tăng số lượng bán của sản phẩm trong productSalesMap
	                    productSalesMap.put(sanPham, productSalesMap.getOrDefault(sanPham, 0) + chiTiet.getSoLuong());
	                }
	            }
	        }
	    }

	    // Sắp xếp sản phẩm theo số lượng bán giảm dần và trả về top 4 sản phẩm
	    return productSalesMap.entrySet().stream()
	            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
	            .map(Map.Entry::getKey)
	            .limit(limit)
	            .collect(Collectors.toList());
	}

	@Override
	public List<SanPham> findTopSoldProductsByCategory(Integer maDanhMuc, int limit) {
	    // Lấy tất cả hóa đơn có trạng thái thanh toán thành công
	    List<HoaDon> successfulInvoices = hoaDonRepository.findByTrangThaiThanhToan("Đã xác nhận");

	    // Tạo Map để lưu số lượng bán của từng sản phẩm
	    Map<SanPham, Integer> productSalesMap = new HashMap<>();

	    // Lặp qua từng hóa đơn thành công
	    for (HoaDon hoaDon : successfulInvoices) {
	        DonHang donHang = hoaDon.getDonHang();
	        if (donHang != null) {
	            // Lặp qua từng ChiTietDonHang trong đơn hàng
	            for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
	                SanPham sanPham = chiTiet.getSanPham();
	                if (sanPham != null && sanPham.getDanhMuc().getMaDanhMuc().equals(maDanhMuc)) {
	                    // Tăng số lượng bán của sản phẩm trong productSalesMap
	                    productSalesMap.put(sanPham, productSalesMap.getOrDefault(sanPham, 0) + chiTiet.getSoLuong());
	                }
	            }
	        }
	    }

	    // Sắp xếp sản phẩm theo số lượng bán giảm dần và trả về top N sản phẩm
	    return productSalesMap.entrySet().stream()
	            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
	            .map(Map.Entry::getKey)
	            .limit(limit)
	            .collect(Collectors.toList());
	}

	@Override
	public int getTotalSoldQuantityByProduct(Integer maSanPham) {
	    List<HoaDon> successfulInvoices = hoaDonRepository.findByTrangThaiThanhToan("Đã xác nhận");

	    return successfulInvoices.stream()
	            .flatMap(hoaDon -> hoaDon.getDonHang().getChiTietDonHangs().stream())
	            .filter(chiTiet -> chiTiet.getSanPham().getMaSanPham().equals(maSanPham))
	            .mapToInt(ChiTietDonHang::getSoLuong)
	            .sum();
	}


	

	



	


	
}