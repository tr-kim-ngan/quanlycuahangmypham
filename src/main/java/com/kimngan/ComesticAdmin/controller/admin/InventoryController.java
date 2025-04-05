package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.KiemKeKho;
import com.kimngan.ComesticAdmin.entity.LichSuCaLamViec;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.LichSuCaLamViecService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private ChiTietDonHangService chiTietDonHangService;
	@Autowired
	private KiemKeKhoService kiemKeKhoService;

	@Autowired
	private LichSuCaLamViecService lichSuCaLamViecService;
	@Autowired
	private DonHangService donHangService;

	@GetMapping
	public String hienThiTonKho(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "trangThai", required = false, defaultValue = "true") Boolean trangThai) {

		Page<SanPham> pageSanPham;

		if (keyword != null && !keyword.isEmpty()) {
			pageSanPham = sanPhamService.searchAllActiveProductsWithOrderDetails(keyword,
					PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "maSanPham")));
		} else {
			pageSanPham = sanPhamService.findByTrangThai(true,
					PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "maSanPham")));
		}

		Map<Integer, Integer> tongSoLuongNhapMap = new HashMap<>();
		Map<Integer, Integer> soLuongTonKhoMap = new HashMap<>();

		for (SanPham sanPham : pageSanPham.getContent()) {
//			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPham.getMaSanPham());
//			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPham.getMaSanPham());
//
//			int soLuongNhapThucTe = tongSoLuongNhap - soLuongBan;
//			int soLuongTonKho = soLuongNhapThucTe - sanPham.getSoLuong();
//
//			tongSoLuongNhapMap.put(sanPham.getMaSanPham(), soLuongNhapThucTe);
//			soLuongTonKhoMap.put(sanPham.getMaSanPham(), soLuongTonKho);
			Integer maSanPham = sanPham.getMaSanPham();

			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham); 
																			
			int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham); 

			int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

			Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

			int soLuongTonKho = (tonKhoDaDuyet != null)
					? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe+ soLuongTraHang)

					: (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

			int soLuongNhapThucTe = soLuongTrenKe;

			tongSoLuongNhapMap.put(maSanPham, soLuongNhapThucTe);
			soLuongTonKhoMap.put(maSanPham, soLuongTonKho);

		}

		model.addAttribute("danhSachSanPham", pageSanPham.getContent());
		model.addAttribute("tongSoLuongNhapMap", tongSoLuongNhapMap);
		model.addAttribute("soLuongTonKhoMap", soLuongTonKhoMap);
		model.addAttribute("currentPage", pageSanPham.getNumber());
		model.addAttribute("totalPages", pageSanPham.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("keyword", keyword);
		model.addAttribute("trangThai", trangThai);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/inventory/inventory"; // View file inventory.html trong admin
	}

	@PostMapping("/update-stock")
	public String capNhatSoLuong(@RequestParam("maSanPham") Integer maSanPham,
			@RequestParam("soLuongMoi") Integer soLuongMoi, RedirectAttributes redirectAttributes) {

		SanPham sanPham = sanPhamService.findById(maSanPham);
		if (sanPham == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
			return "redirect:/admin/inventory";
		}

		int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
		int soLuongTonKho = tongSoLuongNhap - sanPham.getSoLuong();

		if (soLuongMoi > tongSoLuongNhap) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Số lượng trên kệ không thể lớn hơn tổng số lượng nhập (" + tongSoLuongNhap + ").");
			return "redirect:/admin/inventory";
		}

		if (soLuongMoi < 0 || soLuongMoi > soLuongTonKho + sanPham.getSoLuong()) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Số lượng trên kệ phải nằm trong khoảng từ 0 đến " + (soLuongTonKho + sanPham.getSoLuong()) + ".");
			return "redirect:/admin/inventory";
		}

		sanPham.setSoLuong(soLuongMoi);
		sanPhamService.update(sanPham);

		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số lượng trên kệ thành công.");
		return "redirect:/admin/inventory";
	}

	@GetMapping("/kiem-ke-chenh-lech")
	public String hienThiKiemKeChenhLech(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		// ✅ Lấy tất cả các ca làm có kiểm kê (cả đã duyệt và chưa duyệt)
		Page<LichSuCaLamViec> danhSachCaLam = lichSuCaLamViecService
				.getAllShiftsWithInventory(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "thoiGianBatDau")));

		// ✅ Tạo map lưu dữ liệu kiểm kê theo từng ca
		Map<Integer, List<KiemKeKho>> kiemKeTheoCa = new HashMap<>();
		Map<Integer, String> nhanVienThucHien = new HashMap<>();
		Map<Integer, LocalDateTime> thoiGianBatDauMap = new HashMap<>();
		Map<Integer, LocalDateTime> thoiGianKetThucMap = new HashMap<>();
		Map<Integer, Boolean> daXetDuyetMap = new HashMap<>();
		Map<Integer, Integer> soLuongKiemKeMap = new HashMap<>();

		for (LichSuCaLamViec caLam : danhSachCaLam.getContent()) {
			List<KiemKeKho> danhSachKiemKe = kiemKeKhoService.findByLichSuCaLamViecId(caLam.getMaLichSu());
			int soLuongKiemKe = kiemKeKhoService.countAllByShift(caLam.getMaLichSu());
			soLuongKiemKeMap.put(caLam.getMaLichSu(), soLuongKiemKe);

			if (!danhSachKiemKe.isEmpty()) {
				kiemKeTheoCa.put(caLam.getMaLichSu(), danhSachKiemKe);
				nhanVienThucHien.put(caLam.getMaLichSu(), caLam.getNhanVien().getTenNguoiDung());
				thoiGianBatDauMap.put(caLam.getMaLichSu(), caLam.getThoiGianBatDau());
				thoiGianKetThucMap.put(caLam.getMaLichSu(), caLam.getThoiGianKetThuc());

				// ✅ Lấy trạng thái xét duyệt của ca
				daXetDuyetMap.put(caLam.getMaLichSu(), danhSachKiemKe.get(0).getDaXetDuyet());
			}
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
		model.addAttribute("danhSachCaLam", danhSachCaLam.getContent());
		model.addAttribute("currentPage", danhSachCaLam.getNumber());
		model.addAttribute("totalPages", danhSachCaLam.getTotalPages());
		model.addAttribute("kiemKeTheoCa", kiemKeTheoCa);
		model.addAttribute("nhanVienThucHien", nhanVienThucHien);
		model.addAttribute("thoiGianBatDauMap", thoiGianBatDauMap);
		model.addAttribute("thoiGianKetThucMap", thoiGianKetThucMap);
		model.addAttribute("daXetDuyetMap", daXetDuyetMap);

		return "admin/inventory/kiem-ke-chenh-lech";
	}

	@GetMapping("/kiem-ke-chi-tiet")
	public String hienThiChiTietKiemKe(@RequestParam("maCa") Integer maCa, Model model) {

		Optional<LichSuCaLamViec> optionalCaLamViec = lichSuCaLamViecService.findById(maCa);
		if (!optionalCaLamViec.isPresent()) {
			return "redirect:/admin/inventory/kiem-ke-chenh-lech";
		}
		LichSuCaLamViec caLamViec = optionalCaLamViec.get();
		// Lấy danh sách kiểm kê theo ca làm việc
		List<KiemKeKho> danhSachKiemKe = kiemKeKhoService.findByLichSuCaLamViecId(maCa);

		// Map để lưu thông tin tồn kho
		Map<Integer, Integer> tonKhoHeThongMap = new HashMap<>();
		Map<Integer, Integer> tonKhoSauKiemKeMap = new HashMap<>();
		Map<Integer, Integer> tonKhoSauDuyetMap = new HashMap<>();

		boolean daXetDuyet = false;
		for (KiemKeKho kiemKe : danhSachKiemKe) {
			Integer maSanPham = kiemKe.getSanPham().getMaSanPham();

			// Tồn kho trên hệ thống (trước kiểm kê)
			int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
			int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
			int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
			int tonKhoHeThong = tongSoLuongNhap - soLuongBan - soLuongTrenKe;

			// Tồn kho sau khi kiểm (theo báo cáo của nhân viên)
			int tonKhoSauKiemKe = kiemKe.getSoLuongSauKiemKe();

			// Tồn kho sau khi duyệt (nếu đã duyệt thì lấy số lượng sau kiểm, nếu chưa thì
			// giữ nguyên tồn kho hệ thống)
			int tonKhoSauDuyet = kiemKe.getDaXetDuyet() ? tonKhoSauKiemKe : tonKhoHeThong;

			tonKhoHeThongMap.put(maSanPham, tonKhoHeThong);
			tonKhoSauKiemKeMap.put(maSanPham, tonKhoSauKiemKe);
			tonKhoSauDuyetMap.put(maSanPham, tonKhoSauDuyet);

			// Kiểm tra xem tất cả kiểm kê trong ca này đã được duyệt chưa
			if (kiemKe.getDaXetDuyet()) {
				daXetDuyet = true;
			}
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
		model.addAttribute("danhSachKiemKe", danhSachKiemKe);
		model.addAttribute("tonKhoHeThongMap", tonKhoHeThongMap);
		model.addAttribute("tonKhoSauKiemKeMap", tonKhoSauKiemKeMap);
		model.addAttribute("tonKhoSauDuyetMap", tonKhoSauDuyetMap);
		model.addAttribute("maCa", maCa);
		model.addAttribute("daXetDuyet", daXetDuyet);
		// Thêm lý do kiểm kê
//		model.addAttribute("HoTennhanVienThucHien", caLamViec.getNhanVien().getHoTen());
//
//		model.addAttribute("nhanVienThucHien", caLamViec.getNhanVien().getTenNguoiDung());
		NguoiDung nhanVien = caLamViec.getNhanVien();
		model.addAttribute("nhanVienThucHien", nhanVien); // truyền cả object
		model.addAttribute("thoiGianBatDau", caLamViec.getThoiGianBatDau());
		model.addAttribute("thoiGianKetThuc", caLamViec.getThoiGianKetThuc());

		model.addAttribute("thoiGianBatDau", caLamViec.getThoiGianBatDau());
		model.addAttribute("thoiGianKetThuc", caLamViec.getThoiGianKetThuc());
		return "admin/inventory/kiem-ke-chi-tiet";
	}

	@PostMapping("/xac-nhan-kiem-ke")
	public String xacNhanKiemKe(@RequestParam("maLichSu") Integer maLichSu, RedirectAttributes redirectAttributes) {

		// Kiểm tra ca làm có tồn tại không
		List<KiemKeKho> danhSachKiemKe = kiemKeKhoService.findByLichSuCaLamViecId(maLichSu);
		if (danhSachKiemKe.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không có dữ liệu kiểm kê cho ca này.");
			return "redirect:/admin/inventory/kiem-ke-chenh-lech";
		}

		// ✅ Cập nhật trạng thái xét duyệt
		for (KiemKeKho kiemKe : danhSachKiemKe) {
			kiemKe.setDaXetDuyet(true);
			kiemKeKhoService.save(kiemKe);
		}

		redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận kiểm kê cho ca #" + maLichSu);
		return "redirect:/admin/inventory/kiem-ke-chenh-lech";
	}

}
