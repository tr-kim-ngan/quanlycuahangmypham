package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietDonNhapHangRepository;
import com.kimngan.ComesticAdmin.repository.DanhGiaRepository;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;

@Service
public class SanPhamServiceImpl implements SanPhamService {

	@Autowired
	private SanPhamRepository sanPhamRepository;
	@Autowired
	private DanhGiaRepository danhGiaRepository;
	@Autowired
	private ChiTietDonHangService chiTietDonHangService;
	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private ChiTietDonNhapHangRepository chiTietDonNhapHangRepository;

	@Override
	public List<SanPham> getAll() {
		// TODO Auto-generated method stub
		return sanPhamRepository.findAll();
	}

	@Override
	public SanPham create(SanPham sanPham) {
		// Kiểm tra xem sản phẩm có cùng tên đã tồn tại và đang hoạt động không
		if (sanPhamRepository.existsByTenSanPhamAndTrangThai(sanPham.getTenSanPham(), true)) {
			return null; // Sản phẩm đã tồn tại và đang hoạt động
		}
		sanPham.setTrangThai(true); // Đảm bảo trạng thái là hoạt động khi tạo mới
		// Kiểm tra nếu soLuongTonKho bị null thì đặt về 0
		if (sanPham.getSoLuongTonKho() == null) {
			sanPham.setSoLuongTonKho(0);
		}

		return sanPhamRepository.save(sanPham);
	}

	@Override
	public SanPham findById(Integer maSanPham) {
		Optional<SanPham> optionalDanhMuc = sanPhamRepository.findById(maSanPham);
		return optionalDanhMuc.orElse(null); // Trả về null nếu không tìm thấy danh mục
	}

	@Override
	public Boolean delete(Integer maSanPham) {
		Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(maSanPham);
		if (sanPhamOpt.isPresent()) {
			SanPham sanPham = sanPhamOpt.get();
			sanPham.setTrangThai(false); // Đánh dấu trạng thái là ngừng hoạt động
			sanPhamRepository.save(sanPham);
			return true;
		}
		return false;
	}

	@Override
	public Boolean update(SanPham sanPham) {
		// Kiểm tra xem sản phẩm có tồn tại trước khi cập nhật
		if (sanPhamRepository.existsById(sanPham.getMaSanPham())) {
			sanPhamRepository.save(sanPham);
			return true;
		}
		return false;
	}

	@Override
	public Page<SanPham> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findAll(pageable);
	}

	@Override
	public Page<SanPham> searchActiveByName(String tenSanPham, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByTenSanPhamContainingIgnoreCaseAndTrangThai(tenSanPham, true, pageable);
	}

	@Override
	public Page<SanPham> findAllActive(Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByTrangThaiTrue(pageable);
	}

	@Override
	public Optional<SanPham> findByIdOptional(Integer maSanPham) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findById(maSanPham);
	}

	@Override
	public Boolean existsByTenSanPham(String tenSanPham) {
		return sanPhamRepository.existsByTenSanPhamAndTrangThai(tenSanPham, true);
	}

	@Override
	public List<SanPham> findByTrangThai(Boolean trangThai) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByTrangThai(trangThai);
	}

	@Override
	public List<SanPham> findByMaSanPhamInAndTrangThai(List<Integer> maSanPham, Boolean trangThai) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByMaSanPhamInAndTrangThai(maSanPham, trangThai);
	}

	@Override
	public Page<SanPham> findByDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByDanhMucAndTrangThai(maDanhMuc, trangThai, pageable);
	}

	@Override
	public Page<SanPham> getAllActiveProducts(Pageable pageable) {
		return sanPhamRepository.findByTrangThaiTrue(pageable);
	}

//Dùng khi bạn muốn lấy tất cả sản phẩm có trạng thái active 
	// và có trong chi tiết đơn nhập hàng (bất kể danh mục).
	@Override
	public Page<SanPham> getProductsInOrderDetails(Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findActiveProductsInOrderDetails(pageable);
	}

//Dùng khi bạn muốn lấy sản phẩm có trạng thái active theo một danh mục cụ thể 
	// và có trong chi tiết đơn nhập hàng.
	@Override
	public Page<SanPham> findActiveProductsInOrderDetailsByCategory(Integer maDanhMuc, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findActiveProductsInOrderDetailsByCategory(maDanhMuc, pageable);
	}

	@Override
	public List<SanPham> findByDanhMucAndTrangThai(Integer maDanhMuc, Boolean trangThai) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByDanhMuc_MaDanhMucAndTrangThai(maDanhMuc, trangThai);
	}

	@Override
	public List<SanPham> searchAllCategories(String keyword, Pageable pageable) {
		return sanPhamRepository.findByTenSanPhamContaining(keyword, pageable);
	}

	@Override
	public List<SanPham> searchByCategory(Integer categoryId, String keyword, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByDanhMuc_MaDanhMucAndTenSanPhamContaining(categoryId, keyword, pageable);
	}

	@Override
	public Page<SanPham> searchAllActiveProductsWithOrderDetails(String keyword, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.searchAllActiveProductsWithOrderDetails(keyword, pageable);
	}

	@Override
	public Page<SanPham> searchByCategoryWithOrderDetails(Integer categoryId, String keyword, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.searchByCategoryWithOrderDetails(categoryId, keyword, pageable);
	}

	@Override
	public Double getAverageRatingForProduct(Integer maSanPham) {
		// TODO Auto-generated method stub
		Double averageRating = danhGiaRepository.findAverageRatingBySanPhamId(maSanPham);
		return (averageRating != null) ? averageRating : 0.0; // Nếu chưa có đánh giá, trả về 0

	}

	@Override
	public Page<SanPham> findByDanhMucAndTrangThaiWithPagination(Integer maDanhMuc, Boolean trangThai,
			Pageable pageable) {
		// TODO Auto-generated method stub
		// Sử dụng phương thức hiện tại để tìm sản phẩm theo danh mục và trạng thái
		List<SanPham> sanPhams = sanPhamRepository.findByDanhMuc_MaDanhMucAndTrangThai(maDanhMuc, trangThai);

		// Chuyển đổi từ List sang Page bằng Pageable
		int start = Math.min((int) pageable.getOffset(), sanPhams.size());
		int end = Math.min((start + pageable.getPageSize()), sanPhams.size());
		List<SanPham> subList = sanPhams.subList(start, end);

		return new PageImpl<>(subList, pageable, sanPhams.size());
	}

	@Override
	public Page<SanPham> searchByCategoryAndName(Integer maDanhMuc, String keyword, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByDanhMuc_MaDanhMucAndTenSanPhamContainingAndTrangThai(maDanhMuc, keyword, true,
				pageable);

	}

	@Override
	public List<SanPham> findAllWithDanhGiasAndTrangThaiTrue() {
		// TODO Auto-generated method stub
		return sanPhamRepository.findAllWithDanhGiasAndTrangThaiTrue();

	}

	@Override
	public List<SanPham> findAllWithDanhGiasAndTrangThaiTrueBySoSao(int soSao) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findAllWithDanhGiasAndTrangThaiTrueBySoSao(soSao);
	}

	@Override
	public long countActiveProducts() {
		// TODO Auto-generated method stub
		return sanPhamRepository.countByTrangThaiTrue();
	}

	@Override
	public Page<SanPham> findActiveProductsByBrand(Integer maThuongHieu, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByThuongHieu_MaThuongHieuAndTrangThai(maThuongHieu, true, pageable);
	}

	@Override
	public Page<SanPham> findAllActiveByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findAllActiveByPriceRange(minPrice, maxPrice, pageable);
	}

	@Override
	public Page<SanPham> findActiveProductsByCategoryAndPrice(Integer maDanhMuc, BigDecimal minPrice,
			BigDecimal maxPrice, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findActiveProductsByCategoryAndPrice(maDanhMuc, minPrice, maxPrice, pageable);
	}

	@Override
	public List<SanPham> findByIdIn(List<Integer> maSanPhams) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByMaSanPhamIn(maSanPhams);
	}

	@Override
	public Page<SanPham> findAllActiveWithStock(Pageable pageable) {
		return sanPhamRepository.findAllActiveWithStock(pageable);
	}

	@Override
	public void capNhatSoLuongTonKho(Integer maSanPham) {
		// Lấy tổng số lượng nhập từ chi tiết đơn nhập hàng
		int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);

		// Lấy sản phẩm từ database
		SanPham sanPham = sanPhamRepository.findById(maSanPham).orElse(null);
		if (sanPham == null)
			return; // Nếu sản phẩm không tồn tại, thoát

		// Tính số lượng tồn kho = Tổng số lượng nhập - Số lượng trên kệ
		int soLuongTonKho = tongSoLuongNhap - sanPham.getSoLuong();

		// Cập nhật vào database
		sanPham.setSoLuongTonKho(soLuongTonKho);
		sanPhamRepository.save(sanPham);
	}

	@Override
	public Page<SanPham> findByTrangThai(Boolean trangThai, Pageable pageable) {
		// TODO Auto-generated method stub
		return sanPhamRepository.findByTrangThai(trangThai, pageable);
	}

	@Override
	public Integer getTotalImportedQuantity(Integer maSanPham) {
		LocalDate lastStockEmptyTime = chiTietDonNhapHangRepository.findLastTimeStockEmpty(maSanPham);

		if (lastStockEmptyTime == null) {
			// Nếu chưa từng hết hàng, lấy tổng tất cả các lần nhập
			return chiTietDonNhapHangRepository.getTotalImportedQuantityAfterStockEmpty(maSanPham,
					LocalDate.of(2023, 1, 1));
		}

		return chiTietDonNhapHangRepository.getTotalImportedQuantityAfterStockEmpty(maSanPham, lastStockEmptyTime);
	}

	@Override
	public Integer getSoLuongTrenKe(Integer maSanPham) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSoLuongTonKho(Integer maSanPham) {
		// TODO Auto-generated method stub
		return null;
	}

}
