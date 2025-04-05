package com.kimngan.ComesticAdmin.controller.seller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@RequestMapping("/seller/messages")
public class TinNhanSellerController {

	@Autowired
	private TinNhanService tinNhanService;

	@Autowired
	private NguoiDungService nguoiDungService;
	@Autowired
	private StorageService storageService;
	
	
	@GetMapping("/mark-read/{customerId}")
	@ResponseBody
	public void markMessagesAsRead(@PathVariable("customerId") Integer customerId, Principal principal) {
	    NguoiDung seller = nguoiDungService.findByTenNguoiDung(principal.getName());
	    tinNhanService.danhDauDaDocTheoCuocHoiThoai(customerId, seller.getMaNguoiDung());
	}


	@GetMapping("/count-unread")
	@ResponseBody
	public Long countUnreadMessages(Principal principal) {
	    if (principal != null) {
	        NguoiDung seller = nguoiDungService.findByTenNguoiDung(principal.getName());
	        return tinNhanService.demSoKhachHangChuaDoc(seller.getMaNguoiDung());
	    }
	    return 0L;
	}

	

	@GetMapping("/chat-list")
	public String showChatList(Model model, Principal principal) {
	    NguoiDung seller = nguoiDungService.findByTenNguoiDung(principal.getName());
	    //List<NguoiDung> customers = tinNhanService.findAllCustomersWhoChattedWithSeller(seller.getMaNguoiDung());
	    List<NguoiDung> customers = tinNhanService.findAllCustomersWhoChattedWithAnySeller();

	    // Tạo map lưu trạng thái chưa đọc
	    Map<Integer, Boolean> chuaDocMap = new HashMap<>();
	    for (NguoiDung customer : customers) {
	        long soTin = tinNhanService.demTinChuaDocGiua(customer.getMaNguoiDung(), seller.getMaNguoiDung());
	        chuaDocMap.put(customer.getMaNguoiDung(), soTin > 0);
	    }

	    model.addAttribute("customers", customers);
	    model.addAttribute("chuaDocMap", chuaDocMap); 
	    return "seller/fragment/popup-chat :: chatList";
	}



	@GetMapping("/{customerId}")
	public String openChatWithCustomer(@PathVariable("customerId") Integer customerId, Model model, Principal principal) {
	    if (principal == null) return "redirect:/seller/login";

	    NguoiDung seller = nguoiDungService.findByTenNguoiDung(principal.getName());
	    NguoiDung customer = nguoiDungService.findById(customerId);

	    if (customer == null || seller.getMaNguoiDung().equals(customer.getMaNguoiDung())) {
	        return "redirect:/seller/orders";
	    }

//	    List<TinNhan> ds = tinNhanService.getTinNhanGiuaHaiNguoi(seller.getMaNguoiDung(), customer.getMaNguoiDung());
	    List<TinNhan> ds = tinNhanService.getTinNhanGiuaKhachVaTatCaNhanVien(customer.getMaNguoiDung());
   
	    Long soTinChuaDoc = tinNhanService.demTinChuaDoc(seller.getMaNguoiDung());
	    model.addAttribute("soTinChuaDoc", soTinChuaDoc);

	    model.addAttribute("nguoiNhan", customer);
	    model.addAttribute("danhSachTinNhan", ds);
	    model.addAttribute("tinNhanMoi", new TinNhan());
	    model.addAttribute("currentUserId", seller.getMaNguoiDung());
	    return "seller/chat/view";
	}


	@PostMapping("/send")
	public String guiTinNhan(
	    @RequestParam("nguoiNhanId") Integer nguoiNhanId,
	    @RequestParam(value = "noiDung", required = false) String noiDung,
	    @RequestParam(value = "hinhAnh", required = false) MultipartFile[] hinhAnh,
	    Principal principal,
	    RedirectAttributes redirectAttributes
	) {
	    if (principal == null) {
	        return "redirect:/seller/login";
	    }

	    NguoiDung sender = nguoiDungService.findByTenNguoiDung(principal.getName());
	    NguoiDung receiver = nguoiDungService.findById(nguoiNhanId);

	    // Gửi nội dung
	    if (noiDung != null && !noiDung.trim().isEmpty()) {
	        TinNhan tinNhan = new TinNhan();
	        tinNhan.setNoiDung(noiDung);
	        tinNhan.setNguoiGui(sender);
	        tinNhan.setNguoiNhan(receiver);
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

	    redirectAttributes.addFlashAttribute("successMessage", "Tin nhắn đã được gửi!");
	    return "redirect:/seller/messages/" + nguoiNhanId;
	}




}