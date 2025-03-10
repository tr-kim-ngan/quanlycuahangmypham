package com.kimngan.ComesticAdmin.controller.admin;


import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonHangService;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;

import com.kimngan.ComesticAdmin.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;


import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

	


	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private SanPhamService sanPhamService;


	@Autowired
	private ChiTietDonHangService chiTietDonHangService;
	
	
    @GetMapping
    public String hienThiTonKho(Model model, 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false, defaultValue = "true") Boolean trangThai) {

        Page<SanPham> pageSanPham;

        if (keyword != null && !keyword.isEmpty()) {
            pageSanPham = sanPhamService.searchAllActiveProductsWithOrderDetails(keyword,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "maSanPham")));
        } else {
            pageSanPham = sanPhamService.findByTrangThai(true,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "maSanPham")));
        }

        Map<Integer, Integer> tongSoLuongNhapMap = new HashMap<>();
        Map<Integer, Integer> soLuongTonKhoMap = new HashMap<>();

        for (SanPham sanPham : pageSanPham.getContent()) {
            int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(sanPham.getMaSanPham());
            int soLuongBan = chiTietDonHangService.getTotalQuantityBySanPhamId(sanPham.getMaSanPham());

            int soLuongNhapThucTe = tongSoLuongNhap - soLuongBan;
            int soLuongTonKho = soLuongNhapThucTe - sanPham.getSoLuong();

            tongSoLuongNhapMap.put(sanPham.getMaSanPham(), soLuongNhapThucTe);
            soLuongTonKhoMap.put(sanPham.getMaSanPham(), soLuongTonKho);
        }

        model.addAttribute("danhSachSanPham", pageSanPham.getContent());
        model.addAttribute("tongSoLuongNhapMap", tongSoLuongNhapMap);
        model.addAttribute("soLuongTonKhoMap", soLuongTonKhoMap);
        model.addAttribute("currentPage", pageSanPham.getNumber());
        model.addAttribute("totalPages", pageSanPham.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/inventory/inventory"; // View file inventory.html trong admin
    }

    @PostMapping("/update-stock")
    public String capNhatSoLuong(@RequestParam("maSanPham") Integer maSanPham,
            @RequestParam("soLuongMoi") Integer soLuongMoi, RedirectAttributes redirectAttributes) {

        SanPham sanPham = sanPhamService.findById(maSanPham);
        if (sanPham == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
            return "redirect:/admin/inventory";
        }

        int tongSoLuongNhap = chiTietDonNhapHangService.getTotalImportedQuantityBySanPhamId(maSanPham);
        int soLuongTonKho = tongSoLuongNhap - sanPham.getSoLuong();

        if (soLuongMoi > tongSoLuongNhap) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Số lượng trên kệ không thể lớn hơn tổng số lượng nhập (" + tongSoLuongNhap + ").");
            return "redirect:/admin/inventory";
        }

        if (soLuongMoi < 0 || soLuongMoi > soLuongTonKho + sanPham.getSoLuong()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Số lượng trên kệ phải nằm trong khoảng từ 0 đến " + (soLuongTonKho + sanPham.getSoLuong()) + ".");
            return "redirect:/admin/inventory";
        }

        sanPham.setSoLuong(soLuongMoi);
        sanPhamService.update(sanPham);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số lượng trên kệ thành công.");
        return "redirect:/admin/inventory";
    }
}
