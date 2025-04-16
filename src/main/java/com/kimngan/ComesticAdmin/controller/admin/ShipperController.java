package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.QuyenTruyCap;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.repository.QuyenTruyCapRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/admin/shipper")
public class ShipperController {

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private QuyenTruyCapRepository quyenTruyCapRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@ModelAttribute
	public void addUserToModel(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof NguoiDungDetails) {
			NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
			model.addAttribute("user", userDetails);
		}
	}

//	@GetMapping
//	public String listShippers(Model model) {
//		List<NguoiDung> shippers = nguoiDungService.findByRole("SHIPPER").stream().filter(s -> s.isTrangThai())
//				.sorted(Comparator.comparingInt(NguoiDung::getMaNguoiDung).reversed()).collect(Collectors.toList());
//
//		model.addAttribute("shippers", shippers);
//		return "admin/shipper/index";
//	}
	@GetMapping
	public String listShippers(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
		List<NguoiDung> shippers = nguoiDungService.findByRole("SHIPPER").stream()
			.filter(NguoiDung::isTrangThai)
			.filter(s -> keyword == null || keyword.isBlank() || s.getHoTen().toLowerCase().contains(keyword.toLowerCase()))
			.sorted(Comparator.comparingInt(NguoiDung::getMaNguoiDung).reversed())
			.collect(Collectors.toList());

		model.addAttribute("shippers", shippers);
		model.addAttribute("keyword", keyword); // để giữ lại giá trị trong ô input tìm kiếm nếu cần
		return "admin/shipper/index";
	}


	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("nguoiDung", new NguoiDung());
		return "admin/shipper/add"; // Giao diện thêm shipper
	}


	@PostMapping("/add")
	public String addShipper(@ModelAttribute("nguoiDung") NguoiDung nguoiDung, RedirectAttributes redirectAttributes) {
		// Kiểm tra nếu tên đã tồn tại
		
		if (!nguoiDung.getTenNguoiDung().matches("^[a-zA-Z0-9._-]{4,30}$")) {
            redirectAttributes.addFlashAttribute("error", "Tên người dùng không hợp lệ! Chỉ dùng chữ không dấu, số, và không có khoảng trắng.");
            return "redirect:/admin/shipper/add";
        }
		
		if (nguoiDungService.existsByTenNguoiDung(nguoiDung.getTenNguoiDung())) {
			redirectAttributes.addFlashAttribute("error", "Tên người dùng đã được sử dụng!");
			return "redirect:/admin/shipper/add";
		}
		// Kiểm tra nếu email đã tồn tại
		if (nguoiDungService.existsByEmail(nguoiDung.getEmail())) {
			redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
			return "redirect:/admin/shipper/add";
		}

		// Kiểm tra nếu số điện thoại đã tồn tại
		if (nguoiDungService.existsBySoDienThoai(nguoiDung.getSoDienThoai())) {
			redirectAttributes.addFlashAttribute("error", "Số điện thoại đã được sử dụng!");
			return "redirect:/admin/shipper/add";
		}

		// Gán quyền SHIPPER
		QuyenTruyCap quyenShipper = quyenTruyCapRepository.findByTenQuyen("SHIPPER");
		nguoiDung.setQuyenTruyCap(quyenShipper);

		// Mã hóa mật khẩu trước khi lưu
		nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));

		// Lưu vào DB
		nguoiDungService.save(nguoiDung);
		redirectAttributes.addFlashAttribute("success", "Thêm shipper thành công!");

		return "redirect:/admin/shipper";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Integer id, Model model) {
		NguoiDung shipper = nguoiDungService.findById(id);
		model.addAttribute("nguoiDung", shipper);
		return "admin/shipper/edit"; // Giao diện sửa shipper
	}

	@PostMapping("/edit")
	public String updateShipper(@ModelAttribute("nguoiDung") NguoiDung updatedShipper,
			RedirectAttributes redirectAttributes) {
		// Tìm Shipper hiện tại trong database
		NguoiDung existingShipper = nguoiDungService.findById(updatedShipper.getMaNguoiDung());

		if (existingShipper == null) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy shipper cần cập nhật!");
			return "redirect:/admin/shipper";
		}
		// Kiểm tra nếu tên đã tồn tại ở một Shipper khác
		if (nguoiDungService.existsByTenNguoiDungAndNotId(updatedShipper.getTenNguoiDung(),
				updatedShipper.getMaNguoiDung())) {
			redirectAttributes.addFlashAttribute("error", "Tên người dùng đã được sử dụng!");
			return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();
		}

		// Kiểm tra nếu email đã tồn tại ở một Shipper khác
		if (nguoiDungService.existsByEmailAndNotId(updatedShipper.getEmail(), updatedShipper.getMaNguoiDung())) {
			System.out.println("❌ Lỗi: Email đã tồn tại!");
			redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
			return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();
		}

		// Kiểm tra nếu số điện thoại đã tồn tại ở một Shipper khác
		if (nguoiDungService.existsBySoDienThoaiAndNotId(updatedShipper.getSoDienThoai(),
				updatedShipper.getMaNguoiDung())) {
			System.out.println("❌ Lỗi: Số điện thoại đã tồn tại!");
			redirectAttributes.addFlashAttribute("error", "Số điện thoại đã được sử dụng!");
			return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();
		}

		// Cập nhật thông tin Shipper
		existingShipper.setTenNguoiDung(updatedShipper.getTenNguoiDung());
		existingShipper.setEmail(updatedShipper.getEmail());
		existingShipper.setSoDienThoai(updatedShipper.getSoDienThoai());

		// Chỉ mã hóa mật khẩu nếu admin nhập mật khẩu mới
		if (updatedShipper.getMatKhau() != null && !updatedShipper.getMatKhau().isEmpty()) {
			existingShipper.setMatKhau(passwordEncoder.encode(updatedShipper.getMatKhau()));
		}

		// Lưu vào DB
		nguoiDungService.save(existingShipper);
		redirectAttributes.addFlashAttribute("success", "Cập nhật shipper thành công!");

		return "redirect:/admin/shipper/edit/" + updatedShipper.getMaNguoiDung();

	}

	// 4️ Xóa hoặc vô hiệu hóa Shipper
	// 6️⃣ Vô hiệu hóa (ẩn) shipper thay vì xóa
	@GetMapping("/delete/{id}")
	public String disableShipper(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		NguoiDung shipper = nguoiDungService.findById(id);
		if (shipper != null) {
			shipper.setTrangThai(false); // Đặt trạng thái thành false
			nguoiDungService.save(shipper); // Lưu lại vào DB
			redirectAttributes.addFlashAttribute("success", "Đã vô hiệu hóa shipper thành công.");
		} else {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy shipper để vô hiệu hóa.");
		}
		return "redirect:/admin/shipper";
	}

}
