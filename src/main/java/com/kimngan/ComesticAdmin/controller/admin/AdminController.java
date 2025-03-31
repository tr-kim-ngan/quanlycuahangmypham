package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.DonNhapHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.NhaCungCapService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.YeuCauBoSungService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private DonNhapHangService donNhapHangService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private NhaCungCapService nhaCungCapService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	@Autowired
	private KiemKeKhoService kiemKeKhoService;

	@Autowired
	private SanPhamRepository sanPhamRepository;

	@Autowired
	private YeuCauBoSungService yeuCauBoSungService;

	@Autowired
	private DonHangService donHangService;

	@Autowired
	private HoaDonService hoaDonService;

	@GetMapping({ "", "/index" })
	public String adminHome(Model model) {
		// Lấy thông tin người dùng đã đăng nhập
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		// Thêm thông tin người dùng vào model
		model.addAttribute("user", nguoiDungDetails);

		// Thêm thông tin thống kê vào model
		// Tổng số khách hàng (Người dùng có quyền khách hàng)
		long totalCustomers = nguoiDungService.countCustomers();
		model.addAttribute("totalCustomers", totalCustomers);

		long totalOrders = donHangService.countOrders();
		model.addAttribute("totalOrders", totalOrders);

		long totalActiveProducts = sanPhamService.countActiveProducts();
		model.addAttribute("totalProducts", totalActiveProducts);

		//// Thống kê đơn hàng thành công và chưa xác nhận
		long successfulOrders = donHangService.countByTrangThaiDonHang("Đã hoàn thành");
		long pendingOrders = donHangService.countByTrangThaiDonHang("Đang xử lý");
		model.addAttribute("successfulOrders", successfulOrders);
		model.addAttribute("pendingOrders", pendingOrders);

		// Tính doanh thu từ các hóa đơn đã xác nhận
		BigDecimal totalRevenue = hoaDonService.calculateTotalRevenue();
		System.out.println("🔍 Tổng doanh thu từ hóa đơn đã xác nhận: " + totalRevenue);
		model.addAttribute("totalRevenue", totalRevenue);

		// Thêm số hóa đơn chưa xác nhận vào model
		long unconfirmedInvoices = hoaDonService.countUnconfirmedInvoices();
		model.addAttribute("unconfirmedInvoices", unconfirmedInvoices);

		return "admin/index"; // Trang chính cho admin sau khi đăng nhập thành công
	}

	@GetMapping("/thongke/sanpham-gan-het")
	public String thongBaoSanPhamGanHet(Model model) {
		int nguong = 10;

		List<SanPham> sapHetHangRaw = sanPhamService.getSanPhamGanHetHang(nguong);

		// List<SanPham> sapHetHang = sanPhamService.getSanPhamGanHetHang(nguong);
		List<SanPham> allActiveSanPhams = sanPhamService.findByTrangThai(true);
		List<String> tenSanPhams = new ArrayList<>();
		List<Integer> tonKhos = new ArrayList<>();

		List<SanPham> sanPhamsCoNhap = sanPhamService.getSanPhamsCoTrongChiTietNhapVaDangHoatDong();
		List<String> tenSanPhamsAll = new ArrayList<>();
		List<Integer> tonKhosAll = new ArrayList<>();

		// Lọc ra sản phẩm chưa từng được nhập
		List<Integer> sanPhamDaNhapIds = sanPhamsCoNhap.stream().map(SanPham::getMaSanPham)
				.collect(Collectors.toList());
		List<SanPham> chuaNhap = allActiveSanPhams.stream().filter(sp -> !sanPhamDaNhapIds.contains(sp.getMaSanPham()))
				.collect(Collectors.toList());

		List<SanPham> sapHetHang = sapHetHangRaw.stream().filter(sp -> sanPhamDaNhapIds.contains(sp.getMaSanPham()))
				.collect(Collectors.toList());

		for (SanPham sp : sapHetHang) {
			Integer maSanPham = sp.getMaSanPham();

			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sp.getSoLuong();
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);
			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			tenSanPhams.add(sp.getTenSanPham());
			tonKhos.add(soLuongTonKho);
		}

		for (SanPham sp : sanPhamsCoNhap) {
			Integer maSanPham = sp.getMaSanPham();

			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sp.getSoLuong();
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);
			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			tenSanPhamsAll.add(sp.getTenSanPham());
			tonKhosAll.add(soLuongTonKho);
		}
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("sanPhamGanHet", sapHetHang);
		model.addAttribute("tenSanPhams", tenSanPhams);
		model.addAttribute("tonKhos", tonKhos);
		model.addAttribute("chuaNhap", chuaNhap);

		model.addAttribute("tenSanPhamsAll", tenSanPhamsAll);
		model.addAttribute("tonKhosAll", tonKhosAll);

		return "admin/thongke/thong-bao-san-pham-gan-het";
	}

	@GetMapping("/thongke/nhaphang")
	public String thongKeNhapHang(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {
		if (fromDate == null)
			fromDate = LocalDate.now().minusDays(30);
		if (toDate == null)
			toDate = LocalDate.now();

		List<Object[]> results = chiTietDonNhapHangService.getImportStatistics(fromDate, toDate);
		List<Object[]> topProducts = chiTietDonNhapHangService.getTopImportedProducts(fromDate, toDate);
		List<Object[]> danhSachBaoCao = chiTietDonNhapHangService.getBaoCaoChiTiet(fromDate, toDate);
		List<Object[]> topSuppliers = chiTietDonNhapHangService.getTopSuppliers(fromDate, toDate);

		List<String> labels = new ArrayList<>();
		List<Integer> values = new ArrayList<>();

		for (Object[] row : results) {
			if (row[0] != null && row[1] != null) {
				labels.add(row[0].toString());
				values.add(((Number) row[1]).intValue());
			}
		}
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		// Thêm thông tin người dùng vào model
		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("labels", labels);
		model.addAttribute("values", values);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("danhSachBaoCao", danhSachBaoCao);
		model.addAttribute("topSuppliers", topSuppliers);
		model.addAttribute("topProducts", topProducts);

		return "admin/thongke/nhaphang";
	}

	@GetMapping("/thongke/nhaphang/tong-gia-tri")
	public String getTotalImportValueForAdmin(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {

		if (fromDate == null)
			fromDate = LocalDate.now().minusDays(30);
		if (toDate == null)
			toDate = LocalDate.now();

		List<Object[]> results = chiTietDonNhapHangService.getTotalImportValue(fromDate, toDate);

		List<String> labels = new ArrayList<>();
		List<BigDecimal> values = new ArrayList<>();

		for (Object[] row : results) {
			labels.add(row[0].toString()); // Tên sản phẩm
			values.add((BigDecimal) row[1]); // Tổng giá trị nhập
		}

		List<Object[]> reportData = chiTietDonNhapHangService.getTotalImportReport(fromDate, toDate);
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("labels", labels);
		model.addAttribute("values", values);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("reportData", reportData);

		return "admin/thongke/tong-gia-tri-nhap"; // Chuyển sang file HTML bên admin
	}

	@GetMapping("/thongke/nhaphang/xu-huong")
	public String getImportTrendForAdmin(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {

		if (fromDate == null)
			fromDate = LocalDate.now().minusDays(30);
		if (toDate == null)
			toDate = LocalDate.now();

		List<Object[]> results = chiTietDonNhapHangService.getImportTrendDetail(fromDate, toDate);

		List<String> labels = new ArrayList<>();
		List<Integer> values = new ArrayList<>();
		List<Double> totalValues = new ArrayList<>();

		for (Object[] row : results) {
			labels.add(row[0].toString()); // Ngày nhập
			values.add(((Number) row[1]).intValue()); // Tổng số lượng nhập
			totalValues.add(((Number) row[2]).doubleValue()); // Tổng giá trị nhập
		}

		List<Map<String, Object>> reportData = new ArrayList<>();
		for (Object[] row : results) {
			Map<String, Object> reportRow = new HashMap<>();
			reportRow.put("ngayNhap", row[0]);
			reportRow.put("soLuongNhap", row[1]);
			reportRow.put("tongGiaTriNhap", row[2]);
			reportData.add(reportRow);
		}
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("labels", labels);
		model.addAttribute("values", values);
		model.addAttribute("totalValues", totalValues);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("reportData", reportData);

		return "admin/thongke/xu-huong-nhap";
	}

	@GetMapping("/thongke/xuat-kho")
	public String getExportStatisticsForAdmin(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {

		if (fromDate == null) {
			fromDate = LocalDate.now().minusDays(30);
		}
		if (toDate == null) {
			toDate = LocalDate.now();
		}

		LocalDateTime fromDateTime = fromDate.atStartOfDay();
		LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

		List<Object[]> stockStatistics = chiTietDonNhapHangService.getImportStatistics(fromDate, toDate);
		List<Object[]> results = chiTietDonHangService.getExportStatistics(fromDateTime, toDateTime);
		List<Object[]> topProducts = chiTietDonHangService.getTopExportedProducts(fromDateTime, toDateTime);
		List<Object[]> danhSachBaoCao = chiTietDonHangService.getBaoCaoXuatKhoChiTiet(fromDateTime, toDateTime);
		List<Object[]> topCustomers = chiTietDonHangService.getTopCustomers(fromDateTime, toDateTime);

		List<String> stockLabels = new ArrayList<>();
		List<Integer> stockValuesBefore = new ArrayList<>();
		List<Integer> stockValuesAfter = new ArrayList<>();

		for (Object[] row : stockStatistics) {
			if (row.length >= 2) {
				String tenSanPham = row[0].toString();
				int tongSoLuongNhap = ((Number) row[1]).intValue();

				List<SanPham> sanPhams = sanPhamRepository.findByTenSanPhamContainingIgnoreCase(tenSanPham);
				SanPham sanPham = sanPhams.isEmpty() ? null : sanPhams.get(0);

				if (sanPham != null) {
					int maSanPham = sanPham.getMaSanPham();
					int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
					int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);

					int soLuongTonKhoBefore = tongSoLuongNhap - soLuongTrenKe;
					int soLuongTonKhoAfter = soLuongTonKhoBefore + deltaKiemKe;

					stockLabels.add(tenSanPham);
					stockValuesBefore.add(soLuongTonKhoBefore);
					stockValuesAfter.add(soLuongTonKhoAfter);
				}
			}
		}
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("stockLabels", stockLabels);
		model.addAttribute("stockValuesBefore", stockValuesBefore);
		model.addAttribute("stockValuesAfter", stockValuesAfter);
		model.addAttribute("labels", results.stream().map(row -> row[0].toString()).toList());
		model.addAttribute("values", results.stream().map(row -> ((Number) row[1]).intValue()).toList());
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("danhSachBaoCao", danhSachBaoCao);
		model.addAttribute("topCustomers", topCustomers);
		model.addAttribute("topProducts", topProducts);

		return "admin/thongke/thong-ke-xuat"; 
	}

	@GetMapping("/thongke/donhang")
	public String thongKeDonHang(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {
		List<Object[]> donHangTheoNgay = donHangService.getSoDonHangTheoNgay(fromDate, toDate);

		// Tách ra 2 list cho biểu đồ
		List<String> ngayLabels = new ArrayList<>();
		List<Long> soDonTheoNgay = new ArrayList<>();

		for (Object[] row : donHangTheoNgay) {
			ngayLabels.add(row[0].toString()); // Ngày
			soDonTheoNgay.add((Long) row[1]); // Số đơn hàng
		}
		if (fromDate == null)
			fromDate = LocalDate.now().minusDays(30);
		if (toDate == null)
			toDate = LocalDate.now();

		long tongSoDon = donHangService.demDonHangTrongKhoangNgay(fromDate.atStartOfDay(), toDate.atTime(23, 59, 59));
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		model.addAttribute("user", nguoiDungDetails);

		// Lấy danh sách top sản phẩm
		List<Object[]> topSanPham = donHangService.getTopSanPham(fromDate.atStartOfDay(), toDate.atTime(23, 59, 59));

		// Top nhân viên bán hàng
		List<Object[]> topNhanVien = donHangService.getTopNhanVienBanHang(fromDate.atStartOfDay(),
				toDate.atTime(23, 59, 59));

		// Top khách hàng
		List<Object[]> topKhachHang = donHangService.getTopKhachHang(fromDate.atStartOfDay(),
				toDate.atTime(23, 59, 59));
		for (Object[] row : topNhanVien) {
			System.out.println("NV: " + row[0] + ", Số đơn: " + row[1]);
		}

		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("tongSoDon", tongSoDon);
		model.addAttribute("ngayLabels", ngayLabels);
		model.addAttribute("soDonTheoNgay", soDonTheoNgay);
		// Thêm dữ liệu bảng vào model
		model.addAttribute("topSanPham", topSanPham);
		model.addAttribute("topNhanVien", topNhanVien);
		model.addAttribute("topKhachHang", topKhachHang);
		return "admin/thongke/thong-ke-don-hang";
	}

	@GetMapping("/thongke/doanhthu-loinhuan")
	public String thongKeDoanhThuVaLoiNhuan(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {

		if (fromDate == null) {
			fromDate = LocalDate.now().minusDays(30);
		}
		if (toDate == null) {
			toDate = LocalDate.now();
		}

		LocalDateTime fromDateTime = fromDate.atStartOfDay();
		LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

		List<Map<String, Object>> results = donHangService.thongKeDoanhThuVaLoiNhuanTheoNgay(fromDateTime, toDateTime);

		List<String> labels = new ArrayList<>();
		List<BigDecimal> doanhThuList = new ArrayList<>();
		List<BigDecimal> loiNhuanList = new ArrayList<>();

		for (Map<String, Object> row : results) {
			labels.add(row.get("ngay").toString());
			doanhThuList.add(BigDecimal.valueOf(((Number) row.get("doanhThu")).doubleValue()));
			loiNhuanList.add(BigDecimal.valueOf(((Number) row.get("loiNhuan")).doubleValue()));
		}

		// Thống kê theo sản phẩm
		List<Map<String, Object>> theoSanPham = donHangService.thongKeDoanhThuVaLoiNhuanTheoSanPham(fromDateTime,
				toDateTime);
		// Danh sách sản phẩm + doanh thu + lợi nhuận
		List<Map<String, Object>> sanPhamStats = new ArrayList<>();
		for (Map<String, Object> row : theoSanPham) {
			Map<String, Object> map = new HashMap<>();
			map.put("tenSanPham", row.get("tenSanPham"));
			map.put("doanhThu", BigDecimal.valueOf(((Number) row.get("doanhThu")).doubleValue()));
			map.put("loiNhuan", BigDecimal.valueOf(((Number) row.get("loiNhuan")).doubleValue()));
			sanPhamStats.add(map);
		}

		List<String> productNames = new ArrayList<>();
		List<BigDecimal> revenueList = new ArrayList<>();
		List<BigDecimal> profitList = new ArrayList<>();

		for (Map<String, Object> row : sanPhamStats) {
			productNames.add(row.get("tenSanPham").toString());
			revenueList.add((BigDecimal) row.get("doanhThu"));
			profitList.add((BigDecimal) row.get("loiNhuan"));
		}

		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("labels", labels);
		model.addAttribute("doanhThuList", doanhThuList);
		model.addAttribute("loiNhuanList", loiNhuanList);
		model.addAttribute("sanPhamStats", sanPhamStats);
		model.addAttribute("productNames", productNames);
		model.addAttribute("revenueList", revenueList);
		model.addAttribute("profitList", profitList);

		return "admin/thongke/thong-ke-doanh-thu";
	}

	@GetMapping("/thongke/sanpham-banchay")
	public String thongKeSanPhamBanChay(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {

		if (fromDate == null) {
			fromDate = LocalDate.now().minusDays(30);
		}
		if (toDate == null) {
			toDate = LocalDate.now();
		}

		LocalDateTime fromDateTime = fromDate.atStartOfDay();
		LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

		List<Object[]> topSanPham = donHangService.thongKeSanPhamBanChay(fromDateTime, toDateTime);

		List<String> tenSanPhams = new ArrayList<>();
		List<Long> soLuongs = new ArrayList<>();

		for (Object[] row : topSanPham) {
			tenSanPhams.add(row[0].toString());
			soLuongs.add((Long) row[1]);
		}
		// Dữ liệu bảng chi tiết
		List<Object[]> chiTiet = donHangService.thongKeSanPhamBanChayChiTiet(fromDateTime, toDateTime);
		List<Map<String, Object>> sanPhamStats = new ArrayList<>();
		for (Object[] row : chiTiet) {
			Map<String, Object> map = new HashMap<>();
			map.put("tenSanPham", row[0]);
			map.put("soLuong", row[1]);
			map.put("doanhThu", BigDecimal.valueOf(((Number) row[2]).doubleValue()));
			map.put("donGiaTB", BigDecimal.valueOf(((Number) row[3]).doubleValue()));
			map.put("loiNhuan", BigDecimal.valueOf(((Number) row[4]).doubleValue()));
			sanPhamStats.add(map);
		}

		// Thống kê sản phẩm bị trả nhiều nhất
		List<Object[]> traNhieu = donHangService.thongKeSanPhamBiTraNhieuNhat();
		List<String> tenSanPhamsTra = new ArrayList<>();
		List<Long> soLuongTras = new ArrayList<>();
		List<Map<String, Object>> sanPhamHoanTra = new ArrayList<>();

		for (Object[] row : traNhieu) {
			String ten = (String) row[0];
			Long soLuong = (Long) row[1];

			tenSanPhamsTra.add(ten);
			soLuongTras.add(soLuong);

			Map<String, Object> map = new HashMap<>();
			map.put("tenSanPham", ten);
			map.put("soLuongTra", soLuong);
			sanPhamHoanTra.add(map);
		}

		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("tenSanPhams", tenSanPhams);
		model.addAttribute("soLuongs", soLuongs);
		model.addAttribute("sanPhamStats", sanPhamStats);

		model.addAttribute("tenSanPhamsTra", tenSanPhamsTra);
		model.addAttribute("soLuongTras", soLuongTras);
		model.addAttribute("sanPhamHoanTra", sanPhamHoanTra);

		return "admin/thongke/thong-ke-san-pham-ban-chay";
	}

	@GetMapping("/thongke/trangthai-donhang")
	public String thongKeTrangThaiDonHang(Model model) {
		List<Object[]> thongKe = donHangService.thongKeDonHangTheoTrangThai();

		List<String> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();

		for (Object[] row : thongKe) {
			labels.add(row[0].toString());
			values.add((Long) row[1]);
		}
		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("labels", labels);
		model.addAttribute("values", values);
		return "admin/thongke/thong-ke-trang-thai-don-hang";
	}

	@GetMapping("/thongke/doanhthu")
	public String thongKeDoanhThuNhanVien(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {

		if (fromDate == null)
			fromDate = LocalDate.now().minusDays(30);
		if (toDate == null)
			toDate = LocalDate.now();

		LocalDateTime fromDateTime = fromDate.atStartOfDay();
		LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

		List<Object[]> results = donHangService.thongKeDoanhThuTheoNhanVien(fromDateTime, toDateTime);

		List<String> tenNhanViens = new ArrayList<>();
		List<BigDecimal> doanhThus = new ArrayList<>();
		List<Map<String, Object>> bangChiTiet = new ArrayList<>();

		BigDecimal tongDoanhThu = BigDecimal.ZERO;
		for (Object[] row : results) {
			BigDecimal doanhThu = (BigDecimal) row[1];
			if (doanhThu != null)
				tongDoanhThu = tongDoanhThu.add(doanhThu);
		}

		for (Object[] row : results) {
			Object tenNhanVien = row[0];
			BigDecimal doanhThu = (BigDecimal) row[1];
			Long soLuongDon = (Long) row[2];
			Long soLuongSanPham = (Long) row[3];

			String ten = tenNhanVien != null ? tenNhanVien.toString() : "Không xác định";
			BigDecimal dt = doanhThu != null ? doanhThu : BigDecimal.ZERO;

			tenNhanViens.add(ten);
			doanhThus.add(dt);

			Map<String, Object> map = new HashMap<>();
			map.put("hoTen", ten);
			map.put("doanhThu", dt);
			map.put("soLuongDon", soLuongDon);
			map.put("soLuongSanPham", soLuongSanPham);
			map.put("phanTramDongGop",
					tongDoanhThu.compareTo(BigDecimal.ZERO) > 0
							? dt.multiply(BigDecimal.valueOf(100)).divide(tongDoanhThu, 2, RoundingMode.HALF_UP)
							: BigDecimal.ZERO);
			bangChiTiet.add(map);
		}

		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("tenNhanViens", tenNhanViens);
		model.addAttribute("doanhThus", doanhThus);
		model.addAttribute("bangChiTiet", bangChiTiet);
		model.addAttribute("tongDoanhThu", tongDoanhThu);

		return "admin/thongke/thong-ke-doanh-thu-nhan-vien";
	}

	@GetMapping("/thongke/khachhang")
	public String thongKeKhachHang(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

			@RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			Model model) {
		if (fromDate == null)
			fromDate = LocalDate.now().minusDays(30);
		if (toDate == null)
			toDate = LocalDate.now();

		LocalDateTime fromDateTime = fromDate.atStartOfDay();
		LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

		List<Object[]> muaNhieu = donHangService.thongKeKhachHangMuaNhieu(fromDateTime, toDateTime);
		List<Object[]> huyNhieu = donHangService.thongKeKhachHangHuyDonNhieu(fromDateTime, toDateTime);

		List<String> tenKhachMua = new ArrayList<>();
		List<String> tenNguoiDungMua = new ArrayList<>();
		List<BigDecimal> tongTienMua = new ArrayList<>();
		List<Long> soLuongMua = new ArrayList<>();

		List<String> tenKhachHuy = new ArrayList<>();
		List<String> tenNguoiDungHuy = new ArrayList<>();
		List<Long> soLuongHuy = new ArrayList<>();

		for (Object[] row : muaNhieu) {
			tenKhachMua.add((String) row[0]);
			tenNguoiDungMua.add((String) row[1]);
			soLuongMua.add((Long) row[2]);
			tongTienMua.add((BigDecimal) row[3]);
		}

		for (Object[] row : huyNhieu) {
			tenKhachHuy.add((String) row[0]);
			tenNguoiDungHuy.add((String) row[1]);
			soLuongHuy.add((Long) row[2]);
		}

		// Ghép họ tên và tên đăng nhập cho biểu đồ
		List<String> tenDayDuMua = new ArrayList<>();
		for (int i = 0; i < tenKhachMua.size(); i++) {
			String hoTen = tenKhachMua.get(i);
			String tenDangNhap = tenNguoiDungMua.get(i);
			if (hoTen == null || hoTen.trim().isEmpty()) {
				tenDayDuMua.add(tenDangNhap); // Chỉ hiện tên đăng nhập nếu không có họ tên
			} else {
				tenDayDuMua.add(hoTen + " (" + tenDangNhap + ")");
			}
		}

		List<String> tenDayDuHuy = new ArrayList<>();
		for (int i = 0; i < tenKhachHuy.size(); i++) {
			String hoTen = tenKhachHuy.get(i);
			String tenDangNhap = tenNguoiDungHuy.get(i);
			if (hoTen == null || hoTen.trim().isEmpty()) {
				tenDayDuHuy.add(tenDangNhap);
			} else {
				tenDayDuHuy.add(hoTen + " (" + tenDangNhap + ")");
			}
		}

		// Tạo danh sách chi tiết bảng khách hàng mua nhiều nhất
		List<Map<String, Object>> bangChiTietMua = new ArrayList<>();
		for (int i = 0; i < tenKhachMua.size(); i++) {
			Map<String, Object> map = new HashMap<>();
			String hoTen = tenKhachMua.get(i);

			map.put("hoTen", (hoTen == null || hoTen.trim().isEmpty()) ? "Khách vãng lai" : hoTen);
			map.put("tenNguoiDung", tenNguoiDungMua.get(i));
			map.put("soLuongDon", soLuongMua.get(i));
			map.put("tongTien", tongTienMua.get(i));
			bangChiTietMua.add(map);
		}
		// Tạo danh sách chi tiết bảng khách hàng hủy nhiều nhất
		List<Map<String, Object>> bangChiTietHuy = new ArrayList<>();
		for (int i = 0; i < tenKhachHuy.size(); i++) {
			Map<String, Object> map = new HashMap<>();
			 String hoTen = tenKhachHuy.get(i);

			map.put("hoTen", (hoTen == null || hoTen.trim().isEmpty()) ? "không xác định" : hoTen);
			map.put("tenNguoiDung", tenNguoiDungHuy.get(i));
			map.put("soLuongDon", soLuongHuy.get(i));
			bangChiTietHuy.add(map);
		}

		NguoiDungDetails nguoiDungDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		model.addAttribute("user", nguoiDungDetails);
		model.addAttribute("tenDayDuMua", tenDayDuMua);
		model.addAttribute("tongTienMua", tongTienMua);
		model.addAttribute("soLuongMua", soLuongMua);
		model.addAttribute("tenNguoiDungMua", tenNguoiDungMua);
		model.addAttribute("tenDayDuHuy", tenDayDuHuy);
		model.addAttribute("soLuongHuy", soLuongHuy);
		model.addAttribute("tenNguoiDungHuy", tenNguoiDungHuy);
		model.addAttribute("bangChiTietMua", bangChiTietMua);
		model.addAttribute("bangChiTietHuy", bangChiTietHuy);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);

		return "admin/thongke/thong-ke-khach-hang";
	}

	@GetMapping("/revenue-data")
	public ResponseEntity<Map<String, List<Map<String, Object>>>> getRevenueData() {
		Map<String, List<Map<String, Object>>> revenueData = new HashMap<>();

		// Lấy doanh thu theo ngày, tuần, tháng
		revenueData.put("daily", hoaDonService.getRevenueByDate());
		revenueData.put("weekly", hoaDonService.getRevenueByWeek());
		revenueData.put("monthly", hoaDonService.getRevenueByMonth());

		return ResponseEntity.ok(revenueData);
	}
}
