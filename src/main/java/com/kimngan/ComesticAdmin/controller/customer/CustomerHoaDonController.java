package com.kimngan.ComesticAdmin.controller.customer;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
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

//    // Xem chi tiết hóa đơn
//    @GetMapping("/hoadon/{maDonHang}")
//    public String viewHoaDon(@PathVariable("maDonHang") Integer maDonHang, 
//    		Principal principal,
//    		Model model) {
//        DonHang donHang = donHangService.getDonHangById(maDonHang);
//        if (donHang == null) {
//            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng này.");
//            return "customer/hoadon"; // Chuyển về trang danh sách hóa đơn nếu đơn hàng không tồn tại
//        }
//
//        HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
//        if (hoaDon == null) {
//            model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
//            return "customer/hoadon"; // Chuyển về trang danh sách hóa đơn nếu hóa đơn không tồn tại
//        }
//        DecimalFormat formatter = new DecimalFormat("#,###.##");
//        String formattedTotal = formatter.format(hoaDon.getTongTien());
//        model.addAttribute("formattedTotal", formattedTotal);
//
//        model.addAttribute("hoaDon", hoaDon);
//        return "customer/hoadon"; // Trả về view để hiển thị chi tiết hóa đơn
//    }

//    
  

    @GetMapping("/hoadon/{maDonHang}")
    public String viewHoaDon(@PathVariable("maDonHang") Integer maDonHang, Model model, Principal principal) {
        DonHang donHang = donHangService.getDonHangById(maDonHang);
//        if (donHang == null) {
//        	
//            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng này.");
//            return "customer/hoadon"; // Quay lại trang hóa đơn nếu không tìm thấy đơn hàng
//        }
        
        if (donHang == null) {
            throw new RuntimeException("Đơn hàng không tồn tại với mã: " + maDonHang);
        }

        HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
        if (hoaDon == null) {
        	
            System.out.println("Không tìm thấy đơn hàng với mã: " + maDonHang);

            model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
            return "customer/hoadon"; // Quay lại trang hóa đơn nếu không tìm thấy hóa đơn
        
        }

        String username = principal.getName();
        NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);
        System.out.println("Người dùng hiện tại: " + username);
        
        // Thêm danh sách chi tiết đơn hàng vào model
        List<ChiTietDonHang> chiTietDonHangs = hoaDon.getDonHang().getChiTietDonHangs();
        model.addAttribute("chiTietDonHangs", chiTietDonHangs);

        // Kiểm tra trạng thái đánh giá của từng sản phẩm
        List<Boolean> danhGiaStatuses = new ArrayList<>();
//        for (ChiTietDonHang chiTiet : chiTietDonHangs) {
//
//            boolean daDanhGia = danhGiaService.existsByHoaDonAndNguoiDung(hoaDon, nguoiDung);
//            danhGiaStatuses.add(daDanhGia);
//            System.out.println("Sản phẩm: " + chiTiet.getSanPham().getTenSanPham() + " đã được đánh giá: " + daDanhGia);
//
//        }
        if (chiTietDonHangs != null && !chiTietDonHangs.isEmpty()) {
            for (ChiTietDonHang chiTiet : chiTietDonHangs) {
                boolean daDanhGia = danhGiaService.existsByHoaDonAndNguoiDung(hoaDon, nguoiDung);
                danhGiaStatuses.add(daDanhGia);
            }
        }
        model.addAttribute("danhGiaStatuses", danhGiaStatuses);
     
        model.addAttribute("danhGiaStatuses", danhGiaStatuses);
        model.addAttribute("danhGiaStatuses", danhGiaStatuses);
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("nguoiDung", nguoiDung);

        DecimalFormat formatter = new DecimalFormat("#,###.##");
        String formattedTotal = formatter.format(hoaDon.getTongTien());
        model.addAttribute("formattedTotal", formattedTotal);

        return "customer/hoadon";
    }

   

    @GetMapping("/hoadon/{maHoaDon}/danhgia/{maSanPham}/view")
    public String viewRating(
            @PathVariable("maHoaDon") Integer maHoaDon,
            @PathVariable("maSanPham") Integer maSanPham,
            Model model,
            Principal principal) {

        String username = principal.getName();
        NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);
        HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
        SanPham sanPham = sanPhamService.findById(maSanPham);

        if (hoaDon == null || sanPham == null || nguoiDung == null) {
            model.addAttribute("errorMessage", "Không tìm thấy hóa đơn, sản phẩm hoặc người dùng để xem đánh giá.");
            return "redirect:/customer/hoadon/" + maHoaDon;
        }

        DanhGia danhGia = danhGiaService.findByHoaDonAndSanPhamAndNguoiDung(hoaDon, sanPham, nguoiDung);

        if (danhGia == null) {
            model.addAttribute("errorMessage", "Không tìm thấy đánh giá cho sản phẩm này.");
            return "redirect:/customer/hoadon/"  + hoaDon.getDonHang().getMaDonHang();
        }

        model.addAttribute("danhGia", danhGia);
        return "customer/view_rating";
    }

    

    @GetMapping("/hoadon/{maHoaDon}/danhgia/{maSanPham}")
    public String showRatingForm(@PathVariable("maHoaDon") Integer maHoaDon,
                                 @PathVariable("maSanPham") Integer maSanPham,
                                 Model model, Principal principal, 
                                 RedirectAttributes redirectAttributes) {
        // Lấy thông tin hóa đơn và sản phẩm
        HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
        SanPham sanPham = sanPhamService.findById(maSanPham);
        
        if (hoaDon == null || sanPham == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn hoặc sản phẩm để đánh giá.");
            return "redirect:/customer/hoadon" + hoaDon.getDonHang().getMaDonHang();
        }

        String username = principal.getName();
        NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
        if (danhGiaService.existsByHoaDonAndNguoiDung(hoaDon, nguoiDung)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá sản phẩm này rồi.");
            return "redirect:/customer/hoadon/" + maHoaDon;
        }

        // Thêm thông tin sản phẩm vào model để hiển thị trên form
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("errorMessage", ""); // Gán giá trị mặc định cho errorMessage
        return "customer/rating_form";
    }
 


   
    @PostMapping("/hoadon/{maHoaDon}/danhgia/{maSanPham}")
    public String submitRating(
            @PathVariable("maHoaDon") Integer maHoaDon,
            @PathVariable("maSanPham") Integer maSanPham,
            @RequestParam("rating") int rating,  // Đảm bảo tên 'rating' khớp với form
            @RequestParam("comment") String comment,  // Đảm bảo tên 'comment' khớp với form
            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        NguoiDung nguoiDung = nguoiDungService.findByTenNguoiDung(username);
        HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
        SanPham sanPham = sanPhamService.findById(maSanPham);
        
        // In ra để kiểm tra
        System.out.println("=== Start submitRating Method ===");
        System.out.println("Username: " + username);
        System.out.println("HoaDon (maHoaDon=" + maHoaDon + "): " + (hoaDon != null ? hoaDon.toString() : "null"));
        System.out.println("SanPham (maSanPham=" + maSanPham + "): " + (sanPham != null ? sanPham.toString() : "null"));


        if (hoaDon == null || sanPham == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hóa đơn hoặc sản phẩm để đánh giá.");
            return "redirect:/customer/hoadon";
        }
        DonHang donHang = hoaDon.getDonHang();
        if (donHang == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không tồn tại.");
            return "redirect:/customer/hoadon";
        }

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này trong hóa đơn này chưa
        if (danhGiaService.existsByHoaDonAndNguoiDung(hoaDon, nguoiDung)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá sản phẩm này rồi.");
            return "redirect:/customer/hoadon/" + hoaDon.getDonHang().getMaDonHang();
        }

        DanhGia danhGia = new DanhGia();
        danhGia.setHoaDon(hoaDon);
        danhGia.setSanPham(sanPham);
        danhGia.setNguoiDung(nguoiDung);
        danhGia.setSoSao(rating);
        danhGia.setNoiDung(comment);
        danhGia.setThoiGianDanhGia(LocalDateTime.now());
        danhGiaService.create(danhGia);

     // Kiểm tra thông tin trước khi lưu
        System.out.println("DanhGia sẽ lưu:");
        System.out.println(" - MaHoaDon: " + danhGia.getHoaDon().getMaHoaDon());
        System.out.println(" - MaSanPham: " + danhGia.getSanPham().getMaSanPham());
        System.out.println(" - MaNguoiDung: " + danhGia.getNguoiDung().getMaNguoiDung());
        System.out.println(" - Số Sao: " + danhGia.getSoSao());
        System.out.println(" - Nội Dung: " + danhGia.getNoiDung());

        // Lưu đánh giá
        danhGiaService.create(danhGia);
        System.out.println("=== End submitRating Method ===");

        redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm.");
        return "redirect:/customer/hoadon/" + hoaDon.getDonHang().getMaDonHang();
    }




}

