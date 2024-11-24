package com.kimngan.ComesticAdmin.controller.customer;

import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.DanhGia;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.DanhGiaService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.SanPhamService;

@Controller
@RequestMapping("/customer")
public class CustomerHoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private DonHangService donHangService;
    
    @Autowired
    private NguoiDungService nguoiDungService;
    
    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private DanhGiaService danhGiaService;

    // Xem danh sách hóa đơn
    @GetMapping("/hoadon")
    public String getHoaDons(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<HoaDon> hoaDons = hoaDonService.getHoaDonsByCustomer(username);
        model.addAttribute("hoaDons", hoaDons);
        return "customer/hoadon";
    }

    // Xem chi tiết hóa đơn
    @GetMapping("/hoadon/{maDonHang}")
    public String viewHoaDon(@PathVariable("maDonHang") Integer maDonHang, Model model) {
        DonHang donHang = donHangService.getDonHangById(maDonHang);
        if (donHang == null) {
            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng này.");
            return "customer/hoadon"; // Chuyển về trang danh sách hóa đơn nếu đơn hàng không tồn tại
        }

        HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
        if (hoaDon == null) {
            model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
            return "customer/hoadon"; // Chuyển về trang danh sách hóa đơn nếu hóa đơn không tồn tại
        }
        DecimalFormat formatter = new DecimalFormat("#,###.##");
        String formattedTotal = formatter.format(hoaDon.getTongTien());
        model.addAttribute("formattedTotal", formattedTotal);

        model.addAttribute("hoaDon", hoaDon);
        return "customer/hoadon"; // Trả về view để hiển thị chi tiết hóa đơn
    }
   
    @PostMapping("/hoadon/{maHoaDon}/danhgia/{maSanPham}")
    public String submitRating(
            @PathVariable("maHoaDon") Integer maHoaDon,
            @PathVariable("maSanPham") Integer maSanPham,
            @RequestParam("rating") int rating,
            @RequestParam("comment") String comment,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);
        HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
        SanPham sanPham = sanPhamService.findById(maSanPham);

        if (hoaDon == null || sanPham == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn hoặc sản phẩm để đánh giá.");
            return "redirect:/customer/hoadon";
        }

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này trong hóa đơn này chưa
        if (danhGiaService.existsByHoaDonAndNguoiDung(maHoaDon, nguoiDung.getMaNguoiDung())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá sản phẩm này rồi.");
            return "redirect:/customer/hoadon/" + maHoaDon;
        }

        // Tạo đánh giá mới
        DanhGia danhGia = new DanhGia();
        danhGia.setHoaDon(hoaDon);
        danhGia.setSanPham(sanPham);
        danhGia.setNguoiDung(nguoiDung);
        danhGia.setSoSao(rating);
        danhGia.setNoiDung(comment);
        danhGia.setThoiGianDanhGia(LocalDateTime.now());

        // Lưu đánh giá
        danhGiaService.create(danhGia);

        redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm.");
        return "redirect:/customer/hoadon/" + maHoaDon;
    }


}

