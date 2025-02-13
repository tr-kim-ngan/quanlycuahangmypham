package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietDonHangRepository;
import com.kimngan.ComesticAdmin.repository.DonHangRepository;
import com.kimngan.ComesticAdmin.repository.HoaDonRepository;
import com.kimngan.ComesticAdmin.repository.NguoiDungRepository;
import org.springframework.http.HttpStatus;

@Service
public class DonHangServiceImpl implements DonHangService {
	@Autowired
	private NguoiDungRepository nguoiDungRepository;
	@Autowired
	private DonHangRepository donHangRepository;
	@Autowired
	private GioHangService gioHangService;

	@Autowired
	private ChiTietDonHangRepository chiTietDonHangRepository;
	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private SanPhamService sanPhamService;
	@Autowired
	private HoaDonRepository hoaDonRepository;

	@Override
	public DonHang createDonHang(DonHang donHang) {

		return donHangRepository.save(donHang);
	}

	@Override
	public DonHang updateDonHang(DonHang donHang) {
		System.out.println("💾 Cập nhật đơn hàng: " + donHang.getMaDonHang() + " - Trạng thái mới: "
				+ donHang.getTrangThaiDonHang());

		// 🔥 Nếu đơn hàng hoàn thành mà chưa có hóa đơn thì tạo hóa đơn
		if ("Đã hoàn thành".equals(donHang.getTrangThaiDonHang())) {
			HoaDon hoaDon = hoaDonRepository.findByDonHang(donHang);
			if (hoaDon == null) {
				System.out.println("✅ Tạo hóa đơn mới...");
				hoaDon = new HoaDon();
				hoaDon.setDonHang(donHang);
				hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
				hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
				hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
				hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
				hoaDon.setSoDienThoaiNhanHang(donHang.getSdtNhanHang());
				hoaDon.setTrangThaiThanhToan("Chưa xác nhận");

				hoaDonRepository.save(hoaDon);
				System.out.println("✅ Hóa đơn đã được tạo và lưu vào database!");
			} else {
				System.out.println("❌ Hóa đơn đã tồn tại, không tạo mới.");
			}
		}

		return donHangRepository.save(donHang);
	}

	@Override
	public void deleteDonHang(Integer maDonHang) {
		if (donHangRepository.existsById(maDonHang)) {
			donHangRepository.deleteById(maDonHang);
		} else {
			throw new RuntimeException("Đơn hà ng không tồn tại với mã: " + maDonHang);
		}

	}

	@Override
	public DonHang getDonHangById(Integer maDonHang) {
		System.out.println("Lấy đơn hàng với mã: " + maDonHang);
		Optional<DonHang> donHang = donHangRepository.findById(maDonHang);
		if (donHang.isEmpty()) {
			System.out.println(
					" public DonHang getDonHangById(Integer maDonHang) Không tìm thấy đơn hàng với mã: " + maDonHang);
			return null; // Hoặc throw custom exception
		}
		return donHang.get();
	}

	@Override
	public List<DonHang> getAllDonHangs() {
		//
		return donHangRepository.findAll();
	}

//	@Override
//	public List<DonHang> getOrdersByUser(String username) {
//		// 
//		// Lấy người dùng dựa trên username
//		NguoiDung nguoiDung = nguoiDungRepository.findByTenNguoiDung(username);
//		//Optional<NguoiDung> optionalUser = nguoiDungRepository.findByTenNguoiDung(username);
//
//		if (nguoiDung == null) {
//			throw new IllegalArgumentException("Người dùng không tồn tại");
//		}
//		// Lấy danh sách đơn hàng dựa trên người dùng
//		return donHangRepository.findByNguoiDung(nguoiDung);
//	}

	@Override
	public DonHang createOrderFromCart(String username, String address, String phone) {
		System.out.println("Bắt đầu tạo đơn hàng cho người dùng: " + username);

		// Lấy người dùng dựa trên username
		NguoiDung nguoiDung = nguoiDungRepository.findByTenNguoiDung(username);
		if (nguoiDung == null) {
			throw new IllegalArgumentException("Người dùng không tồn tại");
		}

		// Lấy giỏ hàng của người dùng
		GioHang gioHang = gioHangService.getCartByUser(nguoiDung);
		if (gioHang == null || gioHang.getChiTietGioHangs().isEmpty()) {
			throw new IllegalArgumentException("Giỏ hàng trống");
		}

		// Tạo đơn hàng
		DonHang donHang = new DonHang();
		donHang.setNguoiDung(nguoiDung);
		donHang.setNgayDat(LocalDateTime.now());
		donHang.setDiaChiGiaoHang(address); // Lưu địa chỉ giao hàng
		donHang.setSdtNhanHang(phone); // Lưu số điện thoại
		donHang.setTrangThaiDonHang("Đang xử lý");

		// Gán phí vận chuyển (giá trị mặc định là 50,000 VND)
		BigDecimal phiVanChuyen = BigDecimal.valueOf(50000); // Giá trị mặc định
		donHang.setPhiVanChuyen(phiVanChuyen);

		// Tính tổng giá trị đơn hàng và lưu các chi tiết đơn hàng vào danh sách tạm
		BigDecimal tongGiaTri = BigDecimal.ZERO;
		List<ChiTietDonHang> chiTietDonHangList = new ArrayList<>();

		for (ChiTietGioHang chiTiet : gioHang.getChiTietGioHangs()) {
			SanPham sanPham = chiTiet.getSanPham();

			// Tạo đối tượng ChiTietDonHang
			ChiTietDonHang chiTietDonHang = new ChiTietDonHang();
			chiTietDonHang.setSanPham(sanPham);
			chiTietDonHang.setSoLuong(chiTiet.getSoLuong());
			chiTietDonHang.setGiaTaiThoiDiemDat(sanPham.getDonGiaBan());

			tongGiaTri = tongGiaTri.add(sanPham.getDonGiaBan().multiply(BigDecimal.valueOf(chiTiet.getSoLuong())));

			chiTietDonHangList.add(chiTietDonHang);
		}

		// Kiểm tra và đảm bảo tổng giá trị đơn hàng không phải là null
		if (tongGiaTri == null) {
			throw new IllegalStateException("Tổng giá trị đơn hàng không thể là null");
		}

		// Cộng thêm phí vận chuyển vào tổng giá trị đơn hàng
		donHang.setTongGiaTriDonHang(tongGiaTri.add(phiVanChuyen));

		// Lưu đơn hàng vào cơ sở dữ liệu trước để có `maDonHang`
		donHang = donHangRepository.save(donHang);

		// Sau khi lưu đơn hàng, lưu các chi tiết đơn hàng liên quan
		for (ChiTietDonHang chiTietDonHang : chiTietDonHangList) {
			chiTietDonHang.setDonHang(donHang);
			chiTietDonHang
					.setId(new ChiTietDonHangId(donHang.getMaDonHang(), chiTietDonHang.getSanPham().getMaSanPham()));
			chiTietDonHangRepository.save(chiTietDonHang);
		}

		// Xóa giỏ hàng sau khi đặt hàng
		gioHangService.clearCart(nguoiDung);

		return donHang;
	}

	@Override
	public DonHang save(DonHang donHang) {
		return donHangRepository.save(donHang);
	}

	@Override
	public DonHang createTemporaryOrder(String username, List<Integer> productIds) {
		NguoiDung nguoiDung = nguoiDungRepository.findByTenNguoiDung(username);
		GioHang gioHang = gioHangService.getCartByUser(nguoiDung);

		DonHang donHang = new DonHang();
		donHang.setNguoiDung(nguoiDung);
		donHang.setTrangThaiDonHang("Chưa hoàn tất");
		donHang.setNgayDat(LocalDateTime.now());
		donHang.setTongGiaTriDonHang(BigDecimal.ZERO);

		// Thêm chi tiết đơn hàng
		BigDecimal totalPrice = BigDecimal.ZERO;
		for (ChiTietGioHang chiTiet : gioHang.getChiTietGioHangs()) {
			if (productIds.contains(chiTiet.getSanPham().getMaSanPham())) {
				ChiTietDonHang chiTietDonHang = new ChiTietDonHang();
				chiTietDonHang.setDonHang(donHang);
				chiTietDonHang.setSanPham(chiTiet.getSanPham());
				chiTietDonHang.setSoLuong(chiTiet.getSoLuong());
				chiTietDonHang.setGiaTaiThoiDiemDat(chiTiet.getSanPham().getDonGiaBan());

				totalPrice = totalPrice
						.add(chiTiet.getSanPham().getDonGiaBan().multiply(BigDecimal.valueOf(chiTiet.getSoLuong())));
			}
		}

		donHang.setTongGiaTriDonHang(totalPrice);

		return donHangRepository.save(donHang);
	}

	@Override
	public void confirmOrder(Integer maDonHang, String address, String phone) {
		DonHang donHang = donHangRepository.findById(maDonHang)
				.orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

		donHang.setDiaChiGiaoHang(address);
		donHang.setSdtNhanHang(phone);
		donHang.setTrangThaiDonHang("Hoàn tất");
		donHangRepository.save(donHang);

	}

	@Override
	public Page<DonHang> getAllDonHangs(Pageable pageable) {
		//
		return donHangRepository.findAll(pageable);
	}

	@Override
	public void completeOrder(DonHang donHang) {
		donHang.setTrangThaiDonHang("Đã hoàn thành"); // Đặt trạng thái đơn hàng là 'Đã hoàn thành'
		donHangRepository.save(donHang);

		// Ghi log để kiểm tra việc hoàn thành đơn hàng
		System.out.println("Hoàn thành đơn hàng cho người dùng: " + donHang.getNguoiDung().getMaNguoiDung());

		// Cập nhật loại khách hàng dựa trên tổng giá trị đơn hàng
		nguoiDungService.updateLoaiKhachHangBasedOnTotalOrders(donHang.getNguoiDung().getMaNguoiDung());
	}

	@Override
	public List<DonHang> getAllOrdersSortedByNgayDat() {
		//
		return donHangRepository.findAllByOrderByNgayDatAsc();
	}

	@Override
	public Page<DonHang> getDonHangsByStatus(String status, Pageable pageable) {
		//
		return donHangRepository.findByTrangThaiDonHang(status, pageable);
	}

	@Override
	public long countOrders() {
		//
		return donHangRepository.count();
	}

	@Override
	public long countByTrangThaiDonHang(String trangThaiDonHang) {
		//
		return donHangRepository.countByTrangThaiDonHang(trangThaiDonHang);
	}

	@Override
	public Page<DonHang> getOrdersByUser(String username, Pageable pageable) {
		return donHangRepository.findByNguoiDungTenNguoiDung(username, pageable);
	}

	@Override
	public Page<DonHang> getOrdersByUserAndStatus(String username, String status, Pageable pageable) {
		// TODO Auto-generated method stub
		return donHangRepository.findByNguoiDungTenNguoiDungAndTrangThaiDonHang(username, status, pageable);

	}

	@Override
	public Page<DonHang> getLatestOrdersByUser(String username, Pageable pageable) {
		// TODO Auto-generated method stub
		return donHangRepository.findTopByNguoiDungTenNguoiDungOrderByNgayDatDesc(username, pageable);

	}

	@Override
	public List<DonHang> findOrdersByShipper(NguoiDung shipper) {
		System.out.println("🔥 Đang gọi findOrdersByShipper() với Shipper ID: " + shipper.getMaNguoiDung());

		List<DonHang> orders = donHangRepository.findByShipper(shipper);

		// Sắp xếp giảm dần theo mã đơn hàng
		orders.sort(Comparator.comparing(DonHang::getMaDonHang).reversed());
		System.out.println("🛒 Tổng số đơn hàng tìm thấy: " + orders.size());
		for (DonHang dh : orders) {
			System.out.println("📦 Đơn hàng: " + dh.getMaDonHang() + " - Trạng thái: " + dh.getTrangThaiDonHang());
		}
		return orders;
	}

	@Override
	public List<DonHang> findOrdersByShipperAndStatus(NguoiDung shipper, String status) {
		// TODO Auto-generated method stub
		return donHangRepository.findByNguoiDungAndTrangThaiDonHang(shipper, status);
	}

	@Override
	public List<String> getDisplayedStatuses(DonHang donHang) {
		// TODO Auto-generated method stub
		List<String> statuses = Arrays.asList("Đang xử lý", "Đã xác nhận", "Đang chuẩn bị hàng", "Đang giao hàng",
				"Đã hoàn thành", "Đã hủy");

		// ✅ Chỉ hiển thị trạng thái đã được admin xác nhận
		int currentIndex = statuses.indexOf(donHang.getTrangThaiDonHang());
		if (currentIndex == -1) {
			return statuses.subList(0, 1); // Nếu có lỗi, chỉ hiển thị trạng thái đầu tiên
		}
		return statuses.subList(0, currentIndex + 1);
	}

	@Override
	public void capNhatTrangThai(DonHang donHang, String trangThaiMoi) {
	    // Lấy lịch sử cũ nếu có
	    String lichSuCu = (donHang.getLichSuTrangThai() != null) ? donHang.getLichSuTrangThai() : "";

	    // Ghi nhận trạng thái mới mà không cần admin
	    String lichSuMoi = lichSuCu + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
	        + " - " + trangThaiMoi + "\n";

	    // Cập nhật đơn hàng
	    donHang.setLichSuTrangThai(lichSuMoi);
	    donHang.setTrangThaiDonHang(trangThaiMoi);
	    donHangRepository.save(donHang);
	}


}
