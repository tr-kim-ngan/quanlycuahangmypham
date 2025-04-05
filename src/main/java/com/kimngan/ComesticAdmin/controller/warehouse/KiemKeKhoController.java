package com.kimngan.ComesticAdmin.controller.warehouse;

import com.kimngan.ComesticAdmin.entity.KiemKeKho;
import com.kimngan.ComesticAdmin.entity.LichSuCaLamViec;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.LichSuCaLamViecService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/warehouse/import/kiemke")
public class KiemKeKhoController {

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private LichSuCaLamViecService lichSuCaLamViecService;

	@Autowired
	private KiemKeKhoService kiemKeKhoService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private NguoiDungService nguoiDungService;
	
	@Autowired
	private DonHangService donHangService;

	// Lấy thông tin người dùng giống bên ShipperAuthController
	@ModelAttribute("currentWarehouseUser")
	public NguoiDung getCurrentUser(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	// Hiển thị form kiểm kê
	@GetMapping("/{id}")
	public String showKiemKeForm(@PathVariable("id") Integer id, Model model) {
		// Lấy danh sách sản phẩm
		List<SanPham> danhSachSanPham = sanPhamService.getProductsInOrderDetails(Pageable.unpaged()).getContent();
		Map<Integer, Integer> sanPhamSoLuongTonKhoMap = new HashMap<>();
		Map<Integer, Integer> soLuongThucTeMap = new HashMap<>();
		boolean coSaiSo = false;

		for (SanPham sp : danhSachSanPham) {
			Integer maSanPham = sp.getMaSanPham();

			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sp.getMaSanPham());
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sp.getMaSanPham());
			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(sp.getMaSanPham());
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null) ? 
					(tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe +soLuongTraHang);

			sanPhamSoLuongTonKhoMap.put(sp.getMaSanPham(), soLuongTonKho);
			soLuongThucTeMap.put(sp.getMaSanPham(), soLuongTonKho);

			// Nếu số lượng thực tế khác số lượng tồn kho => Có sai số
			if (soLuongTonKho != sp.getSoLuong()) {
				coSaiSo = true;
			}
		}

		model.addAttribute("sanPhamSoLuongTonKhoMap", sanPhamSoLuongTonKhoMap);
		model.addAttribute("soLuongThucTeMap", soLuongThucTeMap);
		model.addAttribute("danhSachSanPham", danhSachSanPham);
		model.addAttribute("caLamViecId", id);
		model.addAttribute("coSaiSo", coSaiSo);

		return "warehouse/shifts/kiemke"; // Trả về giao diện kiểm kê
	}

	// Xem danh sách sản phẩm đã kiểm kê trong một ca làm việc
	@GetMapping("/view/{caLamViecId}")
	public String viewKiemKeHistory(@PathVariable("caLamViecId") Integer caLamViecId, Model model) {
		// Lấy danh sách kiểm kê của ca làm việc
		List<KiemKeKho> danhSachKiemKe = kiemKeKhoService.findByLichSuCaLamViecId(caLamViecId);

		// Kiểm tra có sản phẩm nào chờ xét duyệt không
		boolean coSanPhamChoDuyet = danhSachKiemKe.stream().anyMatch(kk -> !kk.getDaXetDuyet());

		model.addAttribute("danhSachKiemKe", danhSachKiemKe);
		model.addAttribute("coSanPhamChoDuyet", coSanPhamChoDuyet);
		model.addAttribute("caLamViecId", caLamViecId);

		return "warehouse/shifts/kiemke-history"; // Trả về trang hiển thị lịch sử kiểm kê
	}

	@PostMapping("/save")
	public String saveKiemKe(@RequestParam("maSanPham") List<Integer> maSanPhamList,
			@RequestParam("soLuongThucTe") List<Integer> soLuongThucTeList,
			@RequestParam(value = "lyDoDieuChinh", required = false) List<String> lyDoDieuChinhList,
			@RequestParam(value = "lyDoNhapTay", required = false) List<String> lyDoNhapTayList,
			@ModelAttribute("currentWarehouseUser") NguoiDung nguoiDung, 
			@RequestParam("caLamViecId") Integer caLamViecId,
			RedirectAttributes redirectAttributes, Model model) {

		if (nguoiDung == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể lấy thông tin người kiểm kê.");
			return "redirect:/warehouse/import/kiemke/" + caLamViecId;
		}

		boolean coSaiSo = false;
		List<KiemKeKho> danhSachSanPhamThayDoi = new ArrayList<>();

		LichSuCaLamViec caLamViec = lichSuCaLamViecService.findById(caLamViecId)
				.orElseThrow(() -> new IllegalArgumentException("Ca làm việc không tồn tại."));

		for (int i = 0; i < maSanPhamList.size(); i++) {
			Integer maSanPham = maSanPhamList.get(i);
			Integer soLuongMoi = soLuongThucTeList.get(i);
			String lyDoDieuChinh = (lyDoDieuChinhList != null && i < lyDoDieuChinhList.size())
					? lyDoDieuChinhList.get(i)
					: "";
			String lyDoNhapTay = (lyDoNhapTayList != null && i < lyDoNhapTayList.size()) ? lyDoNhapTayList.get(i) : "";

			SanPham sanPham = sanPhamService.findById(maSanPham);
			if (sanPham != null) {
				int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
				int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
				int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
				int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
				int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

//				Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);
//
//				int soLuongTonKho = (tonKhoDaDuyet != null) ? (tonKhoDaDuyet - soLuongBan - soLuongTrenKe) 
//						: (tongSoLuongNhap - soLuongBan - soLuongTrenKe); 
				Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

				int soLuongTonKho = (tonKhoDaDuyet != null)
						? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)

						: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

				// int soLuongTonKho = tongSoLuongNhap - soLuongBan - soLuongTrenKe;
				int soLuongTonKhoLucKiemKe = soLuongTonKho;
				if (!soLuongMoi.equals(soLuongTonKho)) {
					coSaiSo = true;
					KiemKeKho kiemKeKho = new KiemKeKho();
					kiemKeKho.setSanPham(sanPham);
					kiemKeKho.setSoLuongHeThongLucKiemKe(soLuongTonKho);
					kiemKeKho.setSoLuongTruocKiemKe(soLuongTonKhoLucKiemKe);
					kiemKeKho.setSoLuongTruocKiemKe(soLuongTonKho);
					kiemKeKho.setSoLuongSauKiemKe(soLuongMoi);
					kiemKeKho.setLichSuCaLamViec(caLamViec);
					kiemKeKho.setNguoiKiemKe(nguoiDung);
					kiemKeKho.setLyDoDieuChinh("Khác".equals(lyDoDieuChinh) ? lyDoNhapTay : lyDoDieuChinh);

					kiemKeKho.setCoThayDoi(true);
					danhSachSanPhamThayDoi.add(kiemKeKho);
				}
			}
		}

		if (coSaiSo) {
			boolean thieuLyDo = false;
			for (int i = 0; i < danhSachSanPhamThayDoi.size(); i++) {
				String lyDo = (lyDoDieuChinhList != null && i < lyDoDieuChinhList.size()) ? lyDoDieuChinhList.get(i)
						: "";
				if (lyDo.isEmpty()) {
					thieuLyDo = true;
					break;
				}
			}
			if (thieuLyDo) {
				model.addAttribute("coSaiSo", true);
				model.addAttribute("danhSachSanPham", maSanPhamList);
				model.addAttribute("caLamViecId", caLamViecId);
				model.addAttribute("errorMessage", "Vui lòng nhập lý do trước khi xác nhận!");
				return "warehouse/shifts/kiemke"; // Trả về trang kiểm kê để nhập lý do
			}
		}

		// Gán đúng lý do cho từng sản phẩm
		if (coSaiSo) {
			for (int i = 0; i < danhSachSanPhamThayDoi.size(); i++) {
				KiemKeKho kiemKe = danhSachSanPhamThayDoi.get(i);
				String lyDo = (lyDoDieuChinhList != null && i < lyDoDieuChinhList.size()) ? lyDoDieuChinhList.get(i)
						: "";
				String lyDoNhap = (lyDoNhapTayList != null && i < lyDoNhapTayList.size()) ? lyDoNhapTayList.get(i) : "";

				kiemKe.setLyDoDieuChinh("Khác".equals(lyDo) ? lyDoNhap : lyDo);
				kiemKeKhoService.save(kiemKe);
			}
			redirectAttributes.addFlashAttribute("successMessage", "Đã lưu kiểm kê thành công.");
		} else {
			redirectAttributes.addFlashAttribute("successMessage", "Kiểm kê không có sai số.");
		}

		if (caLamViec.getThoiGianKetThuc() == null) {
			caLamViec.setThoiGianKetThuc(LocalDateTime.now());
			caLamViec.setGhiChu(coSaiSo ? "Kiểm kê hoàn tất, có sai số." : "Kiểm kê hoàn tất, không có sai số.");
			lichSuCaLamViecService.saveShift(caLamViec);
		}

		return "redirect:/warehouse/import/kiemke/confirm/" + caLamViecId;
	}

	@PostMapping("/confirm")
	public String confirmKiemKe(@RequestParam("caLamViecId") Integer caLamViecId,
			@RequestParam("lyDoDieuChinh") String lyDoDieuChinh,
			@RequestParam(value = "lyDoNhapTay", required = false) String lyDoNhapTay,
			RedirectAttributes redirectAttributes) {
		List<KiemKeKho> danhSachSanPhamThayDoi = kiemKeKhoService.findByLichSuCaLamViecId(caLamViecId);

		for (KiemKeKho kiemKe : danhSachSanPhamThayDoi) {
			kiemKe.setLyDoDieuChinh("Khác".equals(lyDoDieuChinh) ? lyDoNhapTay : lyDoDieuChinh);
			kiemKe.setDaXetDuyet(false);
			kiemKeKhoService.save(kiemKe);
		}

		redirectAttributes.addFlashAttribute("successMessage", "Kiểm kê đã được xác nhận.");
		return "redirect:/warehouse/import/kiemke/confirm/" + caLamViecId;
	}

//	@PostMapping("/check")
//	public String checkKiemKe(@RequestParam("maSanPham") List<Integer> maSanPhamList,
//			@RequestParam("soLuongThucTe") List<Integer> soLuongThucTeList,
//			@RequestParam("caLamViecId") Integer caLamViecId, Model model) {
//		// 1. Tìm danh sách sản phẩm
//		List<SanPham> danhSachSanPham = sanPhamService.getProductsInOrderDetails(Pageable.unpaged()).getContent();
//
//		// 2. Tính soLuongTonKho cho mỗi sản phẩm
//		Map<Integer, Integer> sanPhamSoLuongTonKhoMap = new HashMap<>();
//
//		boolean coSaiSo = false;
//		// Map lưu lại số lượng thực tế mà người dùng vừa nhập để hiển thị lại
//		Map<Integer, Integer> soLuongThucTeMap = new HashMap<>();
//
//		// Tạo map sẵn cho quick lookup
//		for (SanPham sp : danhSachSanPham) {
//			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sp.getMaSanPham());
//			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sp.getMaSanPham());
//			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(sp.getMaSanPham());
//
//			int soLuongTonKho = tongSoLuongNhap - soLuongBan - soLuongTrenKe;
//			sanPhamSoLuongTonKhoMap.put(sp.getMaSanPham(), soLuongTonKho);
//		}
//
//		// 3. Duyệt danh sách người dùng nhập
//		for (int i = 0; i < maSanPhamList.size(); i++) {
//			Integer maSp = maSanPhamList.get(i);
//			Integer soLuongMoi = soLuongThucTeList.get(i);
//			soLuongThucTeMap.put(maSp, soLuongMoi);
//
//			// Kiểm tra chênh lệch
//			if (soLuongMoi != null && !soLuongMoi.equals(sanPhamSoLuongTonKhoMap.get(maSp))) {
//				coSaiSo = true;
//			}
//		}
//
//		// 4. Gửi dữ liệu về lại kiemke.html
//		model.addAttribute("danhSachSanPham", danhSachSanPham);
//		model.addAttribute("sanPhamSoLuongTonKhoMap", sanPhamSoLuongTonKhoMap);
//		model.addAttribute("caLamViecId", caLamViecId);
//		model.addAttribute("coSaiSo", coSaiSo);
//		model.addAttribute("soLuongThucTeMap", soLuongThucTeMap);
//
//		return "warehouse/shifts/kiemke"; // Trả về lại trang kiemke.html
//	}

	@GetMapping("/confirm/{caLamViecId}")
	public String viewKiemKeConfirm(@PathVariable("caLamViecId") Integer caLamViecId, Model model) {
		List<KiemKeKho> danhSachSanPhamThayDoi = kiemKeKhoService.findByLichSuCaLamViecId(caLamViecId);

		model.addAttribute("danhSachSanPhamThayDoi", danhSachSanPhamThayDoi);
		model.addAttribute("caLamViecId", caLamViecId);

		return "warehouse/shifts/kiemke-confirm"; // Hiển thị trang xác nhận
	}

}
