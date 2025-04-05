package com.kimngan.ComesticAdmin.controller.customer;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.NguoiDung;
import com.kimngan.ComesticAdmin.entity.TinNhan;
import com.kimngan.ComesticAdmin.services.NguoiDungService;
import com.kimngan.ComesticAdmin.services.StorageService;
import com.kimngan.ComesticAdmin.services.TinNhanService;

@Controller
@RequestMapping("/customer/messages")
public class TinNhanController {

	@Autowired
	private TinNhanService tinNhanService;

	@Autowired
	private NguoiDungService nguoiDungService;
	
	@Autowired
	private StorageService storageService;
	
	
//	@GetMapping("/mark-read/{senderId}")
//	@ResponseBody
//	public void markMessagesAsRead(@PathVariable("senderId") Integer senderId, Principal principal) {
//	    NguoiDung customer = nguoiDungService.findByTenNguoiDung(principal.getName());
//	    tinNhanService.danhDauDaDocTheoCuocHoiThoai(senderId, customer.getMaNguoiDung());
//	}
	
	@GetMapping("/mark-read")
	@ResponseBody
	public void markAllFromSellersAsRead(Principal principal) {
	    if (principal == null) return;
	    NguoiDung customer = nguoiDungService.findByTenNguoiDung(principal.getName());
	    tinNhanService.danhDauDaDocTuTatCaNhanVienChoKhach(customer.getMaNguoiDung());
	}

	
	@GetMapping("/count-unread")
	@ResponseBody
	public Long countUnreadMessages(Principal principal) {
	    if (principal != null) {
	        NguoiDung customer = nguoiDungService.findByTenNguoiDung(principal.getName());
	        return tinNhanService.demTinChuaDoc(customer.getMaNguoiDung());
	    }
	    return 0L;
	}


	@GetMapping("/{sellerId}")
	public String openChatWithSeller(@PathVariable("sellerId") Integer sellerId, Model model, Principal principal) {
		if (principal == null)
			return "redirect:/customer/login";

		NguoiDung customer = nguoiDungService.findByTenNguoiDung(principal.getName());
		NguoiDung seller = nguoiDungService.findById(sellerId);

		if (seller == null || customer.getMaNguoiDung().equals(seller.getMaNguoiDung())) {
			return "redirect:/";
		}


		//List<TinNhan> ds = tinNhanService.getTinNhanGiuaHaiNguoi(customer.getMaNguoiDung(), seller.getMaNguoiDung());
		List<TinNhan> ds = tinNhanService.getTatCaTinNhanVoiKhach(customer.getMaNguoiDung());

		Long soTinChuaDoc = tinNhanService.demTinChuaDoc(customer.getMaNguoiDung());
		model.addAttribute("soTinChuaDoc", soTinChuaDoc);
		System.out.println("Số tin chưa đọc: " + soTinChuaDoc);

		model.addAttribute("nguoiNhan", seller);
		model.addAttribute("danhSachTinNhan", ds);
		model.addAttribute("tinNhanMoi", new TinNhan());
		model.addAttribute("currentUserId", customer.getMaNguoiDung());
		return "chat/view";
	}


	@PostMapping("/send")
	public String guiTinNhan(
	    @RequestParam(value = "noiDung", required = false) String noiDung,
	    @RequestParam(value = "hinhAnh", required = false) MultipartFile[] hinhAnh,
	    Principal principal,
	    RedirectAttributes redirectAttributes
	) {
	    if (principal == null) {
	        return "redirect:/customer/login";
	    }

	    NguoiDung sender = nguoiDungService.findByTenNguoiDung(principal.getName());
	    List<NguoiDung> nhanViens = nguoiDungService.findByRole(5); // role 5 = nhân viên bán hàng

	    if (nhanViens.isEmpty()) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Không có nhân viên bán hàng nào để gửi tin.");
	        return "redirect:/";
	    }

	    NguoiDung receiver = nhanViens.get(0); // gửi mặc định cho nhân viên đầu

	    // Gửi nội dung
	    if (noiDung != null && !noiDung.trim().isEmpty()) {
	        TinNhan tinNhan = new TinNhan();
	        tinNhan.setNguoiGui(sender);
	        tinNhan.setNguoiNhan(receiver);
	        tinNhan.setNoiDung(noiDung);
	        tinNhan.setThoiGianGui(LocalDateTime.now());
	        tinNhanService.guiTinNhan(tinNhan);
	    }

	    // Gửi ảnh
	    if (hinhAnh != null && hinhAnh.length > 0) {
	        for (MultipartFile file : hinhAnh) {
	            if (!file.isEmpty()) {
	                try {
	                    String fileName = storageService.storeFile(file);
	                    TinNhan tinAnh = new TinNhan();
	                    tinAnh.setHinhAnh(fileName);
	                    tinAnh.setNguoiGui(sender);
	                    tinAnh.setNguoiNhan(receiver);
	                    tinAnh.setThoiGianGui(LocalDateTime.now());
	                    tinNhanService.guiTinNhan(tinAnh);
	                } catch (IOException e) {
	                    e.printStackTrace();
	                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu ảnh: " + file.getOriginalFilename());
	                }
	            }
	        }
	    }

	    redirectAttributes.addFlashAttribute("successMessage", "📨 Tin nhắn đã được gửi!");
	    return "redirect:/customer/messages/" + receiver.getMaNguoiDung();
	}


}
