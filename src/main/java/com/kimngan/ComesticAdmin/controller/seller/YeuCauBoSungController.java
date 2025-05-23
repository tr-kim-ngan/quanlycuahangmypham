package com.kimngan.ComesticAdmin.controller.seller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ui.Model;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.entity.YeuCauBoSung;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.KiemKeKhoService;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
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
   
   
   @Autowired
	private NguoiDungService nguoiDungService;
	@ModelAttribute("currentSeller")
	public NguoiDung getCurrentSeller(Principal principal) {
	    if (principal != null) {
	        return nguoiDungService.findByTenNguoiDung(principal.getName());
	    }
	    return null;
	}

	@GetMapping("/products-to-request")
	public String showProductsToRequest(Model model,
	        @RequestParam(value = "keyword", required = false) String keyword,
	        @RequestParam(value = "maSanPham", required = false) String maSanPham,
	        @RequestParam(value = "page", defaultValue = "0") int page,
	        @RequestParam(value = "size", defaultValue = "10") int size) {

	    Pageable pageable = PageRequest.of(page, size);
	    Page<SanPham> danhSachSanPham;

	    if (maSanPham != null && !maSanPham.trim().isEmpty()) {
	        danhSachSanPham = sanPhamService.searchActiveByMaSanPham(maSanPham.trim(), pageable);
	    } else if (keyword != null && !keyword.trim().isEmpty()) {
	        danhSachSanPham = sanPhamService.searchActiveByName(keyword.trim(), pageable);
	    } else {
	        danhSachSanPham = sanPhamService.findAllActive(pageable);
	    }

	    List<YeuCauBoSung> danhSachYeuCau = yeuCauBoSungService.getAll();

	    Map<Integer, YeuCauBoSung> daYeuCauMap = danhSachYeuCau.stream()
	        .collect(Collectors.toMap(
	            yc -> yc.getSanPham().getMaSanPham(),
	            Function.identity(),
	            (oldVal, newVal) -> newVal
	        ));

	    Map<Integer, Integer> tongSoLuongNhapMap = new HashMap<>();
	    Map<Integer, Integer> soLuongTonKhoMap = new HashMap<>();

	    for (SanPham sp : danhSachSanPham.getContent()) {
	        Integer maSP = sp.getMaSanPham();
	        int tongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSP);
	        int ban = chiTietDonHangService.getTotalQuantityBySanPhamId(maSP);
	        int trenKe = sanPhamService.getSoLuongTrenKe(maSP);
	        int delta = kiemKeKhoService.getDeltaKiemKe(maSP);
	        int traHang = donHangService.getSoLuongTraHang(maSP);
	        Integer tonDaDuyet = kiemKeKhoService.getLastApprovedStock(maSP);

	        int tonKho = (tonDaDuyet != null)
	                ? (tongNhap - ban - trenKe + delta + traHang)
	                : (tongNhap - ban - trenKe + traHang);

	        tongSoLuongNhapMap.put(maSP, tongNhap);
	        soLuongTonKhoMap.put(maSP, tonKho);
	    }

	    model.addAttribute("sanPhamList", danhSachSanPham.getContent());
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", danhSachSanPham.getTotalPages());
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("maSanPham", maSanPham);

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
