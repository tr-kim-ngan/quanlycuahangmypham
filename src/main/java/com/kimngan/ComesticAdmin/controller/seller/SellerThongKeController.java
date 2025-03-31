package com.kimngan.ComesticAdmin.controller.seller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.NguoiDung;

import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

@Controller
@RequestMapping("/seller/thongke")
public class SellerThongKeController {

	@Autowired
	private DonHangService donHangService;

	@Autowired
	private NguoiDungService nguoiDungService;

	@GetMapping
	public String thongKeSeller(
	    @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
	    @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
	    @RequestParam(value = "staffId", required = false) Long staffId,
	    Model model,
	    Principal principal) {

	    if (principal == null) return "redirect:/seller/login";

	    String username = principal.getName();
	    NguoiDung seller = nguoiDungService.findByTenNguoiDung(username);
	    if (seller == null) return "redirect:/seller/login";

	    // Lấy danh sách từng loại người dùng
	    List<NguoiDung> admins = nguoiDungService.findByRole("ADMIN");
	    List<NguoiDung> sellersOnly = nguoiDungService.findByRole("NHAN_VIEN_BAN_HANG");

	    // Gộp lại thành danh sách duy nhất
	    List<NguoiDung> sellers = new ArrayList<>();

	    if (staffId != null) {
	        // Lọc theo nhân viên cụ thể
	        NguoiDung selectedStaff = nguoiDungService.findById(staffId.intValue());
	        if (selectedStaff != null) {
	            sellers.add(selectedStaff);
	        }
	    } else {
	        // Lấy tất cả admin và seller
	        sellers.addAll(admins);
	        sellers.addAll(sellersOnly);
	    }

	    List<String> labels = new ArrayList<>();
	    List<Integer> completedOrders = new ArrayList<>();
	    List<Integer> soldProducts = new ArrayList<>();
	    List<BigDecimal> revenues = new ArrayList<>();

	    for (NguoiDung nguoiBan : sellers) {
	        List<DonHang> donHangList = donHangService.findBySeller(nguoiBan);
	        int done = 0;
	        int sold = 0;
	        BigDecimal total = BigDecimal.ZERO;

	        for (DonHang don : donHangList) {
	            if (!"Đã hoàn thành".equals(don.getTrangThaiDonHang())) continue;
	            if (from != null && don.getNgayDat().toLocalDate().isBefore(from)) continue;
	            if (to != null && don.getNgayDat().toLocalDate().isAfter(to)) continue;

	            done++;
	            total = total.add(don.getTongGiaTriDonHang());

	            for (ChiTietDonHang ct : don.getChiTietDonHangs()) {
	                sold += ct.getSoLuong();
	            }
	        }

	        labels.add(nguoiBan.getHoTen());
	        completedOrders.add(done);
	        soldProducts.add(sold);
	        revenues.add(total);
	    }

	    BigDecimal tongDoanhThu = revenues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

	    model.addAttribute("tongDoanhThu", tongDoanhThu);
	    model.addAttribute("labels", labels);
	    model.addAttribute("completedOrders", completedOrders);
	    model.addAttribute("soldProducts", soldProducts);
	    model.addAttribute("revenues", revenues);
	    model.addAttribute("sellers", sellersOnly); // Chỉ hiển thị nhân viên bán hàng cho dropdown
	    model.addAttribute("from", from);
	    model.addAttribute("to", to);
	    model.addAttribute("staffId", staffId);

	    return "seller/thongke/index";
	}

}
