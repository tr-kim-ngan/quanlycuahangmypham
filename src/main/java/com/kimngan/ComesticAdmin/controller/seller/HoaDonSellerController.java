package com.kimngan.ComesticAdmin.controller.seller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/seller")
public class HoaDonSellerController {

	@Autowired
	private HoaDonService hoaDonService;

	@Autowired
	private DonHangService donHangService;

	@GetMapping("/hoadon")
	public String getHoaDonsForSeller(HttpServletRequest request, Model model,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		if (page < 0) {
			page = 0;
		}

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maHoaDon"));
		Page<HoaDon> hoaDonPage;

		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			model.addAttribute("errorMessage", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
			hoaDonPage = Page.empty();
		} else {
			if (status != null && !status.isEmpty()) {
				hoaDonPage = hoaDonService.searchByStatus(status, pageRequest);
				model.addAttribute("selectedStatus", status);
			} else if (startDate != null && endDate != null) {
				LocalDateTime startDateTime = startDate.atStartOfDay();
				LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
				hoaDonPage = hoaDonService.searchByNgayXuat(startDateTime, endDateTime, pageRequest);
				model.addAttribute("startDate", startDate);
				model.addAttribute("endDate", endDate);
			} else {
				hoaDonPage = hoaDonService.getAllHoaDons(pageRequest);
			}

			if (hoaDonPage.isEmpty()) {
				model.addAttribute("message", "Không tìm thấy hóa đơn phù hợp.");
			}
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedStartDate = (startDate != null) ? startDate.format(formatter) : "";
		String formattedEndDate = (endDate != null) ? endDate.format(formatter) : "";

		model.addAttribute("hoaDons", hoaDonPage.getContent());
		model.addAttribute("formattedStartDate", formattedStartDate);
		model.addAttribute("formattedEndDate", formattedEndDate);
		model.addAttribute("currentPage", hoaDonPage.getNumber());
		model.addAttribute("totalPages", hoaDonPage.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/seller/hoadon");
		model.addAttribute("requestUri", request.getRequestURI());

		// Thêm thông tin người dùng vào model
		
		return "seller/hoadon/index";
	}
}
