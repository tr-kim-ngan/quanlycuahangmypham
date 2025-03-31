package com.kimngan.ComesticAdmin.controller.shipper;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
@Controller
@RequestMapping("/shipper/thongke")
public class ShipperThongKeController {
	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private DonHangService donHangService;
  

	@GetMapping
	public String thongKeShipper(Model model, Principal principal) {
		if (principal == null) return "redirect:/shipper/login";

		String username = principal.getName();
		NguoiDung shipper = nguoiDungService.findByTenNguoiDung(username);

		if (shipper == null) return "redirect:/shipper/login";

		// Lấy số lượng từng trạng thái
		int tongThanhCong = donHangService.findByTrangThaiAndShipper(shipper, "Đã hoàn thành").size();
		int tongDangGiao = donHangService.findByTrangThaiAndShipper(shipper, "Đang chuẩn bị hàng").size();
		int tongDangGiaoHang = donHangService.findByTrangThaiAndShipper(shipper, "Đang giao hàng").size();
		int tongThatBai = donHangService.findByTrangThaiAndShipper(shipper, "Đã hủy").size();

		// Tính tổng đơn thực tế
		int tongTatCa = tongThanhCong + tongDangGiao + tongDangGiaoHang + tongThatBai;

		// Tỷ lệ hoàn thành
		double tyLeHoanThanh = (tongTatCa == 0) ? 0 : ((double) tongThanhCong / tongTatCa) * 100;

		// Gửi dữ liệu sang view
		model.addAttribute("tongThanhCong", tongThanhCong);
		model.addAttribute("tongThatBai", tongThatBai);
		model.addAttribute("tongDangGiao", tongDangGiao);
		model.addAttribute("tongDangGiaoHang", tongDangGiaoHang);
		model.addAttribute("tyLeHoanThanh", tyLeHoanThanh);

		return "shipper/thongke/index";
	}

}
