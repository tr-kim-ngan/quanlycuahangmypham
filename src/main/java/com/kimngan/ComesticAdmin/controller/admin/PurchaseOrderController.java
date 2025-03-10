package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHang;
import com.kimngan.ComesticAdmin.entity.ChiTietDonNhapHangId;
import com.kimngan.ComesticAdmin.entity.DonNhapHang;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.entity.NhaCungCap;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.services.ChiTietDonNhapHangService;
import com.kimngan.ComesticAdmin.services.DonNhapHangService;
import com.kimngan.ComesticAdmin.services.NhaCungCapService;
import com.kimngan.ComesticAdmin.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.InputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class PurchaseOrderController {

	@Autowired
	private DonNhapHangService donNhapHangService;

	@Autowired
	private ChiTietDonNhapHangService chiTietDonNhapHangService;

	@Autowired
	private SanPhamService sanPhamService;

	@Autowired
	private NhaCungCapService nhaCungCapService;

	
	

	// Hiển thị danh sách đơn nhập hàng
	@GetMapping("/purchaseorder")
	public String index(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "keyword", required = false) String keyword) {

		// Tạo đối tượng PageRequest để phân trang
		Page<DonNhapHang> pageDonNhapHang;

		if (keyword != null && !keyword.isEmpty()) {
			// Tìm kiếm theo nhà cung cấp và sắp xếp giảm dần theo ngày nhập
			pageDonNhapHang = donNhapHangService.findByNhaCungCap_Ten(keyword,
					PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonNhapHang")));
			model.addAttribute("keyword", keyword);
		} else {
			// Hiển thị tất cả đơn nhập hàng, sắp xếp giảm dần theo ngày nhập
			pageDonNhapHang = donNhapHangService
					.findAllActive(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maDonNhapHang")));
		}
		// Định dạng tổng giá trị đơn nhập hàng
		Map<Integer, String> formattedTotalValues = new HashMap<>();
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		// Định dạng ngày nhập hàng
		Map<Integer, String> formattedNgayNhapHangValues = new HashMap<>();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		for (DonNhapHang donNhapHang : pageDonNhapHang.getContent()) {
			BigDecimal tongGiaTri = donNhapHang.getTongGiaTriNhapHang();
			String formattedValue = tongGiaTri != null ? decimalFormat.format(tongGiaTri) + " VND" : "0.00 VND";
			formattedTotalValues.put(donNhapHang.getMaDonNhapHang(), formattedValue);

			// Định dạng ngày nhập hàng
			LocalDate ngayNhapHang = donNhapHang.getNgayNhapHang();
			String formattedNgayNhap = ngayNhapHang != null ? ngayNhapHang.format(dateFormatter) : "N/A";
			formattedNgayNhapHangValues.put(donNhapHang.getMaDonNhapHang(), formattedNgayNhap);

		}

		model.addAttribute("formattedTotalValues", formattedTotalValues);
		model.addAttribute("formattedNgayNhapHangValues", formattedNgayNhapHangValues);

		model.addAttribute("listDonNhapHang", pageDonNhapHang.getContent());
		model.addAttribute("currentPage", pageDonNhapHang.getNumber());
		model.addAttribute("totalPages", pageDonNhapHang.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/admin/purchaseorder");

		// Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
		return "admin/purchaseorder/index";
	}

	// Trang thêm đơn nhập hàng
	@GetMapping("/add-purchase-order")
	public String showAddPurchaseOrderPage(Model model) {
		model.addAttribute("providers", nhaCungCapService.findAllActive(PageRequest.of(0, 10)).getContent());

		// Thêm thông tin người dùng
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
		return "admin/purchaseorder/add"; // Trả về trang tạo đơn nhập hàng
	}

	// Lưu đơn nhập hàng mới (không có chi tiết)
	@PostMapping("/save-purchase-order")
	public String createPurchaseOrder(
			@RequestParam("ngayNhapHang") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate ngayNhapHang,
			@RequestParam("maNhaCungCap") Integer maNhaCungCap, Model model, RedirectAttributes redirectAttributes) {
		 // Kiểm tra nếu ngày nhập hàng lớn hơn ngày hiện tại
	    LocalDate today = LocalDate.now();
	    if (ngayNhapHang.isAfter(today)) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Ngày nhập hàng không được lớn hơn ngày hôm nay.");
	        return "redirect:/admin/add-purchase-order";
	    }
		// Kiểm tra nhà cung cấp
		NhaCungCap nhaCungCap = nhaCungCapService.findById(maNhaCungCap);
		if (nhaCungCap == null) {
			model.addAttribute("errorMessage", "Nhà cung cấp không tồn tại.");
			return "admin/purchaseorder/add";
		}

		// Tạo mới đơn nhập hàng với tổng giá trị ban đầu là 0
		DonNhapHang donNhapHang = new DonNhapHang();
		donNhapHang.setNgayNhapHang(ngayNhapHang);
		donNhapHang.setNhaCungCap(nhaCungCap);
		donNhapHang.setTongGiaTriNhapHang(BigDecimal.ZERO); // Tổng giá trị ban đầu là 0
		donNhapHang.setTrangThai(true); // Đơn nhập hàng đang hoạt động

		donNhapHangService.create(donNhapHang);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Điều hướng đến trang thêm chi tiết đơn nhập hàng

		return "redirect:/admin/add-purchase-order-detail/" + donNhapHang.getMaDonNhapHang();
		// nó sẽ logic chuyển đến url admin/add-purchase-order-detail chứ không phải
		// file html

	}

	// Trang thêm chi tiết đơn nhập hàng
	@GetMapping("/add-purchase-order-detail/{maDonNhapHang}")
	public String showAddPurchaseOrderDetailsPage(@PathVariable("maDonNhapHang") Integer maDonNhapHang, Model model) {
		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		if (donNhapHang == null) {
			model.addAttribute("errorMessage", "Không tìm thấy đơn nhập hàng.");
			return "redirect:/admin/purchaseorder";
		}


	    // Lấy danh sách sản phẩm còn hoạt động
	    List<SanPham> sanPhams = sanPhamService.findByTrangThai(true);

	    // Định dạng ngày
	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	    String formattedNgayNhapHang = donNhapHang.getNgayNhapHang().format(dateFormatter);

	    // Định dạng số tiền
	    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

	    // Định dạng giá trị trong chi tiết đơn nhập hàng và tính tổng giá trị
	    List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
	    Map<Integer, String> formattedDonGiaNhapMap = new HashMap<>();
	    BigDecimal totalValue = BigDecimal.ZERO;

	    for (ChiTietDonNhapHang chiTiet : chiTietList) {
	        BigDecimal value = chiTiet.getDonGiaNhap().multiply(BigDecimal.valueOf(chiTiet.getSoLuongNhap()));
	        totalValue = totalValue.add(value);

	        String formattedValue = decimalFormat.format(chiTiet.getDonGiaNhap()) + " VND";
	        formattedDonGiaNhapMap.put(chiTiet.getId().hashCode(), formattedValue);
	    }

	    // Tổng giá trị nhập đã định dạng
	    String formattedTotalValue = decimalFormat.format(totalValue) + " VND";
	    
	    
	    
	    model.addAttribute("sanPhams", sanPhams);
	    model.addAttribute("donNhapHang", donNhapHang);
	    model.addAttribute("chiTietList", chiTietList);
	    model.addAttribute("formattedNgayNhapHang", formattedNgayNhapHang);
	    model.addAttribute("formattedDonGiaNhapMap", formattedDonGiaNhapMap);
	    model.addAttribute("formattedTotalValue", formattedTotalValue);
	    
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);
		return "admin/purchaseorder/add-details";
	}

	@PostMapping("/save-purchase-order-details/{maDonNhapHang}")
	public String savePurchaseOrderDetails(@PathVariable("maDonNhapHang") Integer maDonNhapHang,
			@RequestParam("sanPhamIds") List<Integer> sanPhamIds,
			@RequestParam("soLuongNhap") List<Integer> soLuongNhap,
			@RequestParam("donGiaNhap") List<BigDecimal> donGiaNhap, Model model) {

		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		if (donNhapHang == null) {
			model.addAttribute("errorMessage", "Không tìm thấy đơn nhập hàng.");
			return "redirect:/admin/purchaseorder";
		}
		BigDecimal tongGiaTriNhap = donNhapHang.getTongGiaTriNhapHang();
		// Lưu từng chi tiết đơn nhập hàng
		for (int i = 0; i < sanPhamIds.size(); i++) {
			if (soLuongNhap.get(i) == null || donGiaNhap.get(i) == null) {
				model.addAttribute("errorMessage", "Số lượng và đơn giá không được để trống.");
				return "redirect:/admin/add-purchase-order-detail/" + maDonNhapHang;
			}

			ChiTietDonNhapHang chiTiet = new ChiTietDonNhapHang();
			chiTiet.setDonNhapHang(donNhapHang);
			chiTiet.setSanPham(sanPhamService.findById(sanPhamIds.get(i)));
			chiTiet.setSoLuongNhap(soLuongNhap.get(i));
			chiTiet.setDonGiaNhap(donGiaNhap.get(i));

			BigDecimal giaTriNhap = donGiaNhap.get(i).multiply(new BigDecimal(soLuongNhap.get(i)));
			tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);

			chiTietDonNhapHangService.create(chiTiet);
		}

		// Cập nhật tổng giá trị nhập hàng trong đơn nhập hàng
		donNhapHang.setTongGiaTriNhapHang(tongGiaTriNhap);
		donNhapHangService.update(donNhapHang);
		// Đưa lại danh sách chi tiết đơn hàng đã thêm
		List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
		model.addAttribute("chiTietList", chiTietList);

		// Đưa lại danh sách sản phẩm để có thể tiếp tục thêm
		model.addAttribute("donNhapHang", donNhapHang);
		model.addAttribute("products", sanPhamService.getAll()); // Lấy danh sách sản phẩm

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Trả về lại trang thêm chi tiết để có thể tiếp tục thêm sản phẩm khác
		model.addAttribute("donNhapHang", donNhapHang);
		model.addAttribute("products", sanPhamService.getAll()); // Lấy danh sách sản phẩm
		model.addAttribute("chiTietList", chiTietList);

		return "admin/purchaseorder/add-details"; // Trả về trang thêm chi tiết }
	}



	@GetMapping("/edit/{id}")
	public String viewPurchaseOrderDetails(@PathVariable("id") Integer maDonNhapHang,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "3") int size, Model model) {
		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);

		// Phân trang cho chi tiết đơn nhập hàng
		Page<ChiTietDonNhapHang> chiTietPage = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang,
				PageRequest.of(page, size));

		// Lấy toàn bộ danh sách chi tiết để tính tổng giá trị
		List<ChiTietDonNhapHang> allChiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);

		// Lấy danh sách sản phẩm đang hoạt động
		List<SanPham> activeSanPhams = sanPhamService.findByTrangThai(true);

		// Định dạng ngày nhập hàng
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String formattedNgayNhap = donNhapHang.getNgayNhapHang().format(formatter);

		// Định dạng đơn giá và tổng giá tiền cho từng chi tiết
		Map<Integer, String> formattedChiTietValues = new HashMap<>();
		Map<Integer, String> formattedTotalPrices = new HashMap<>();
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		BigDecimal totalOrderPrice = BigDecimal.ZERO;

		for (ChiTietDonNhapHang chiTiet : allChiTietList) {
			BigDecimal donGiaNhap = chiTiet.getDonGiaNhap();
			int soLuongNhap = chiTiet.getSoLuongNhap();
			BigDecimal totalPrice = donGiaNhap.multiply(new BigDecimal(soLuongNhap));

			if (chiTietPage.getContent().contains(chiTiet)) {
				String formattedValue = decimalFormat.format(donGiaNhap) + " VND";
				String formattedTotalPrice = decimalFormat.format(totalPrice) + " VND";
				formattedChiTietValues.put(chiTiet.getSanPham().getMaSanPham(), formattedValue);
				formattedTotalPrices.put(chiTiet.getSanPham().getMaSanPham(), formattedTotalPrice);
			}

			totalOrderPrice = totalOrderPrice.add(totalPrice);
		}

		String formattedTotalOrderPrice = decimalFormat.format(totalOrderPrice) + " VND";

		// Gửi dữ liệu xuống view
		model.addAttribute("formattedChiTietValues", formattedChiTietValues);
		model.addAttribute("formattedTotalPrices", formattedTotalPrices);
		model.addAttribute("formattedTotalOrderPrice", formattedTotalOrderPrice);
		model.addAttribute("formattedNgayNhap", formattedNgayNhap);
		model.addAttribute("donNhapHang", donNhapHang);
		model.addAttribute("chiTietPage", chiTietPage);
		model.addAttribute("sanPhams", activeSanPhams);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/purchaseorder/edit";
	}

//Lưu  chi tiết đơn nhập hàng vừa sửa
	@PostMapping("/update-purchase-order-detail/{maDonNhapHang}")
	public String updatePurchaseOrderDetails(@PathVariable("maDonNhapHang") Integer maDonNhapHang,
			@RequestParam("sanPhamIds") List<Integer> sanPhamIds,
			@RequestParam("soLuongNhap") List<Integer> soLuongNhap,
			@RequestParam("donGiaNhap") List<BigDecimal> donGiaNhap, Model model) {

		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		if (donNhapHang == null) {
			model.addAttribute("errorMessage", "Không tìm thấy đơn nhập hàng.");
			return "redirect:/admin/purchaseorder";
		}

		BigDecimal tongGiaTriNhap = BigDecimal.ZERO; // Tính tổng giá trị nhập

		// Lưu từng chi tiết đơn nhập hàng
		for (int i = 0; i < sanPhamIds.size(); i++) {
			// Tạo composite key và tìm chi tiết đơn nhập hàng
			ChiTietDonNhapHangId chiTietId = new ChiTietDonNhapHangId(maDonNhapHang, sanPhamIds.get(i));
			ChiTietDonNhapHang chiTiet = chiTietDonNhapHangService.findById(chiTietId);

			if (chiTiet != null) {
				// Cập nhật số lượng và đơn giá
				chiTiet.setSoLuongNhap(soLuongNhap.get(i));
				chiTiet.setDonGiaNhap(donGiaNhap.get(i));
				// Cập nhật chi tiết đơn nhập hàng
				chiTietDonNhapHangService.update(chiTiet);
			}
		}
		// Tính tổng giá trị nhập hàng sau khi cập nhật chi tiết
		// BigDecimal tongGiaTriNhap = BigDecimal.ZERO;
		List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
		for (ChiTietDonNhapHang ct : chiTietList) {
			BigDecimal giaTriNhap = ct.getDonGiaNhap().multiply(new BigDecimal(ct.getSoLuongNhap()));
			tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);
		}
		// Cập nhật tổng giá trị nhập hàng
		donNhapHang.setTongGiaTriNhapHang(tongGiaTriNhap);
		donNhapHangService.update(donNhapHang);

		return "redirect:/admin/purchaseorder";
	}

	@PostMapping("/add-purchase-order-detail/{maDonNhapHang}")
	public String addPurchaseOrderDetail(@PathVariable("maDonNhapHang") Integer maDonNhapHang,
			@RequestParam("sanPhamIds") Integer sanPhamIds, @RequestParam("soLuongNhap") Integer soLuongNhap,
			@RequestParam("donGiaNhap") BigDecimal donGiaNhap) {

		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		SanPham sanPham = sanPhamService.findById(sanPhamIds);

		ChiTietDonNhapHangId chiTietId = new ChiTietDonNhapHangId(maDonNhapHang, sanPhamIds);
		ChiTietDonNhapHang chiTiet = new ChiTietDonNhapHang();
		chiTiet.setId(chiTietId); // Thiết lập composite key
		chiTiet.setDonNhapHang(donNhapHang);
		chiTiet.setSanPham(sanPham);
		chiTiet.setSoLuongNhap(soLuongNhap);
		chiTiet.setDonGiaNhap(donGiaNhap);

		chiTietDonNhapHangService.create(chiTiet);
		// Tính tổng giá trị nhập hàng sau khi thêm chi tiết
		BigDecimal tongGiaTriNhap = BigDecimal.ZERO;
		List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
		for (ChiTietDonNhapHang ct : chiTietList) {
			BigDecimal giaTriNhap = ct.getDonGiaNhap().multiply(new BigDecimal(ct.getSoLuongNhap()));
			tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);
		}

		// Cập nhật tổng giá trị nhập hàng trong đơn nhập hàng
		donNhapHang.setTongGiaTriNhapHang(tongGiaTriNhap);
		donNhapHangService.update(donNhapHang);

		// Điều hướng về trang chi tiết đơn nhập hàng
		return "redirect:/admin/add-purchase-order-detail/" + maDonNhapHang;
	}

	@PostMapping("/update-detail")
	public String updatePurchaseOrderDetail(@RequestParam("maDonNhapHang") Integer maDonNhapHang,
			@RequestParam("maSanPham") Integer maSanPham, @RequestParam("soluongNhap") Integer soluongNhap,
			@RequestParam("donGiaNhap") BigDecimal donGiaNhap, Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		// Tạo đối tượng composite key
		ChiTietDonNhapHangId chiTietId = new ChiTietDonNhapHangId(maDonNhapHang, maSanPham);

		// Lấy thông tin chi tiết đơn nhập
		ChiTietDonNhapHang chiTiet = chiTietDonNhapHangService.findById(chiTietId);

		if (chiTiet != null) {
			// Cập nhật thông tin chi tiết
			chiTiet.setSoLuongNhap(soluongNhap);
			chiTiet.setDonGiaNhap(donGiaNhap);
			chiTietDonNhapHangService.update(chiTiet);

			// Cập nhật lại tổng giá trị của đơn nhập hàng
			DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
			if (donNhapHang != null) {
				BigDecimal tongGiaTriNhap = BigDecimal.ZERO;
				List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);

				for (ChiTietDonNhapHang ct : chiTietList) {
					BigDecimal giaTriNhap = ct.getDonGiaNhap().multiply(new BigDecimal(ct.getSoLuongNhap()));
					tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);
				}

				donNhapHang.setTongGiaTriNhapHang(tongGiaTriNhap);
				donNhapHangService.update(donNhapHang);
			}
		} else {
			// Xử lý khi không tìm thấy chi tiết đơn nhập hàng
			model.addAttribute("errorMessage", "Không tìm thấy chi tiết đơn nhập hàng cần cập nhật.");
			return "redirect:/admin/purchaseorder"; // Điều hướng về trang danh sách đơn nhập hàng
		}

		// Điều hướng về trang chi tiết đơn nhập hàng sau khi cập nhật
		return "redirect:/admin/edit/" + maDonNhapHang;
	}

// ẩn các sản phẩm nếu xóa 	
	// Ẩn chi tiết đơn nhập hàng (không xóa mà chỉ cập nhật trạng thái)
	@GetMapping("/hide-purchase-order-detail/{maDonNhapHang}/{maSanPham}")
	public String hidePurchaseOrderDetail(@PathVariable("maDonNhapHang") Integer maDonNhapHang,
			@PathVariable("maSanPham") Integer maSanPham, Model model) {
		// Tạo composite key
		ChiTietDonNhapHangId chiTietId = new ChiTietDonNhapHangId(maDonNhapHang, maSanPham);

		// Tìm chi tiết đơn nhập hàng
		ChiTietDonNhapHang chiTiet = chiTietDonNhapHangService.findById(chiTietId);

		if (chiTiet != null) {
			// Ẩn chi tiết đơn nhập hàng bằng cách đặt trạng thái thành false
			// chiTiet.setTrangThai(false);
			chiTiet.setSoLuongNhap(0);
			chiTiet.setDonGiaNhap(BigDecimal.ZERO);
			chiTietDonNhapHangService.update(chiTiet);

			// Cập nhật lại tổng giá trị của đơn nhập hàng
			DonNhapHang donNhapHang = chiTiet.getDonNhapHang();
			BigDecimal tongGiaTriNhap = BigDecimal.ZERO;

			List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang).stream()
					.filter(ct -> ct.isTrangThai()) // Lọc những chi tiết còn hoạt động
					.collect(Collectors.toList());

			for (ChiTietDonNhapHang ct : chiTietList) {
				BigDecimal giaTriNhap = ct.getDonGiaNhap().multiply(new BigDecimal(ct.getSoLuongNhap()));
				tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);
			}

			donNhapHang.setTongGiaTriNhapHang(tongGiaTriNhap);
			donNhapHangService.update(donNhapHang);
			// }
		}
		return "redirect:/admin/add-purchase-order-detail/" + maDonNhapHang;
		// return "redirect:/admin/edit/" + maDonNhapHang;
	}

	@PostMapping("/delete-purchase-order/{id}")
	public String deletePurchaseOrder(@PathVariable("id") Integer maDonNhapHang,
			RedirectAttributes redirectAttributes) {
		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);

		// Kiểm tra xem đơn nhập hàng có chi tiết không và tổng giá trị nhập hàng lớn
		// hơn 0 không
		if (donNhapHang.getTongGiaTriNhapHang().compareTo(BigDecimal.ZERO) > 0) {
			// Nếu tổng giá trị lớn hơn 0, thêm thông báo lỗi và quay lại trang danh sách
			redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng đang có sản phẩm");
			return "redirect:/admin/purchaseorder"; // Quay lại trang danh sách đơn nhập hàng
		}

		// Nếu tổng giá trị bằng 0, đổi trạng thái đơn nhập hàng thành false (ẩn đơn
		// nhập hàng)
//		donNhapHang.setTrangThai(false);
//		donNhapHangService.update(donNhapHang);
		// Nếu tổng giá trị bằng 0, xóa đơn nhập hàng
		donNhapHangService.delete(maDonNhapHang);

		// Quay lại trang danh sách đơn nhập hàng với thông báo thành công (nếu cần)
		redirectAttributes.addFlashAttribute("successMessage", "Xóa đơn nhập hàng thành công.");
		return "redirect:/admin/purchaseorder"; // Quay lại trang danh sách đơn nhập hàng
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@GetMapping("/purchaseorder/export/{id}")
	public void exportToPDF(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
		response.setContentType("application/pdf");
		DonNhapHang donNhapHang = donNhapHangService.findById(id);
		// Định dạng ngày
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		String fileName = "DonNhap_" + donNhapHang.getMaDonNhapHang() + "_"
				+ donNhapHang.getNgayNhapHang().format(formatter) + ".pdf";
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		// Tạo tài liệu PDF
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		// Tải font Unicode
		InputStream fontStream = getClass().getResourceAsStream("/fonts/TIMES.TTF");
		PDFont font = PDType0Font.load(document, fontStream);

		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		// Tiêu đề chính giữa
		contentStream.beginText();
		contentStream.setFont(font, 16); // Font lớn hơn cho tiêu đề
		contentStream.newLineAtOffset(220, 750); // Căn giữa
		contentStream.showText("ĐƠN NHẬP HÀNG");
		contentStream.endText();

		// Hiển thị thông tin chi tiết bên trái
		float infoStartY = 700; // Điều chỉnh khoảng cách xuống dưới
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.newLineAtOffset(50, infoStartY);
		contentStream.setLeading(14.5f);

		// DonNhapHang donNhapHang = donNhapHangService.findById(id);
		// Định dạng ngày
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		contentStream.showText("Mã đơn nhập: " + donNhapHang.getMaDonNhapHang());
		contentStream.newLine();
		contentStream.showText("Ngày nhập: " + donNhapHang.getNgayNhapHang().format(formatter));
		contentStream.newLine();
		contentStream.endText();

		// Hiển thị thông tin nhà cung cấp bên phải
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.newLineAtOffset(270, infoStartY); // Căn lề phải
		contentStream.setLeading(14.5f);

		contentStream.showText("Nhà cung cấp: " + donNhapHang.getNhaCungCap().getTenNhaCungCap());
		contentStream.newLine();
		contentStream.showText("Email: " + donNhapHang.getNhaCungCap().getEmailNhaCungCap());
		contentStream.newLine();
		contentStream.endText();

		// Bắt đầu vẽ bảng
		float startX = 50; // Vị trí bắt đầu của bảng
		float startY = infoStartY - 30; // Vị trí dòng đầu tiên
		float[] columnWidths = { 100, 190, 65, 90, 100 }; // Điều chỉnh cột cho cân đối
		float cellHeight = 60; // Tăng chiều cao để hình ảnh không bị méo
		float headerCellHeight = 40;

		// Tiêu đề bảng
		String[] headers = { "HÌNH ẢNH", "SẢN PHẨM", "SỐ LƯỢNG NHẬP", "ĐƠN GIÁ NHẬP", "TỔNG GIÁ TIỀN" };
		drawRow(contentStream, font, startX, startY, headerCellHeight, columnWidths, headers, null, document);
		startY -= headerCellHeight; // Di chuyển xuống dòng tiếp theo

		// Lấy danh sách chi tiết đơn nhập hàng
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
		// Tính tổng giá trị đơn nhập hàng
		BigDecimal tongGiaTriNhap = chiTietList.stream()
				.map(chiTiet -> chiTiet.getDonGiaNhap().multiply(new BigDecimal(chiTiet.getSoLuongNhap())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		for (ChiTietDonNhapHang chiTiet : chiTietList) {
			String imagePath = "src/main/resources/static/upload/" + chiTiet.getSanPham().getHinhAnh();
			String[] data = { "", // Placeholder cho hình ảnh
					chiTiet.getSanPham().getTenSanPham(), String.valueOf(chiTiet.getSoLuongNhap()),
					decimalFormat.format(chiTiet.getDonGiaNhap()) + " VND",
					decimalFormat.format(chiTiet.getDonGiaNhap().multiply(new BigDecimal(chiTiet.getSoLuongNhap())))
							+ " VND" };
			drawRow(contentStream, font, startX, startY, cellHeight, columnWidths, data, imagePath, document);
			startY -= cellHeight;
		}

		// Hiển thị tổng tiền sau bảng, cách bảng 10px
		contentStream.beginText();
		contentStream.setFont(font, 12);

		// Tính toán vị trí hiển thị tổng tiền ở bên phải
		float totalTextX = startX + columnWidths[0] + columnWidths[1] + 10; // Tổng độ rộng cột
		contentStream.newLineAtOffset(totalTextX, startY - 20); // Vị trí ngoài bảng, bên phải
		contentStream.showText("Tổng tiền phải trả: " + decimalFormat.format(tongGiaTriNhap) + " VND");
		contentStream.endText();

		contentStream.close();

		// Lưu tài liệu
		document.save(response.getOutputStream());
		document.close();
	}

	private void drawRow(PDPageContentStream contentStream, PDFont font, float startX, float startY, float cellHeight,
			float[] columnWidths, String[] content, String imagePath, PDDocument document) throws IOException {
		float currentX = startX;

// Vẽ khung của dòng
		contentStream.setLineWidth(0.75f); // Đường viền rõ hơn
		for (float width : columnWidths) {
			contentStream.addRect(currentX, startY - cellHeight, width, cellHeight);
			currentX += width;
		}
		contentStream.stroke();

		currentX = startX;

		for (int i = 0; i < content.length; i++) {
			if (i == 0 && imagePath != null) {
				// Nếu là cột hình ảnh, vẽ hình
				PDImageXObject image = PDImageXObject.createFromFile(imagePath, document);
				float imageWidth = columnWidths[i] - 10;
				float aspectRatio = (float) image.getHeight() / image.getWidth();
				float imageHeight = imageWidth * aspectRatio;
				if (imageHeight > cellHeight - 10) {
					imageHeight = cellHeight - 10;
					imageWidth = imageHeight / aspectRatio;
				}
				float imageX = currentX + (columnWidths[i] - imageWidth) / 2;
				float imageY = startY - cellHeight + (cellHeight - imageHeight) / 2;
				contentStream.drawImage(image, imageX, imageY, imageWidth, imageHeight);
			} else {
				// Nếu là nội dung văn bản
				float textX = currentX + 5; // Padding
				float textY = startY - 15; // Vị trí bắt đầu text
				contentStream.beginText();
				contentStream.setFont(font, 10);
				contentStream.newLineAtOffset(textX, textY);

				// Xử lý nội dung dài
				List<String> lines = splitTextIntoLines(content[i], columnWidths[i] - 10, font, 10);
				for (String line : lines) {
					contentStream.showText(line);
					contentStream.newLineAtOffset(0, -12); // Xuống dòng
				}
				contentStream.endText();
			}
			currentX += columnWidths[i];
		}
	}

	private List<String> splitTextIntoLines(String text, float maxWidth, PDFont font, int fontSize) throws IOException {
		List<String> lines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder();
		for (String word : text.split(" ")) {
			String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
			float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
			if (textWidth > maxWidth) {
				lines.add(currentLine.toString());
				currentLine = new StringBuilder(word);
			} else {
				currentLine.append(word).append(" ");
			}
		}
		lines.add(currentLine.toString());
		return lines;
	}

}
