package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;

import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;


import org.springframework.beans.factory.annotation.Autowired;



@Controller
@RequestMapping("/admin")
public class AdminController {

	
	@Autowired
    private NguoiDungService nguoiDungService;
	@Autowired
	private DonHangService donHangService;

	@Autowired
	private SanPhamService sanPhamService;
	
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
        model.addAttribute("totalRevenue", totalRevenue);
		
     // Thêm số hóa đơn chưa xác nhận vào model
        long unconfirmedInvoices = hoaDonService.countUnconfirmedInvoices();
        model.addAttribute("unconfirmedInvoices", unconfirmedInvoices);
		
		
		
		
		
		
		

		return "admin/index"; // Trang chính cho admin sau khi đăng nhập thành công
	}

	// Thêm một phương thức để chuyển hướng từ "/" sang "/admin/index"

}
