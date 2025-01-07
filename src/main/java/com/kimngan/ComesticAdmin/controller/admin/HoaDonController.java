package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@Controller
@RequestMapping("/admin")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private DonHangService donHangService;

    @GetMapping("/hoadon")
    public String getHoaDons(
            HttpServletRequest request,
            Model model,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        if (page < 0) {
            page = 0;
        }

        Page<HoaDon> hoaDonPage = Page.empty();

        // Kiểm tra nếu ngày bắt đầu sau ngày kết thúc
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            model.addAttribute("errorMessage", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        } else {
            // Lọc theo trạng thái nếu được cung cấp
            if (status != null && !status.isEmpty()) {
                hoaDonPage = hoaDonService.searchByStatus(status, PageRequest.of(page, size));
                model.addAttribute("selectedStatus", status);
            }
            // Nếu có từ khóa tìm kiếm theo ngày xuất
            else if (startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                hoaDonPage = hoaDonService.searchByNgayXuat(startDateTime, endDateTime, PageRequest.of(page, size));
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
            }
            // Nếu không có từ khóa tìm kiếm, lấy tất cả hóa đơn
            else {
                hoaDonPage = hoaDonService.getAllHoaDons(PageRequest.of(page, size));
            }

            // Kiểm tra nếu không có kết quả
            if (hoaDonPage.isEmpty()) {
                model.addAttribute("message", "Không tìm thấy kết quả nào cho thông tin tìm kiếm của bạn.");
            }
        }

        // Định dạng ngày thành chuỗi để truyền vào model
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = (startDate != null) ? startDate.format(formatter) : "";
        String formattedEndDate = (endDate != null) ? endDate.format(formatter) : "";
        boolean isEmpty = hoaDonPage.isEmpty();

        model.addAttribute("isEmpty", isEmpty);
        model.addAttribute("formattedStartDate", formattedStartDate);
        model.addAttribute("formattedEndDate", formattedEndDate);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
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
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

    	
    	
    	// Điều hướng về trang danh sách hóa đơn nếu nhấn tìm kiếm
        if (status != null || startDate != null || endDate != null) {
            String redirectUrl = "/admin/hoadon?";

            if (status != null) {
                try {
                    redirectUrl += "status=" + URLEncoder.encode(status, StandardCharsets.UTF_8.toString()) + "&";
                } catch (Exception e) {
                    // Log lỗi nếu mã hóa thất bại
                    e.printStackTrace();
                }
            }

            if (startDate != null) {
                redirectUrl += "startDate=" + startDate + "&";
            }

            if (endDate != null) {
                redirectUrl += "endDate=" + endDate + "&";
            }

            redirectUrl += "page=" + page + "&size=" + size;

            return "redirect:" + redirectUrl;
        }
        // Lấy thông tin đơn hàng
        DonHang donHang = donHangService.getDonHangById(maDonHang);
        if (donHang == null) {
            return "redirect:/admin/orders"; // Nếu đơn hàng không tồn tại, chuyển về danh sách đơn hàng
        }

        // Lấy thông tin hóa đơn liên kết với đơn hàng
        HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
        if (hoaDon == null) {
            model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
            return "redirect:/admin/orders"; // Nếu hóa đơn không tồn tại, chuyển về danh sách đơn hàng
        }

        // Đưa dữ liệu cần thiết vào model
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("requestUri", request.getRequestURI());

        // Thêm thông tin người dùng vào model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/hoadon/view"; // Trả về view để hiển thị chi tiết hóa đơn
    }

    @PostMapping("/hoadon/xacnhan/{maHoaDon}")
    public String xacNhanThanhToanHoaDon(@PathVariable("maHoaDon") Integer maHoaDon, RedirectAttributes redirectAttributes) {
        try {
            hoaDonService.xacNhanThanhToan(maHoaDon);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán hóa đơn thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // Chuyển hướng về chi tiết hóa đơn dựa vào mã đơn hàng liên kết với hóa đơn
        HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
        Integer maDonHang = hoaDon.getDonHang().getMaDonHang();
        return "redirect:/admin/hoadon/" + maDonHang;
    }


}
