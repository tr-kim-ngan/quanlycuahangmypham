package com.kimngan.ComesticAdmin.controller.seller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ui.Model;

import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.YeuCauBoSung;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import com.kimngan.ComesticAdmin.services.YeuCauBoSungService;
@Controller
@RequestMapping("/seller")
public class YeuCauBoSungController {

    @Autowired
    private ChiTietDonNhapHangService chiTietDonNhapHangService;

   @Autowired
   private SanPhamService sanPhamService;
   @Autowired 
   private YeuCauBoSungService yeuCauBoSungService;
   
   @Autowired
   private ChiTietDonHangService chiTietDonHangService;

   @Autowired
   private KiemKeKhoService kiemKeKhoService;

   @Autowired
   private DonHangService donHangService;
   @GetMapping("/products-to-request")
   public String showProductsToRequest(Model model,
		   @RequestParam(value = "keyword", required = false) String keyword,
		    @RequestParam(value = "page", defaultValue = "0") int page,
		    @RequestParam(value = "size", defaultValue = "10") int size
		   
		   
		   ) {
	   Page<SanPham> danhSachSanPham;

	    if (keyword != null && !keyword.trim().isEmpty()) {
	        danhSachSanPham = sanPhamService.searchActiveByName(keyword.trim(), PageRequest.of(page, size));
	    } else {
	        danhSachSanPham = sanPhamService.findAllActive(PageRequest.of(page, size));
	    }

	   
//       List<SanPham> danhSachSanPham = sanPhamService.findByTrangThai(true);
      List<YeuCauBoSung> danhSachYeuCau = yeuCauBoSungService.getAll();

       Map<Integer, YeuCauBoSung> daYeuCauMap = danhSachYeuCau.stream()
           .collect(Collectors.toMap(
               yc -> yc.getSanPham().getMaSanPham(),
               Function.identity(),
               (oldVal, newVal) -> newVal
           ));

       Map<Integer, Integer> tongSoLuongNhapMap = new HashMap<>();
       Map<Integer, Integer> soLuongTonKhoMap = new HashMap<>();

       for (SanPham sanPham : danhSachSanPham) {
           Integer maSanPham = sanPham.getMaSanPham();

           int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
           int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(maSanPham);
           int soLuongTrenKe = sanPhamService.getSoLuongTrenKe(maSanPham);
           int deltaKiemKe = kiemKeKhoService.getDeltaKiemKe(maSanPham);
           int soLuongTraHang = donHangService.getSoLuongTraHang(maSanPham);

           Integer tonKhoDaDuyet = kiemKeKhoService.getLastApprovedStock(maSanPham);

           int soLuongTonKho = (tonKhoDaDuyet != null)
               ? (tongSoLuongNhap - soLuongBan - soLuongTrenKe + deltaKiemKe + soLuongTraHang)
               : (tongSoLuongNhap - soLuongBan - soLuongTrenKe + soLuongTraHang);

           tongSoLuongNhapMap.put(maSanPham, tongSoLuongNhap);
           soLuongTonKhoMap.put(maSanPham, soLuongTonKho);
       }

       model.addAttribute("sanPhamList",danhSachSanPham.getContent());
       model.addAttribute("currentPage", page);
       model.addAttribute("totalPages", danhSachSanPham.getTotalPages());
       model.addAttribute("keyword", keyword);
       
       model.addAttribute("daYeuCauMap", daYeuCauMap);
       model.addAttribute("tongSoLuongNhapMap", tongSoLuongNhapMap);
       model.addAttribute("soLuongTonKhoMap", soLuongTonKhoMap);

       return "seller/product/request-stock";
   }

    
   @PostMapping("/yeu-cau-bo-sung")
   public String guiNhieuYeuCau(
           @RequestParam("selectedIds") List<Integer> selectedIds,
           @RequestParam Map<String, String> soLuongYeuCauMap,
           RedirectAttributes redirectAttributes) {

       NguoiDungDetails userDetails = (NguoiDungDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

       for (Integer maSanPham : selectedIds) {
           String key = "soLuongYeuCauMap[" + maSanPham + "]";
           if (soLuongYeuCauMap.containsKey(key)) {
               String value = soLuongYeuCauMap.get(key);
               try {
                   int soLuong = Integer.parseInt(value);
                   if (soLuong > 0) {
                       SanPham sp = sanPhamService.findById(maSanPham);
                       YeuCauBoSung yeuCau = new YeuCauBoSung();
                       yeuCau.setSanPham(sp);
                       yeuCau.setSoLuongYeuCau(soLuong);
                       yeuCau.setThoiGianYeuCau(LocalDateTime.now());
                       yeuCau.setNguoiYeuCau(userDetails.getNguoiDung());
                       yeuCau.setDaXuLy(false);

                       yeuCauBoSungService.save(yeuCau);
                   }
               } catch (NumberFormatException e) {
                   e.printStackTrace(); // log lỗi
               }
           }
       }

       redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu bổ sung cho các sản phẩm đã chọn.");
       return "redirect:/seller/products-to-request";
   }


}
