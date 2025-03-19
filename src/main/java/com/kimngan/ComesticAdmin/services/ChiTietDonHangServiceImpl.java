package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;

import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietDonHangRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ChiTietDonHangServiceImpl implements ChiTietDonHangService {

	@Autowired
	private ChiTietDonHangRepository chiTietDonHangRepository;

	@Override
	public ChiTietDonHang createChiTietDonHang(ChiTietDonHang chiTietDonHang) {
		return chiTietDonHangRepository.save(chiTietDonHang);
	}

	@Override
	public ChiTietDonHang updateChiTietDonHang(ChiTietDonHang chiTietDonHang) {
		if (chiTietDonHangRepository.existsById(chiTietDonHang.getId())) {
			return chiTietDonHangRepository.save(chiTietDonHang);
		}
		throw new RuntimeException("Chi tiết đơn hàng không tồn tại với mã: " + chiTietDonHang.getId());
	}

	@Override
	public void deleteChiTietDonHang(ChiTietDonHangId id) {
		if (chiTietDonHangRepository.existsById(id)) {
			chiTietDonHangRepository.deleteById(id);
		} else {
			throw new RuntimeException("Chi tiết đơn hàng không tồn tại với mã: " + id);
		}
	}

	@Override
	public ChiTietDonHang getChiTietDonHangById(ChiTietDonHangId id) {
		return chiTietDonHangRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Chi tiết đơn hàng không tồn tại với mã: " + id));
	}

	@Override
	public List<ChiTietDonHang> getChiTietDonHangsByDonHangId(Integer maDonHang) {
		// Giả sử bạn thêm query để tìm theo mã đơn hàng
		return chiTietDonHangRepository.findAll(); // Thay bằng custom query nếu cần
	}

	@Override
	public ChiTietDonHang save(ChiTietDonHang chiTietDonHang) {
		return chiTietDonHangRepository.save(chiTietDonHang);
	}

//	@Override
//	public Integer getSoldQuantityBySanPhamId(Integer sanPhamId) {
//		// Lấy danh sách chi tiết đơn hàng với trạng thái "Đã xác nhận", "Đang giao
//		// hàng" hoặc "Đã hoàn thành"
//		List<ChiTietDonHang> chiTietDonHangList = chiTietDonHangRepository
//				.findBySanPhamMaSanPhamAndDonHangTrangThaiDonHangIn(sanPhamId,
//						Arrays.asList("Đã xác nhận", "Đang giao hàng", "Đã hoàn thành"));
//
//		// Tính tổng số lượng đã bán từ các chi tiết đơn hàng
//		return chiTietDonHangList.stream().mapToInt(ChiTietDonHang::getSoLuong).sum();
//	}
	@Override
	public Integer getSoldQuantityBySanPhamId(Integer sanPhamId) {
	    // Lấy số lượng từ hóa đơn online đã hoàn thành
	    Integer onlineSold = chiTietDonHangRepository.getSoldQuantityFromCompletedInvoices(sanPhamId);
	    onlineSold = (onlineSold != null) ? onlineSold : 0; // Nếu null thì gán giá trị 0

	    // Lấy số lượng từ hóa đơn offline đã thanh toán
	    Integer offlineSold = chiTietDonHangRepository.getSoldQuantityFromOfflineOrders(sanPhamId);
	    offlineSold = (offlineSold != null) ? offlineSold : 0; // Nếu null thì gán giá trị 0

	    // Tổng số lượng bán ra
	    return onlineSold + offlineSold;
	}



	@Override
	public List<Object[]> getTop3BestSellingProducts() {
		// TODO Auto-generated method stub
		return chiTietDonHangRepository.findTop3BestSellingProducts();
	}

	@Override
	public List<SanPham> findTopSoldProductsByBrand(Integer maThuongHieu, int limit) {
		return chiTietDonHangRepository.findTopSoldProductsByBrand(maThuongHieu, PageRequest.of(0, limit + 1));
	}

	@Override
	public Integer getTotalQuantityBySanPhamId(Integer maSanPham) {
		// TODO Auto-generated method stub
		Integer totalQuantity = chiTietDonHangRepository.getTotalQuantityBySanPhamId(maSanPham);
	    return (totalQuantity != null) ? totalQuantity : 0;
	}

	@Override
	public List<Object[]> getExportStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
	    return chiTietDonHangRepository.getExportStatistics(fromDate, toDate);
	}

	@Override
	public List<Object[]> getTopExportedProducts(LocalDateTime fromDate, LocalDateTime toDate) {
	    return chiTietDonHangRepository.getTopExportedProducts(fromDate, toDate);
	}

	@Override
	public List<Object[]> getBaoCaoXuatKhoChiTiet(LocalDateTime fromDate, LocalDateTime toDate) {
	    return chiTietDonHangRepository.getBaoCaoXuatKhoChiTiet(fromDate, toDate);
	}

	@Override
	public List<Object[]> getTopCustomers(LocalDateTime fromDate, LocalDateTime toDate) {
	    return chiTietDonHangRepository.getTopCustomers(fromDate, toDate);
	}






}