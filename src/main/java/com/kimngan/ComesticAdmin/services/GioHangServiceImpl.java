package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHangId;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietGioHangRepository;
import com.kimngan.ComesticAdmin.repository.GioHangRepository;

@Service
public class GioHangServiceImpl implements GioHangService {

	@Autowired
	private GioHangRepository gioHangRepository;
	@Autowired
	private ChiTietGioHangRepository chiTietGioHangRepository;

	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private ChiTietGioHangService chiTietGioHangService;


	@Override
	public GioHang getOrCreateGioHang(NguoiDung nguoiDung) {
		// TODO Auto-generated method stub
		// Tìm giỏ hàng của người dùng, nếu chưa có sẽ tạo mới
		return gioHangRepository.findByNguoiDung(nguoiDung).orElseGet(() -> {
			GioHang newGioHang = new GioHang();
			newGioHang.setNguoiDung(nguoiDung);
			newGioHang.setNgayTao(LocalDate.now());
			return gioHangRepository.save(newGioHang);
		});
	}

	@Override
	public void addToCart(NguoiDung nguoiDung, SanPham sanPham, Integer quantity) {
		GioHang gioHang = getOrCreateGioHang(nguoiDung);

		// In ra thông tin người dùng, sản phẩm, và số lượng nhập vào
		System.out.println("Người dùng: " + nguoiDung.getTenNguoiDung());
		System.out.println("Sản phẩm: " + sanPham.getTenSanPham() + " (ID: " + sanPham.getMaSanPham() + ")");
		System.out.println("Số lượng nhập vào: " + quantity);

		// Gọi phương thức addOrUpdateChiTietGioHang từ ChiTietGioHangService để thêm
		// hoặc cập nhật sản phẩm trong giỏ hàng
		chiTietGioHangService.addOrUpdateChiTietGioHang(gioHang, sanPham, quantity);
	}

	@Override
	public void removeFromCart(NguoiDung nguoiDung, SanPham sanPham) {
		GioHang gioHang = getOrCreateGioHang(nguoiDung);
		chiTietGioHangRepository.findByGioHangAndSanPham(gioHang, sanPham).ifPresent(chiTietGioHangRepository::delete);

	}

	@Override
	public void clearCartAfterCheckout(NguoiDung nguoiDung) {
		GioHang gioHang = getOrCreateGioHang(nguoiDung);
		chiTietGioHangRepository.deleteByGioHang(gioHang);

	}

	@Override
	public List<ChiTietGioHang> viewCartItems(NguoiDung nguoiDung) {
		GioHang gioHang = getOrCreateGioHang(nguoiDung);
		return chiTietGioHangRepository.findByGioHang(gioHang);
	}

	@Override
	public void updateCartItemQuantity(NguoiDung nguoiDung, SanPham sanPham, Integer newQuantity) {
		// Lấy giỏ hàng của người dùng
		GioHang gioHang = getOrCreateGioHang(nguoiDung);

		// Tìm chi tiết giỏ hàng cho sản phẩm cần cập nhật
		Optional<ChiTietGioHang> optionalChiTietGioHang = chiTietGioHangRepository.findByGioHangAndSanPham(gioHang,
				sanPham);

		if (optionalChiTietGioHang.isPresent()) {
			ChiTietGioHang chiTietGioHang = optionalChiTietGioHang.get();
			// Thay đổi số lượng thành giá trị mới, không cộng thêm
			chiTietGioHang.setSoLuong(newQuantity);
			System.out.println("Cập nhật sản phẩm của updateCartItemQuantity: " + sanPham.getMaSanPham()
					+ " của updateCartItemQuantity thành số lượng mới: " + newQuantity);

			// Lưu thay đổi vào database
			chiTietGioHangRepository.save(chiTietGioHang);
		} else {
			throw new RuntimeException("Sản phẩm không có trong giỏ hàng của bạn.");
		}
	}

	@Override
	public GioHang getCartByUser(NguoiDung nguoiDung) {
		// TODO Auto-generated method stub
		return gioHangRepository.findByNguoiDung(nguoiDung)
				.orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại!"));
	}

	@Override
	public void clearCart(NguoiDung nguoiDung) {
		GioHang gioHang = nguoiDung.getGioHang();
	    if (gioHang != null) {
	        List<ChiTietGioHang> cartItems = chiTietGioHangRepository.findByGioHang(gioHang);
	        if (!cartItems.isEmpty()) {
	            chiTietGioHangRepository.deleteAll(cartItems);
	        }
	    }

	}

	@Override
	public List<ChiTietGioHang> getSelectedItems(NguoiDung nguoiDung, List<Integer> productIds) {
		// TODO Auto-generated method stub
		 GioHang gioHang = getOrCreateGioHang(nguoiDung);
		    List<ChiTietGioHang> allItems = chiTietGioHangRepository.findByGioHang(gioHang);

		    // Lọc các sản phẩm theo danh sách productIds được chọn
		    return allItems.stream()
		            .filter(item -> productIds.contains(item.getSanPham().getMaSanPham()))
		            .collect(Collectors.toList());
	}

	

}
