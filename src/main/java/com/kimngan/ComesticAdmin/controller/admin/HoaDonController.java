package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private DonHangService donHangService;

    // Hiển thị danh sách hóa đơn
    @GetMapping("/hoadon")
    public String getHoaDons(
    		HttpServletRequest request,
    		Model model, 
    		@RequestParam(value = "tenNguoiNhan", required = false) String tenNguoiNhan,
    		@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    	    @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
    		@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        // Kiểm tra nếu page nhỏ hơn 0 thì đặt lại giá trị page về 0
        if (page < 0) {
            page = 0;
        }

      //  Page<HoaDon> hoaDonPage = hoaDonService.getAllHoaDons(PageRequest.of(page, size));
        Page<HoaDon> hoaDonPage;

        // Nếu có từ khóa tìm kiếm theo ngày xuất
     // Nếu có từ khóa tìm kiếm tên người nhận và ngày xuất
        // Nếu có từ khóa tìm kiếm tên người nhận và ngày xuất
     // Nếu có từ khóa tìm kiếm tên người nhận và ngày xuất hóa đơn
        if ((tenNguoiNhan != null && !tenNguoiNhan.isEmpty()) && (startDate != null && endDate != null && !endDate.isBefore(startDate))) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            hoaDonPage = hoaDonService.searchByTenNguoiNhanAndNgayXuatHoaDon(tenNguoiNhan, startDateTime, endDateTime, PageRequest.of(page, size));
            model.addAttribute("tenNguoiNhan", tenNguoiNhan);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
        }

        // Nếu có từ khóa tìm kiếm theo ngày xuất
        else if (startDate != null && endDate != null && !endDate.isBefore(startDate)) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            hoaDonPage = hoaDonService.searchByNgayXuat(startDateTime, endDateTime, PageRequest.of(page, size));
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
        } 
        // Nếu có từ khóa tìm kiếm tên người nhận
        else if (tenNguoiNhan != null && !tenNguoiNhan.isEmpty()) {
            hoaDonPage = hoaDonService.searchByTenNguoiNhan(tenNguoiNhan, PageRequest.of(page, size));
            model.addAttribute("tenNguoiNhan", tenNguoiNhan);
        } 
        // Nếu không có từ khóa tìm kiếm, lấy tất cả hóa đơn
        else {
            hoaDonPage = hoaDonService.getAllHoaDons(PageRequest.of(page, size));
        }

        // Kiểm tra nếu không có kết quả
        if (hoaDonPage.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy kết quả nào cho thông tin tìm kiếm của bạn.");
        }

        model.addAttribute("hoaDons", hoaDonPage.getContent());
        model.addAttribute("currentPage", hoaDonPage.getNumber());
        model.addAttribute("totalPages", hoaDonPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("searchAction", "/admin/hoadon");
        model.addAttribute("requestUri", request.getRequestURI());
        // Thêm thông tin người dùng vào model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/hoadon/index";
    }

    // Xem chi tiết hóa đơn
    @GetMapping("/hoadon/{maDonHang}")
    public String viewHoaDon(
    		@PathVariable("maDonHang") Integer maDonHang,
            HttpServletRequest request,
            Model model,
            @RequestParam(value = "tenNguoiNhan", required = false) String tenNguoiNhan,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size
    		
    		) {
    	 // Nếu có từ khóa tìm kiếm, chuyển hướng về danh sách hóa đơn với các tham số tìm kiếm
        if ((tenNguoiNhan != null && !tenNguoiNhan.isEmpty()) || 
            (startDate != null && endDate != null && !endDate.isBefore(startDate))) {

            String redirectUrl = "/admin/hoadon?";
            if (tenNguoiNhan != null && !tenNguoiNhan.isEmpty()) {
                redirectUrl += "tenNguoiNhan=" + tenNguoiNhan + "&";
            }
            if (startDate != null && endDate != null) {
                redirectUrl += "startDate=" + startDate + "&endDate=" + endDate + "&";
            }
            redirectUrl += "page=" + page + "&size=" + size;

            return "redirect:" + redirectUrl;
        }

        DonHang donHang = donHangService.getDonHangById(maDonHang);
        if (donHang == null) {
            return "redirect:/admin/orders"; // Nếu đơn hàng không tồn tại, chuyển về danh sách đơn hàng
        }

        HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
        if (hoaDon == null) {
            model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
            return "redirect:/admin/orders"; // Nếu hóa đơn không tồn tại, chuyển về danh sách đơn hàng
        }

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("requestUri", "/admin/hoadon"); // Hoặc "/admin/hoadon/{maDonHang}"


        // Thêm thông tin người dùng vào model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/hoadon/view"; // Trả về view để hiển thị chi tiết hóa đơn
    }
}
