package com.kimngan.ComesticAdmin.controller.customer;

import java.io.IOException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.kimngan.ComesticAdmin.services.StorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.QuyenTruyCap;
import com.kimngan.ComesticAdmin.repository.QuyenTruyCapRepository;
import com.kimngan.ComesticAdmin.services.NguoiDungService;

@Controller
@RequestMapping("/customer") // Thêm @RequestMapping để tất cả các phương thức đều có tiền tố "/customer"
public class CustomerUserController {

	@Autowired
	private NguoiDungService nguoiDungService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private QuyenTruyCapRepository quyenTruyCapRepository;
	@Autowired
	private StorageService storageService;

	// Phương thức hiển thị trang đăng ký
	@GetMapping("/register")
	public String showRegistrationForm(@RequestParam(value = "success", required = false) Boolean success,
			Model model) {
		model.addAttribute("nguoiDung", new NguoiDung());
		if (Boolean.TRUE.equals(success)) {
			model.addAttribute("successMessage", "🎉 Đăng ký thành công! Đang chuyển đến trang đăng nhập...");
		}

		return "customer/register"; // Trả về tên của file view để hiển thị form đăng ký
	}

	// Phương thức xử lý đăng ký
	@PostMapping("/register")
	public String registerCustomer(@ModelAttribute("nguoiDung") NguoiDung nguoiDung, Model model,
			@RequestParam("avatarFile") MultipartFile avatarFile) {

		// Kiểm tra tên đăng nhập trùng
		if (nguoiDungService.existsByTenNguoiDung(nguoiDung.getTenNguoiDung())) {
			model.addAttribute("error", "⚠️ Tên đăng nhập đã tồn tại.");
			return "customer/register";
		}

		// Kiểm tra email trùng
		if (nguoiDungService.existsByEmail(nguoiDung.getEmail())) {
			model.addAttribute("error", "⚠️ Email đã được sử dụng.");

			return "customer/register";
		}

		// Kiểm tra số điện thoại trùng
		if (nguoiDungService.existsBySoDienThoai(nguoiDung.getSoDienThoai())) {
			model.addAttribute("error", "⚠️ Số điện thoại đã được đăng ký.");
			return "customer/register";
		}

		// Mã hóa mật khẩu trước khi lưu
		nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));

		// Lấy quyền CUSTOMER từ cơ sở dữ liệu
		QuyenTruyCap quyenCustomer = quyenTruyCapRepository.findByTenQuyen("CUSTOMER");

		// Gán quyền CUSTOMER cho người dùng
		nguoiDung.setQuyenTruyCap(quyenCustomer);

		// Xử lý lưu ảnh đại diện
		if (avatarFile != null && !avatarFile.isEmpty()) {
			try {
				String fileName = storageService.storeFile(avatarFile);
				nguoiDung.setAvatar(fileName);
			} catch (IOException e) {
				e.printStackTrace(); // Ghi log lỗi
				nguoiDung.setAvatar("default-avatar.jpg"); // fallback ảnh mặc định
			}
		} else {
			nguoiDung.setAvatar("default-avatar.jpg");
		}

		// Lưu người dùng vào cơ sở dữ liệu
		nguoiDungService.saveCustomer(nguoiDung);

		// Chuyển hướng về trang đăng nhập sau khi đăng ký thành công
		return "redirect:/customer/register?success=true";

	}

	// Phương thức hiển thị trang đăng nhập
	@GetMapping("/login")
	public String showLoginForm() {
		return "customer/login";
	}

	// Thêm thông tin người dùng hiện tại vào model
	@ModelAttribute("currentUser")
	public NguoiDung getCurrentUser(Principal principal) {
		if (principal != null) {
			return nguoiDungService.findByTenNguoiDung(principal.getName());
		}
		return null;
	}

	// Phương thức điều hướng sau khi đăng xuất
	@GetMapping("/logout")
	public String logout() {
		return "redirect:/"; // Điều hướng về trang chủ sau khi đăng xuất
	}

	// **Phương thức hiển thị thông tin tài khoản**
	@GetMapping("/account")
	public String showAccountPage(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/customer/login";
		}

		// Lấy thông tin người dùng hiện tại
		NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
		model.addAttribute("nguoiDung", currentUser);
		return "customer/account"; // Hiển thị file account.html
	}

	@PostMapping("/account")
	public String updateAccount(@ModelAttribute("nguoiDung") NguoiDung updatedUser,
			@RequestParam("avatarFile") MultipartFile avatarFile, Principal principal,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			System.out.println("Principal is null, user is not logged in.");

			return "redirect:/customer/login";
		}

		try {
			// Lấy thông tin người dùng hiện tại
			NguoiDung currentUser = nguoiDungService.findByTenNguoiDung(principal.getName());
			if (currentUser == null) {
				redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
				return "redirect:/customer/account";
			}
			// Cập nhật thông tin cơ bản
			currentUser.setHoTen(updatedUser.getHoTen());
			currentUser.setEmail(updatedUser.getEmail());
			currentUser.setSoDienThoai(updatedUser.getSoDienThoai());
			currentUser.setDiaChi(updatedUser.getDiaChi());

			// Xử lý lưu ảnh đại diện (nếu có tải lên)
			if (avatarFile != null && !avatarFile.isEmpty()) {
				try {
					String fileName = storageService.storeFile(avatarFile); // Lưu ảnh
					currentUser.setAvatar(fileName); // Cập nhật avatar mới
				} catch (IOException e) {
					redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tải ảnh đại diện.");
					return "redirect:/customer/account";
				}
			} else if (currentUser.getAvatar() == null || currentUser.getAvatar().isEmpty()) {
				// Nếu người dùng chưa có avatar, gán ảnh mặc định
				currentUser.setAvatar("aa.jpg");
			}

			// Lưu thông tin người dùng
			nguoiDungService.save(currentUser);
			// Gọi phương thức để cập nhật SecurityContext
			updateSecurityContext(currentUser);

			// Thông báo thành công
			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật thông tin.");
		}

		return "redirect:/customer/account";
	}

	// Phương thức cập nhật SecurityContext
	private void updateSecurityContext(NguoiDung updatedUser) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			// Sử dụng constructor hiện tại của NguoiDungDetails
			NguoiDungDetails userDetails = new NguoiDungDetails(updatedUser, authentication.getAuthorities());

			// Tạo một Authentication mới
			Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails,
					authentication.getCredentials(), authentication.getAuthorities());

			// Cập nhật SecurityContext với Authentication mới
			SecurityContextHolder.getContext().setAuthentication(newAuth);
		}
	}

}
