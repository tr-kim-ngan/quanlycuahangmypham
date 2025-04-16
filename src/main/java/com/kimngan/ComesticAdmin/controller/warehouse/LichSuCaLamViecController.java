package com.kimngan.ComesticAdmin.controller.warehouse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.kimngan.ComesticAdmin.entity.LichSuCaLamViec;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.LichSuCaLamViecService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/warehouse/import")
public class LichSuCaLamViecController {

	@Autowired
	private LichSuCaLamViecService lichSuCaLamViecService;

	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private SanPhamService sanPhamService;
	@Autowired
	private KiemKeKhoService kiemKeKhoService;
	
	
	@ModelAttribute("currentWarehouseUser")
	public NguoiDung getCurrentUser(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	// Hiển thị danh sách ca làm việc (Admin hoặc nhân viên kho xem)
	@GetMapping("/shifts")
	public String listShifts(Model model, Principal principal, 
	                         @RequestParam(defaultValue = "0") int page,
	                         @RequestParam(defaultValue = "5") int size) {
	    if (principal == null) {
	        return "redirect:/login";
	    }

	    String username = principal.getName();
	    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "thoiGianBatDau"));
	    Page<LichSuCaLamViec> danhSachCaPage = lichSuCaLamViecService.getAllShifts(pageable);
	    LichSuCaLamViec caDangHoatDong = lichSuCaLamViecService.getCurrentShift(username);
	    
	    Map<Integer, Boolean> caDaKiemKeMap = new HashMap<>();
	    for (LichSuCaLamViec ca : danhSachCaPage.getContent()) {
	        boolean daKiemKe = !kiemKeKhoService.findByLichSuCaLamViecId(ca.getMaLichSu()).isEmpty();
	        caDaKiemKeMap.put(ca.getMaLichSu(), daKiemKe);
	    }

	    // ✅ Thêm dòng này để navbar nhận đúng user
	    NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
	    model.addAttribute("currentWarehouseUser", currentUser);

	    model.addAttribute("danhSachCa", danhSachCaPage.getContent());
	    model.addAttribute("caDangHoatDong", caDangHoatDong);
	    model.addAttribute("caDaKiemKeMap", caDaKiemKeMap);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", danhSachCaPage.getTotalPages());

	    return "warehouse/shifts/list";
	}



	// Nhân viên kho bắt đầu ca làm việc
	@PostMapping("/shifts/start")
	public String startShift(Principal principal, RedirectAttributes redirectAttributes) {
		if (principal == null) {
			return "redirect:/login";
		}

		String username = principal.getName();
		NguoiDung nhanVien = nguoiDungService.findByTenNguoiDung(username);

		LichSuCaLamViec caMoi = new LichSuCaLamViec();
		caMoi.setNhanVien(nhanVien);
		caMoi.setThoiGianBatDau(LocalDateTime.now());

		lichSuCaLamViecService.saveShift(caMoi);

		redirectAttributes.addFlashAttribute("successMessage", "Ca làm việc đã bắt đầu.");
		return "redirect:/warehouse/import/shifts";
	}

	// Nhân viên kho kết thúc ca làm việc
	@PostMapping("/shifts/end/{id}")
	public String endShift(@PathVariable("id") Integer id) {
		Optional<LichSuCaLamViec> optionalCaLamViec = lichSuCaLamViecService.findById(id);

		if (optionalCaLamViec.isPresent()) {
			LichSuCaLamViec caLamViec = optionalCaLamViec.get();
			if (caLamViec.getThoiGianKetThuc() == null) {
				caLamViec.setThoiGianKetThuc(LocalDateTime.now());
				lichSuCaLamViecService.saveShift(caLamViec);
			}
		}

		return "redirect:/warehouse/import/shifts";
	}
	// Nhân viên kho kết thúc ca làm việc
//	@PostMapping("/shifts/end/{id}")
//	public String endShift(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
//	    Optional<LichSuCaLamViec> optionalCaLamViec = lichSuCaLamViecService.findById(id);
//
//	    if (optionalCaLamViec.isPresent()) {
//	        LichSuCaLamViec caLamViec = optionalCaLamViec.get();
//
//	        // Lấy danh sách sản phẩm chưa xét duyệt
//	        List<KiemKeKho> danhSachKiemKeChuaDuyet = kiemKeKhoService.findByLichSuCaLamViecId(id).stream()
//	                .filter(kk -> !kk.getDaXetDuyet()) // Chỉ lấy sản phẩm chưa xét duyệt
//	                .collect(Collectors.toList());
//
//	        if (!danhSachKiemKeChuaDuyet.isEmpty()) {
//	            redirectAttributes.addFlashAttribute("errorMessage", "Có sản phẩm đang chờ xét duyệt. Không thể kết thúc ca làm việc.");
//	            redirectAttributes.addFlashAttribute("danhSachKiemKeChuaDuyet", danhSachKiemKeChuaDuyet);
//	            return "redirect:/warehouse/import/shifts"; // Quay lại danh sách ca làm việc
//	        }
//
//	        // Nếu không có sản phẩm chờ xét duyệt thì cho phép kết thúc ca
//	        if (caLamViec.getThoiGianKetThuc() == null) {
//	            caLamViec.setThoiGianKetThuc(LocalDateTime.now());
//	            lichSuCaLamViecService.saveShift(caLamViec);
//	        }
//	    }
//
//	    redirectAttributes.addFlashAttribute("successMessage", "Ca làm việc đã kết thúc.");
//	    return "redirect:/warehouse/import/shifts";
//	}

}
