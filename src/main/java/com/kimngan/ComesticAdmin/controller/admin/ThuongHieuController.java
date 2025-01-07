package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ThuongHieu;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.ThuongHieuService;
import com.kimngan.ComesticAdmin.services.StorageService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    private StorageService storageService;

    // Hiển thị danh sách thương hiệu
    @GetMapping("/brand")
    public String index(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size,
                        @RequestParam(value = "keyword", required = false) String keyword) {

        Page<ThuongHieu> pageThuongHieu;


        if (keyword != null && !keyword.isEmpty()) {
            pageThuongHieu = thuongHieuService.searchByName(keyword, PageRequest.of(page, size));
            model.addAttribute("keyword", keyword);
        } else {
        	// Lấy tất cả thương hiệu, sắp xếp theo maThuongHieu giảm dần
            pageThuongHieu = thuongHieuService.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maThuongHieu")));
           }

        if (page > pageThuongHieu.getTotalPages()) {
            pageThuongHieu = thuongHieuService.findAll(PageRequest.of(pageThuongHieu.getTotalPages() - 1, size));
        }

        model.addAttribute("listThuongHieu", pageThuongHieu.getContent());
        model.addAttribute("currentPage", pageThuongHieu.getNumber());
        model.addAttribute("totalPages", pageThuongHieu.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("searchAction", "/admin/brand");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/brand/index";
    }

    // Thêm thương hiệu
    @GetMapping("/add-brand")
    public String addBrandPage(Model model) {
        model.addAttribute("thuongHieu", new ThuongHieu());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/brand/add";
    }

    @PostMapping("/add-brand")
    public String save(@ModelAttribute("thuongHieu") ThuongHieu thuongHieu,
                       @RequestParam("imageFile") MultipartFile imageFile, Model model) {

        if (thuongHieuService.existsByTenThuongHieu(thuongHieu.getTenThuongHieu())) {
            model.addAttribute("error", "Tên thương hiệu đã tồn tại!");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
            model.addAttribute("user", userDetails);
            return "admin/brand/add";
        }

        if (!imageFile.isEmpty()) {
            try {
                long maxFileSize = 2 * 1024 * 1024; // 2MB
                if (imageFile.getSize() > maxFileSize) {
                    model.addAttribute("error", "Kích thước ảnh không được vượt quá 2MB!");
                    model.addAttribute("thuongHieu", thuongHieu);
                    return "admin/brand/add";
                }

                String contentType = imageFile.getContentType();
                if (contentType != null && !contentType.startsWith("image/")) {
                    model.addAttribute("error", "Vui lòng chọn tệp hình ảnh hợp lệ!");
                    model.addAttribute("thuongHieu", thuongHieu);
                    return "admin/brand/add";
                }

                String fileName = storageService.storeFile(imageFile);
                thuongHieu.setHinhAnh(fileName);
            } catch (IOException e) {
                model.addAttribute("error", "Lỗi khi lưu ảnh.");
                model.addAttribute("thuongHieu", thuongHieu);
                return "admin/brand/add";
            }
        }

        if (thuongHieuService.create(thuongHieu)) {
            return "redirect:/admin/brand";
        } else {
            model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
            return "admin/brand/add";
        }
    }

    // Sửa thương hiệu
    @GetMapping("/edit-brand/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        ThuongHieu thuongHieu = thuongHieuService.findById(id);

        if (thuongHieu == null) {
            return "redirect:/admin/brand";
        }

        model.addAttribute("thuongHieu", thuongHieu);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
        model.addAttribute("user", userDetails);

        return "admin/brand/edit";
    }

    @PostMapping("/edit-brand")
    public String update(@ModelAttribute("thuongHieu") ThuongHieu thuongHieu,
                         @RequestParam("imageFile") MultipartFile imageFile, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
            NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
            model.addAttribute("user", userDetails);
        }

        ThuongHieu existingThuongHieu = thuongHieuService.findById(thuongHieu.getMaThuongHieu());
        if (existingThuongHieu == null) {
            model.addAttribute("error", "Thương hiệu không tồn tại!");
            return "admin/brand/edit";
        }

        if (!existingThuongHieu.getTenThuongHieu().equals(thuongHieu.getTenThuongHieu())
                && thuongHieuService.existsByTenThuongHieu(thuongHieu.getTenThuongHieu())) {
            model.addAttribute("error", "Tên thương hiệu đã tồn tại!");
            model.addAttribute("thuongHieu", thuongHieu);
            return "admin/brand/edit";
        }

        if (!imageFile.isEmpty()) {
            try {
                long maxFileSize = 2 * 1024 * 1024; // 2MB
                if (imageFile.getSize() > maxFileSize) {
                    model.addAttribute("error", "Kích thước ảnh không được vượt quá 2MB!");
                    model.addAttribute("thuongHieu", thuongHieu);
                    return "admin/brand/edit";
                }

                String contentType = imageFile.getContentType();
                if (contentType != null && !contentType.startsWith("image/")) {
                    model.addAttribute("error", "Vui lòng chọn tệp hình ảnh hợp lệ!");
                    return "admin/brand/edit";
                }

                String fileName = storageService.storeFile(imageFile);
                thuongHieu.setHinhAnh(fileName);
            } catch (IOException e) {
                model.addAttribute("error", "Có lỗi xảy ra khi lưu hình ảnh!");
                return "admin/brand/edit";
            }
        } else {
            thuongHieu.setHinhAnh(existingThuongHieu.getHinhAnh());
        }

        if (thuongHieuService.update(thuongHieu)) {
            model.addAttribute("success", "Cập nhật thành công!");
            model.addAttribute("thuongHieu", thuongHieu);
        } else {
            model.addAttribute("error", "Cập nhật thất bại!");
        }

        return "admin/brand/edit";
    }

    @GetMapping("/delete-brand/{id}")
    public String deleteBrand(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        ThuongHieu thuongHieu = thuongHieuService.findById(id);
        if (thuongHieu == null) {
            return "redirect:/admin/brand";
        }

        if (thuongHieuService.hasProducts(id)) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa thương hiệu này vì nó đang chứa sản phẩm!");
            return "redirect:/admin/brand";
        }

        thuongHieuService.delete(id);
        return "redirect:/admin/brand";
    }
}
