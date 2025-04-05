package com.kimngan.ComesticAdmin.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonHangId;
import com.kimngan.ComesticAdmin.entity.ChiTietGioHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.GioHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.ChiTietDonHangRepository;
import com.kimngan.ComesticAdmin.repository.DonHangRepository;
import com.kimngan.ComesticAdmin.repository.HoaDonRepository;
import com.kimngan.ComesticAdmin.repository.NguoiDungRepository;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;

import jakarta.transaction.Transactional;

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
	private SanPhamRepository sanPhamRepository;

	@Autowired
	private HoaDonRepository hoaDonRepository;
	//  Thêm biến offlineOrder để lưu tạm đơn hàng offline
	private final Map<Integer, ChiTietDonHang> offlineOrder = new HashMap<>();

	@Override
	public DonHang createDonHang(DonHang donHang) {

		return donHangRepository.save(donHang);
	}

	@Override
	public DonHang updateDonHang(DonHang donHang) {
		System.out.println(" Cập nhật đơn hàng: " + donHang.getMaDonHang() + " - Trạng thái mới: "
				+ donHang.getTrangThaiDonHang());

		//  Nếu đơn hàng hoàn thành mà chưa có hóa đơn thì tạo hóa đơn
		if ("Đã hoàn thành".equals(donHang.getTrangThaiDonHang())) {
			HoaDon hoaDon = hoaDonRepository.findByDonHang(donHang);
			if (hoaDon == null) {
				System.out.println(" Tạo hóa đơn mới...");
				hoaDon = new HoaDon();
				hoaDon.setDonHang(donHang);
				hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
				hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
				hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
				hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
				hoaDon.setSoDienThoaiNhanHang(donHang.getSdtNhanHang());
				hoaDon.setTrangThaiThanhToan("Chưa xác nhận");

				//  Nếu không có giá trị từ `DonHang`, đặt mặc định là "Tiền mặt"
				hoaDon.setPhuongThucThanhToan("COD");
			
				  // Nếu đơn hàng có tổng tiền = 0 (thanh toán VNPay), tự động xác nhận hóa đơn
		        if (donHang.getTongGiaTriDonHang().compareTo(BigDecimal.ZERO) == 0) {
		            hoaDon.setPhuongThucThanhToan("VNPay");
		            hoaDon.setTrangThaiThanhToan("Đã xác nhận"); //  Cập nhật trạng thái hóa đơn
		            System.out.println(" Hóa đơn VNPay đã được cập nhật thành 'Đã xác nhận'!");
		        }

	           

				hoaDonRepository.save(hoaDon);
				System.out.println(" Hóa đơn đã được tạo và lưu vào database!");
			} else {
				System.out.println(" Hóa đơn đã tồn tại, không tạo mới.");
	           
			}
			
		}

		//  Lưu lại lịch sử trạng thái
		String lichSuCu = donHang.getLichSuTrangThai() == null ? "" : donHang.getLichSuTrangThai() + "\n";
		String trangThaiMoi =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
				+ " - " + donHang.getTrangThaiDonHang();
		donHang.setLichSuTrangThai(lichSuCu + trangThaiMoi);

		System.out.println(" Cập nhật đơn hàng: " + donHang.getMaDonHang());
		System.out.println(" Trạng thái mới: " + donHang.getTrangThaiDonHang());
		System.out.println(" Hình ảnh giao hàng: " + donHang.getHinhAnhGiaoHang());

		return donHangRepository.save(donHang);
	}

	@Override
	public void deleteDonHang(Integer maDonHang) {
		if (donHangRepository.existsById(maDonHang)) {
			donHangRepository.deleteById(maDonHang);
		} else {
			throw new RuntimeException("Đơn hàng không tồn tại với mã: " + maDonHang);
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
		System.out.println(" Đang gọi findOrdersByShipper() với Shipper ID: " + shipper.getMaNguoiDung());

		List<DonHang> orders = donHangRepository.findByShipper(shipper);

		// Sắp xếp giảm dần theo mã đơn hàng
		orders.sort(Comparator.comparing(DonHang::getMaDonHang).reversed());
		System.out.println(" Tổng số đơn hàng tìm thấy: " + orders.size());
		for (DonHang dh : orders) {
			System.out.println(" Đơn hàng: " + dh.getMaDonHang() + " - Trạng thái: " + dh.getTrangThaiDonHang());
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

		//  Chỉ hiển thị trạng thái đã được admin xác nhận
		int currentIndex = statuses.indexOf(donHang.getTrangThaiDonHang());
		if (currentIndex == -1) {
			return statuses.subList(0, 1); // Nếu có lỗi, chỉ hiển thị trạng thái đầu tiên
		}
		return statuses.subList(0, currentIndex + 1);
	}

	@Override
	public void capNhatTrangThai(DonHang donHang, String trangThaiMoi) {
		// Kiểm tra nếu trạng thái cuối cùng đã lưu trùng với trạng thái mới
		if (donHang.getLichSuTrangThai() != null && donHang.getLichSuTrangThai().contains(trangThaiMoi)) {
			System.out.println(" Trạng thái đã tồn tại, không lưu trùng: " + trangThaiMoi);
			return; // Không lưu trùng
		}

		// Lưu trạng thái mới vào lịch sử
		String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		String lichSuMoi = "🕘 " + thoiGian + " - " + trangThaiMoi;

		// Thêm vào lịch sử (nếu có dữ liệu cũ thì nối thêm)
		if (donHang.getLichSuTrangThai() == null || donHang.getLichSuTrangThai().isEmpty()) {
			donHang.setLichSuTrangThai(lichSuMoi);
		} else {
			donHang.setLichSuTrangThai(donHang.getLichSuTrangThai() + "\n" + lichSuMoi);
		}

		// Cập nhật vào database
		donHangRepository.save(donHang);
	}

	@Override
	public void updateOrderStatus(Integer maDonHang, String trangThaiMoi) {
		Optional<DonHang> optionalDonHang = donHangRepository.findById(maDonHang);
		if (optionalDonHang.isPresent()) {
			DonHang donHang = optionalDonHang.get();
			donHang.setTrangThaiDonHang(trangThaiMoi); // Cập nhật trạng thái
			addOrderStatusHistory(maDonHang, trangThaiMoi);
			donHangRepository.save(donHang);
		} else {
			throw new RuntimeException("Không tìm thấy đơn hàng với mã: " + maDonHang);
		}
	}

	@Override
	public void updatePaymentStatus(Integer maDonHang, String trangThaiThanhToan) {
		Optional<DonHang> optionalDonHang = donHangRepository.findById(maDonHang);
		if (optionalDonHang.isPresent()) {
			DonHang donHang = optionalDonHang.get();
			donHang.setTrangThaiChoXacNhan(trangThaiThanhToan); // Cập nhật trạng thái thanh toán
			addOrderStatusHistory(maDonHang, "Cập nhật trạng thái thanh toán: " + trangThaiThanhToan);
			donHangRepository.save(donHang);
		} else {
			throw new RuntimeException("Không tìm thấy đơn hàng với mã: " + maDonHang);
		}
	}

	@Override
	public void addOrderStatusHistory(Integer maDonHang, String trangThaiMoi) {
		Optional<DonHang> optionalDonHang = donHangRepository.findById(maDonHang);
		if (optionalDonHang.isPresent()) {
			DonHang donHang = optionalDonHang.get();
			String currentHistory = donHang.getLichSuTrangThai();
			String updatedHistory = (currentHistory == null ? "" : currentHistory + "\n") + LocalDateTime.now() + ": "
					+ trangThaiMoi;
			donHang.setLichSuTrangThai(updatedHistory);
			donHangRepository.save(donHang);
		}
	}

	@Override
	public List<ChiTietDonHang> getCurrentOfflineOrder() {
		// TODO Auto-generated method stub
		return new ArrayList<>(offlineOrder.values());
	}

	@Override
	public void addToOfflineOrder(SanPham sanPham, int quantity) {
		if (offlineOrder.containsKey(sanPham.getMaSanPham())) {
			ChiTietDonHang chiTiet = offlineOrder.get(sanPham.getMaSanPham());
			chiTiet.setSoLuong(chiTiet.getSoLuong() + quantity); //  Cập nhật số lượng trực tiếp
		} else {
			//  Dùng constructor có sẵn của ChiTietDonHang
			ChiTietDonHang chiTiet = new ChiTietDonHang();
			chiTiet.setSanPham(sanPham);
			chiTiet.setSoLuong(quantity);
			offlineOrder.put(sanPham.getMaSanPham(), chiTiet);
		}
	}

	@Override
	public void removeFromOfflineOrder(Integer sanPhamId) {
		// TODO Auto-generated method stub
		offlineOrder.remove(sanPhamId);
	}

	@Override
	public BigDecimal calculateTotalPrice() {
		BigDecimal total = BigDecimal.ZERO;
		for (ChiTietDonHang item : offlineOrder.values()) {
			total = total.add(item.getSanPham().getDonGiaBan().multiply(BigDecimal.valueOf(item.getSoLuong())));
		}
		return total;
	}

	@Override
	public boolean confirmOfflineOrder() {
		if (offlineOrder.isEmpty()) {
			return false;
		}
		offlineOrder.clear();
		return true;
	}

	@Override
	@Transactional
	public void processOfflineOrder(List<Integer> productIds, List<Integer> quantities) {
	    offlineOrder.clear();

	    System.out.println(" Nhận dữ liệu vào processOfflineOrder:");
	    System.out.println(" Sản phẩm nhận được: " + productIds);
	    System.out.println(" Số lượng nhận được: " + quantities);

	    List<SanPham> selectedProducts = sanPhamRepository.findByIdInWithKhuyenMai(productIds);

	    for (int i = 0; i < selectedProducts.size(); i++) {
	        SanPham sanPham = selectedProducts.get(i);
	        Hibernate.initialize(sanPham.getKhuyenMais());

	        Integer quantity = (quantities.get(i) != null) ? quantities.get(i) : 1;
	        if (quantity > sanPham.getSoLuong()) {
	            quantity = sanPham.getSoLuong();
	        }

	        // 🔥 Đảm bảo giá tại thời điểm đặt hàng không bị null
	        BigDecimal giaGoc = sanPham.getDonGiaBan();
	        if (giaGoc == null) {
	            System.out.println(" Cảnh báo: Sản phẩm ID " + sanPham.getMaSanPham() + " không có giá, gán 0.");
	            giaGoc = BigDecimal.ZERO;
	        }

	        //  Tính giá sau khuyến mãi nếu có
	        BigDecimal giaSauGiam = giaGoc;
	        if (!sanPham.getKhuyenMais().isEmpty()) {
	            Optional<KhuyenMai> highestKhuyenMai = sanPham.getKhuyenMais().stream()
	                .filter(KhuyenMai::getTrangThai)
	                .max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

	            if (highestKhuyenMai.isPresent()) {
	                BigDecimal phanTramGiam = highestKhuyenMai.get().getPhanTramGiamGia();
	                giaSauGiam = giaGoc.subtract(giaGoc.multiply(phanTramGiam).divide(BigDecimal.valueOf(100)));
	            }
	        }

	        // 🔥 Nếu giá sau giảm vẫn bị null, gán về 0
	        if (giaSauGiam == null) {
	            System.out.println(" Giá sau giảm bị null, đặt về 0.");
	            giaSauGiam = BigDecimal.ZERO;
	        }

	        //  Kiểm tra trước khi lưu vào ChiTietDonHang
	        System.out.println(" Kiểm tra giá trị trước khi lưu:");
	        System.out.println(" - ID sản phẩm: " + sanPham.getMaSanPham());
	        System.out.println(" - Giá gốc: " + giaGoc);
	        System.out.println(" - Giá sau giảm: " + giaSauGiam);
	        System.out.println(" - Số lượng: " + quantity);

	        ChiTietDonHang chiTiet = new ChiTietDonHang();
	        chiTiet.setSanPham(sanPham);
	        chiTiet.setSoLuong(quantity);
	        chiTiet.setGiaTaiThoiDiemDat(giaSauGiam); //  Đảm bảo không bị null

	        offlineOrder.put(sanPham.getMaSanPham(), chiTiet);
	    }
	}








	@Override
	public List<ChiTietDonHang> getOfflineOrderItems() {
		// TODO Auto-generated method stub
		return new ArrayList<>(offlineOrder.values());
	}

	@Override
	public Collection<ChiTietDonHang> getOfflineOrder() {
		// TODO Auto-generated method stub
		return chiTietDonHangRepository.findOfflineOrderWithKhuyenMai();
	}


	@Override
	@Transactional
	public boolean processAndGenerateInvoiceForOfflineOrder(String soDienThoaiKhach) {
	    System.out.println(" Đang xử lý tạo hóa đơn cho số điện thoại: " + soDienThoaiKhach);

	    if (offlineOrder.isEmpty()) {
	        System.out.println(" Lỗi: Không có sản phẩm trong offlineOrder.");
	        return false;
	    }
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
	    NguoiDung seller = nguoiDungRepository.findByTenNguoiDung(userDetails.getUsername());

	    NguoiDung khachHang;
	    boolean isGuest = false;

	  //  NguoiDung existingGuest = nguoiDungRepository.findByTenNguoiDung("Khách vãng lai");
	    Optional<NguoiDung> existingUser = nguoiDungRepository.findBySoDienThoai(soDienThoaiKhach);

//	    if (existingGuest != null) {
//	        khachHang = existingGuest;
//	    } else {
//	        khachHang = new NguoiDung();
//	        khachHang.setTenNguoiDung("Khách vãng lai");
//	        khachHang.setSoDienThoai("0000000000");
//	        khachHang.setDiaChi("Mua tại quầy KN");
//	        khachHang = nguoiDungRepository.save(khachHang);
//	    }
	    if (existingUser.isPresent()) {
	        khachHang = existingUser.get();
	    } else {
	        khachHang = new NguoiDung();
	        khachHang.setTenNguoiDung("Khách vãng lai");
	        khachHang.setSoDienThoai("0000000000");
	        khachHang.setDiaChi("Mua tại quầy KN");
	        khachHang = nguoiDungRepository.save(khachHang);
	        isGuest = true;
	    }

	    BigDecimal totalPrice = BigDecimal.ZERO;
	    List<ChiTietDonHang> chiTietList = new ArrayList<>();

	    for (ChiTietDonHang chiTiet : offlineOrder.values()) {
	        SanPham sanPham = chiTiet.getSanPham();
	        Integer quantity = chiTiet.getSoLuong(); //  Lấy đúng số lượng sản phẩm

	        BigDecimal giaGoc = sanPham.getDonGiaBan();
	        if (giaGoc == null) {
	            System.out.println(" Cảnh báo: Giá gốc của sản phẩm ID " + sanPham.getMaSanPham() + " bị null, gán 0.");
	            giaGoc = BigDecimal.ZERO;
	        }

	        BigDecimal giaSauGiam = giaGoc;
	        if (!sanPham.getKhuyenMais().isEmpty()) {
	            Optional<KhuyenMai> highestKhuyenMai = sanPham.getKhuyenMais().stream()
	                .filter(km -> km.getTrangThai() &&
	                             km.getNgayBatDau().toLocalDate().isBefore(LocalDate.now()) &&
	                             km.getNgayKetThuc().toLocalDate().isAfter(LocalDate.now()))
	                .max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

	            if (highestKhuyenMai.isPresent()) {
	                BigDecimal phanTramGiam = highestKhuyenMai.get().getPhanTramGiamGia();
	                giaSauGiam = giaGoc.multiply(BigDecimal.ONE.subtract(phanTramGiam.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
	            }
	        }
	        //  Cập nhật giá vào chi tiết đơn hàng
	        chiTiet.setGiaTaiThoiDiemDat(giaSauGiam);

	        //  Tính tổng tiền
	        totalPrice = totalPrice.add(giaSauGiam.multiply(BigDecimal.valueOf(quantity)));
	        //  Trừ số lượng sản phẩm trên kệ và cộng số lượng đã bán
	     //  Trừ số lượng sản phẩm trên kệ
	        if (sanPham.getSoLuong() >= quantity) {
	            sanPham.setSoLuong(sanPham.getSoLuong() - quantity);
	        } else {
	            System.out.println(" Lỗi: Số lượng sản phẩm không đủ. Sản phẩm ID: " + sanPham.getMaSanPham());
	            return false;
	        }
	        //  Lưu cập nhật vào database
	        sanPhamRepository.save(sanPham);
	        System.out.println(" Kiểm tra giá trị đơn hàng:");
	        System.out.println(" - ID sản phẩm: " + sanPham.getMaSanPham());
	        System.out.println(" - Giá gốc: " + giaGoc);
	        System.out.println(" - Giá sau giảm: " + giaSauGiam);
	        System.out.println(" - Số lượng: " + quantity);
	        System.out.println(" - Thành tiền: " + giaSauGiam.multiply(BigDecimal.valueOf(quantity)));

	        chiTietList.add(chiTiet);
	    }

	 //  Lưu đơn hàng với thông tin khách hàng
	    DonHang donHang = new DonHang();
	    donHang.setNguoiDung(khachHang);
	    donHang.setNgayDat(LocalDateTime.now());
	    donHang.setTrangThaiDonHang("Đã hoàn thành");
	    donHang.setDiaChiGiaoHang("Mua tại quầy KN");
	    donHang.setSdtNhanHang(isGuest ? "0000000000" : khachHang.getSoDienThoai());
	    donHang.setHinhAnhGiaoHang(null);
	    donHang.setPhiVanChuyen(BigDecimal.ZERO);
	    donHang.setTongGiaTriDonHang(totalPrice);
	    donHang.setSeller(seller);
	    
	    donHang = donHangRepository.save(donHang);

	    //  Cập nhật mã đơn hàng cho từng chi tiết đơn hàng
	    for (ChiTietDonHang chiTiet : chiTietList) {
	        chiTiet.setDonHang(donHang);
	        chiTiet.setId(new ChiTietDonHangId(donHang.getMaDonHang(), chiTiet.getSanPham().getMaSanPham()));
	    }

	    chiTietDonHangRepository.saveAll(chiTietList);

	    HoaDon hoaDon = new HoaDon();
	    hoaDon.setDonHang(donHang);
	    hoaDon.setNgayXuatHoaDon(LocalDateTime.now());
	    hoaDon.setTongTien(donHang.getTongGiaTriDonHang());
	    hoaDon.setTrangThaiThanhToan("Đã hoàn thành");
	    hoaDon.setPhuongThucThanhToan("Tiền mặt (COD)");
	    hoaDon.setDiaChiGiaoHang(donHang.getDiaChiGiaoHang());
	    hoaDon.setTenNguoiNhan(donHang.getNguoiDung().getTenNguoiDung());
	    hoaDon.setSoDienThoaiNhanHang(isGuest ? "" : donHang.getSdtNhanHang());

	    hoaDonRepository.save(hoaDon);

	    offlineOrder.clear();

	    return true;
	}

	@Override
	public void updateDonHangVNPay(Integer maDonHang) {
		// TODO Auto-generated method stub
		Optional<DonHang> optionalDonHang = donHangRepository.findById(maDonHang);
		if (optionalDonHang.isPresent()) {
			DonHang donHang = optionalDonHang.get();
			//donHang.setTrangThaiDonHang("Đã thanh toán");
			//donHang.setTrangThaiDonHang("Đã hoàn thành");
			donHang.setTrangThaiDonHang("Đang xử lý");
			donHang.setTongGiaTriDonHang(BigDecimal.ZERO);
			donHangRepository.save(donHang);
			
		} else {
			throw new RuntimeException("Không tìm thấy đơn hàng với mã: " + maDonHang);
		}
	}

//	@Override
//	public List<DonHang> getOrdersByStatusAndExportStaff(String trangThai, Integer maNhanVien) {
//		// TODO Auto-generated method stub
//	    return donHangRepository.findByTrangThaiAndNhanVienXuatKho(trangThai, maNhanVien);
//
//	}
//
//	@Override
//	public List<DonHang> getOrdersByStatusesAndExportStaff(List<String> trangThaiList, Integer maNhanVien) {
//		// TODO Auto-generated method stub
//        return donHangRepository.findByTrangThaiDonHangInAndNhanVienXuatKho(trangThaiList, maNhanVien);
//	}

	@Override
	public List<DonHang> getDonHangsByStatus(String status) {
		// TODO Auto-generated method stub
		return donHangRepository.findByTrangThaiDonHang(status);
	}

	@Override
	public Page<DonHang> findDonHangsDaXuatKho(Pageable pageable){
		// TODO Auto-generated method stub
		return donHangRepository.findDonHangsDaXuatKho(pageable);
	}

	@Override
	public int getSoLuongTraHang(Integer maSanPham) {
		// TODO Auto-generated method stub
		 return donHangRepository.tinhTongSoLuongTraHang(maSanPham);
	}

	@Override
	public void clearOfflineOrder() {
		// TODO Auto-generated method stub
		offlineOrder.clear();	
	}

	@Override
	public void updateOfflineOrderQuantity(Integer sanPhamId, int quantity) {
		 if (offlineOrder.containsKey(sanPhamId)) {
		        ChiTietDonHang chiTiet = offlineOrder.get(sanPhamId);
		        chiTiet.setSoLuong(quantity); // Cập nhật số lượng mới
		    }
		
	}

	@Override
	public void saveOfflineOrder(String soDienThoai) {
	    List<ChiTietDonHang> orderItems = getCurrentOfflineOrder();
	    if (orderItems.isEmpty()) return;

	    DonHang donHang = new DonHang();
	    donHang.setNgayDat(LocalDateTime.now());
	    donHang.setDiaChiGiaoHang("Mua tại quầy KN");
	    donHang.setTrangThaiDonHang("Đã hoàn thành");
	    donHang.setPhiVanChuyen(BigDecimal.ZERO);
	    donHang.setSdtNhanHang(soDienThoai);

	    BigDecimal tongTien = BigDecimal.ZERO;
	    for (ChiTietDonHang chiTiet : orderItems) {
	        chiTiet.setDonHang(donHang);
	        tongTien = tongTien.add(chiTiet.getGiaTaiThoiDiemDat().multiply(BigDecimal.valueOf(chiTiet.getSoLuong())));
	    }

	    donHang.setTongGiaTriDonHang(tongTien);

	    NguoiDung nguoiDung = nguoiDungRepository.findBySoDienThoai(soDienThoai).orElse(null);
	    if (nguoiDung != null) {
	        donHang.setNguoiDung(nguoiDung);
	    } else {
	        // Gán user mặc định nếu không tìm thấy
	        donHang.setNguoiDung(null); // bạn cần có user mặc định
	    }

	    donHangRepository.save(donHang);
	    offlineOrder.clear();
	}

	@Override
	public Page<DonHang> getOrdersByUserAndDiaChi(String username, String diaChi, Pageable pageable) {
		// TODO Auto-generated method stub
		return donHangRepository.findByNguoiDung_TenNguoiDungAndDiaChiGiaoHang(username, diaChi, pageable);
	}

	@Override
	public Page<DonHang> getOrdersByUserAndDiaChiNot(String username, String diaChi, Pageable pageable) {
		// TODO Auto-generated method stub
		return donHangRepository.findByNguoiDung_TenNguoiDungAndDiaChiGiaoHangNot(username, diaChi, pageable);
	}

	@Override
	public List<DonHang> findByTrangThaiAndShipper(NguoiDung shipper, String trangThai) {
		// TODO Auto-generated method stub
	    return donHangRepository.findByTrangThaiDonHangAndShipper(trangThai, shipper);
	}

	@Override
	public int countByShipper(NguoiDung shipper) {
		// TODO Auto-generated method stub
	    return donHangRepository.countByShipper(shipper);
	}

	@Override
	public List<DonHang> findBySeller(NguoiDung seller) {
	    return donHangRepository.findBySeller(seller);
	}

	@Override
	public long demDonHangTrongKhoangNgay(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
		// TODO Auto-generated method stub
		 return donHangRepository.countByNgayDatBetween(fromDateTime, toDateTime);
	}

	@Override
	public List<Object[]> getSoDonHangTheoNgay(LocalDate fromDate, LocalDate toDate) {
	    if (fromDate == null) {
	        fromDate = LocalDate.now().minusDays(30);
	    }
	    if (toDate == null) {
	        toDate = LocalDate.now();
	    }

	    LocalDateTime fromDateTime = fromDate.atStartOfDay();
	    LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

	    return donHangRepository.getSoDonHangTheoNgay(fromDateTime, toDateTime);
	}

	@Override
	public List<Object[]> getTopNhanVienBanHang(LocalDateTime from, LocalDateTime to) {
	    return donHangRepository.findTopNhanVienBanHang(from, to);
	}


	@Override
	public List<Object[]> getTopKhachHang(LocalDateTime from, LocalDateTime to) {
	    return donHangRepository.findTopKhachHang(from, to);
	}
	@Override
	public List<Object[]> getTopSanPham(LocalDateTime from, LocalDateTime to) {
	    return donHangRepository.findTopSanPham(from, to);
	}

	@Override
	public List<Map<String, Object>> thongKeDoanhThuVaLoiNhuanTheoNgay(LocalDateTime fromDate, LocalDateTime toDate) {
	    // KHÔNG gọi atStartOfDay hay atTime nữa
	    List<Object[]> results = donHangRepository.getDoanhThuVaLoiNhuanTheoNgay(fromDate, toDate);

	    // Xử lý dữ liệu như cũ
	    List<Map<String, Object>> list = new ArrayList<>();
	    for (Object[] row : results) {
	        Map<String, Object> map = new HashMap<>();
	        map.put("ngay", row[0]);
	        map.put("doanhThu", row[1]);
	        map.put("loiNhuan", row[2]);
	        list.add(map);
	    }

	    return list;
	}

	@Override
	public List<Map<String, Object>> thongKeDoanhThuVaLoiNhuanTheoSanPham(LocalDateTime fromDate, LocalDateTime toDate) {
	    List<Object[]> rawData = donHangRepository.getDoanhThuVaLoiNhuanTheoSanPham(fromDate, toDate);
	    List<Map<String, Object>> result = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> item = new HashMap<>();
	        item.put("tenSanPham", row[0]);
	        item.put("doanhThu", row[1]);
	        item.put("loiNhuan", row[2]);
	        result.add(item);
	    }

	    return result;
	}

	@Override
	public List<Object[]> thongKeSanPhamBanChay(LocalDateTime fromDate, LocalDateTime toDate) {
	    return chiTietDonHangRepository.findTopSanPhamBanChay(fromDate, toDate);
	}
	@Override
	public List<Object[]> thongKeSanPhamBanChayChiTiet(LocalDateTime fromDate, LocalDateTime toDate) {
	    return donHangRepository.thongKeSanPhamBanChayChiTiet(fromDate, toDate);
	}

	@Override
	public List<Object[]> thongKeDonHangTheoTrangThai() {
	    return donHangRepository.thongKeDonHangTheoTrangThai();
	}
	@Override
	public List<Object[]> thongKeDoanhThuTheoNhanVien(LocalDateTime from, LocalDateTime to) {
	    return donHangRepository.thongKeDoanhThuTheoNhanVien(from, to);
	}

	@Override
	public List<Object[]> thongKeSanPhamBiTraNhieuNhat() {
	    return chiTietDonHangRepository.thongKeSanPhamBiTraNhieuNhat();
	}

	@Override
	public List<Object[]> thongKeKhachHangMuaNhieu(LocalDateTime from, LocalDateTime to) {
	    return donHangRepository.thongKeKhachHangMuaNhieu(from, to);
	}

	@Override
	public List<Object[]> thongKeKhachHangHuyDonNhieu(LocalDateTime from, LocalDateTime to) {
	    return donHangRepository.thongKeKhachHangHuyDonNhieu(from, to);
	}
	
	@Override
	public List<Object[]> thongKeDonMuaTheoKhach(Integer id) {
	    return donHangRepository.thongKeDonMuaTheoKhach(id);
	}


	@Override
	public Long thongKeDonHuyTheoKhach(Integer maNguoiDung) {
	    return donHangRepository.thongKeDonHuyTheoKhach(maNguoiDung);
	}

	@Override
	public List<DonHang> getDonHangsByStatuses(List<String> trangThaiList) {
		// TODO Auto-generated method stub
	    return donHangRepository.findByTrangThaiDonHangIn(trangThaiList);

	}





}
