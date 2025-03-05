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
import java.util.ArrayList;
import java.util.Arrays;
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
		 System.out.println("💾 Lưu hóa đơn vào database: " + hoaDon);
		    return hoaDonRepository.save(hoaDon);
	}




//	@Override
//	public HoaDon getHoaDonByDonHang(DonHang donHang) {
//		System.out.println("Lấy hóa đơn liên kết với đơn hàng: " + donHang.getMaDonHang());
//		HoaDon hoaDon = hoaDonRepository.findByDonHang(donHang);
//		if (hoaDon == null) {
//			throw new RuntimeException("Không tìm thấy hóa đơn liên kết với đơn hàng: " + donHang.getMaDonHang());
//		}
//		return hoaDon;
//	}
	@Override
	public HoaDon getHoaDonByDonHang(DonHang donHang) {
	    System.out.println(" Kiểm tra hóa đơn trong database cho đơn hàng: " + donHang.getMaDonHang());
	    HoaDon hoaDon = hoaDonRepository.findByDonHang(donHang);
	    
	    if (hoaDon == null) {
	        System.out.println(" Không tìm thấy hóa đơn trong database!");
	        return null;
	    }
	    
	    System.out.println("✅ Hóa đơn đã tồn tại: " + hoaDon);
	    return hoaDon                                                                                                                                                                                                                                                                       ;
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
	    // Lấy danh sách hóa đơn có trạng thái "Đã xác nhận"
	    List<HoaDon> hoaDons = hoaDonRepository.findByTrangThaiThanhToanIn(Arrays.asList("Đã xác nhận", "Đã hoàn thành")); 

	    BigDecimal totalRevenue = BigDecimal.ZERO;

	    for (HoaDon hoaDon : hoaDons) {
	        BigDecimal tongGiaTriSanPham = BigDecimal.ZERO;
	        BigDecimal phiVanChuyen = hoaDon.getDonHang().getPhiVanChuyen(); // Lấy phí vận chuyển

	        // Kiểm tra nếu phí vận chuyển là null thì gán 0
	        if (phiVanChuyen == null) {
	            phiVanChuyen = BigDecimal.ZERO;
	        }

	        // Tính tổng giá trị sản phẩm của hóa đơn
	        for (ChiTietDonHang chiTiet : hoaDon.getDonHang().getChiTietDonHangs()) {
	            BigDecimal thanhTien = chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong()));
	            tongGiaTriSanPham = tongGiaTriSanPham.add(thanhTien);
	        }

	        // Tính tổng giá trị đơn hàng
	        BigDecimal tongGiaTriDonHang = tongGiaTriSanPham.add(phiVanChuyen);

	        // Cộng vào tổng doanh thu
	        totalRevenue = totalRevenue.add(tongGiaTriDonHang);
	    }

	    return totalRevenue;
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

	@Override
	public void createHoaDon(DonHang donHang, String phuongThucThanhToan) {
		 HoaDon hoaDon = new HoaDon();
		    hoaDon.setDonHang(donHang);
		    hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
		    hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
		    hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
		    hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
		    hoaDon.setSoDienThoaiNhanHang(donHang.getSdtNhanHang());
		    hoaDon.setTrangThaiThanhToan("Chưa thanh toán"); // Mặc định khi tạo hóa đơn
		    hoaDon.setPhuongThucThanhToan(phuongThucThanhToan); // Thêm phương thức thanh toán 

		 
		    
		    
		    hoaDonRepository.save(hoaDon);
		
	}

	@Override
	 public List<Map<String, Object>> getRevenueByDate() {
        List<Object[]> results = hoaDonRepository.getRevenueByDate();
        return formatRevenueData(results);
    }
	@Override
	public List<Map<String, Object>> getRevenueByWeek() {
		// TODO Auto-generated method stub
		   List<Object[]> results = hoaDonRepository.getRevenueByWeek();
	        return formatRevenueData(results);
	}

	@Override
	public List<Map<String, Object>> getRevenueByMonth() {
		// TODO Auto-generated method stub
		List<Object[]> results = hoaDonRepository.getRevenueByMonth();
        return formatRevenueData(results);
	}
	 private List<Map<String, Object>> formatRevenueData(List<Object[]> results) {
	        List<Map<String, Object>> formattedData = new ArrayList<>();
	        for (Object[] result : results) {
	            Map<String, Object> data = new HashMap<>();
	            data.put("date", result[0]); // Ngày, tuần, hoặc tháng
	            data.put("totalRevenue", result[1]); // Tổng doanh thu
	            formattedData.add(data);
	        }
	        return formattedData;
	    }

	@Override
	public void updateHoaDon(HoaDon hoaDon) {
	    hoaDonRepository.save(hoaDon);
	}

	
	
}