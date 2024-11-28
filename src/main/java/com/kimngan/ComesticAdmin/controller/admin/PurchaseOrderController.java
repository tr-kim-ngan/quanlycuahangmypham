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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

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
			// Tìm kiếm theo nhà cung cấp
			pageDonNhapHang = donNhapHangService.findByNhaCungCap_Ten(keyword, PageRequest.of(page, size));
			model.addAttribute("keyword", keyword);
		} else {
			// Hiển thị tất cả đơn nhập hàng
			pageDonNhapHang = donNhapHangService.findAllActive(PageRequest.of(page, size));
		}

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
			@RequestParam("maNhaCungCap") Integer maNhaCungCap, Model model) {

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
		// return "redirect:/admin/purchaseorder/add-details/" +
		// donNhapHang.getMaDonNhapHang();
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
		// model.addAttribute("donNhapHang", donNhapHang);
		// model.addAttribute("products", sanPhamService.getAll()); // Lấy danh sách sản
		// phẩm

		List<SanPham> sanPhams = sanPhamService.findByTrangThai(true); // Lấy danh sách sản phẩm còn hoạt động
		model.addAttribute("sanPhams", sanPhams);
		model.addAttribute("donNhapHang", donNhapHang);
		model.addAttribute("chiTietList", chiTietDonNhapHangService.findByDonNhapHang(donNhapHang));

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
		// List<ChiTietDonNhapHang> chiTietList =
		// chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
		model.addAttribute("chiTietList", chiTietList);

		return "admin/purchaseorder/add-details"; // Trả về trang thêm chi tiết }
	}

//sửa chi tiết đơn 
	@GetMapping("/edit/{id}")
	public String viewPurchaseOrderDetails(@PathVariable("id") Integer maDonNhapHang, Model model) {
		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
		List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);

		List<SanPham> activeSanPhams = sanPhamService.findByTrangThai(true);

		model.addAttribute("donNhapHang", donNhapHang);
		model.addAttribute("chiTietList", chiTietList);
		model.addAttribute("sanPhams", activeSanPhams);

		boolean isFirstEdit = chiTietList.isEmpty();
		model.addAttribute("isFirstEdit", isFirstEdit);

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

				// Tính giá trị nhập
				// BigDecimal giaTriNhap = donGiaNhap.get(i).multiply(new
				// BigDecimal(soLuongNhap.get(i)));
				// tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);

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

	// Thêm chi tiết đơn nhập hàng
//	@PostMapping("/add-purchase-order-detail/{maDonNhapHang}")
//	public String addPurchaseOrderDetail(
//			@PathVariable("maDonNhapHang") Integer maDonNhapHang,
//			@RequestParam("sanPhamIds") Integer sanPhamIds, 
//			@RequestParam("soLuongNhap") Integer soLuongNhap,
//			@RequestParam("donGiaNhap") BigDecimal donGiaNhap) {
//
//		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
//
//		ChiTietDonNhapHang chiTiet = new ChiTietDonNhapHang();
//		chiTiet.setDonNhapHang(donNhapHang);
//		chiTiet.setSanPham(sanPhamService.findById(sanPhamIds));
//		chiTiet.setSoLuongNhap(soLuongNhap);
//		chiTiet.setDonGiaNhap(donGiaNhap);
//
//		chiTietDonNhapHangService.create(chiTiet);
//		return "redirect:/admin/purchaseorder/" + maDonNhapHang;
//	}
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

			// Cập nhật tổng giá trị nhập hàng sau khi ẩn chi tiết
			// DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
			// Cập nhật lại tổng giá trị của đơn nhập hàng
			DonNhapHang donNhapHang = chiTiet.getDonNhapHang();
			BigDecimal tongGiaTriNhap = BigDecimal.ZERO;

			List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang).stream()
					.filter(ct -> ct.isTrangThai()) // Lọc những chi tiết còn hoạt động
					.collect(Collectors.toList());

			// if (donNhapHang != null) {
			// BigDecimal tongGiaTriNhap = BigDecimal.ZERO;

//	            // Tính lại tổng giá trị sau khi ẩn chi tiết
//	            List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang)
//	                    .stream()
//	                    .filter(ct -> ct.isTrangThai()) // Lọc những chi tiết còn hoạt động
//	                    .collect(Collectors.toList());

			for (ChiTietDonNhapHang ct : chiTietList) {
				BigDecimal giaTriNhap = ct.getDonGiaNhap().multiply(new BigDecimal(ct.getSoLuongNhap()));
				tongGiaTriNhap = tongGiaTriNhap.add(giaTriNhap);
			}

			donNhapHang.setTongGiaTriNhapHang(tongGiaTriNhap);
			donNhapHangService.update(donNhapHang);
			// }
		}
		return "redirect:/admin/add-purchase-order-detail/" + maDonNhapHang;
		//return "redirect:/admin/edit/" + maDonNhapHang;
	}

//	@PostMapping("/delete-purchase-order/{maDonNhapHang}")
//	public String deletePurchaseOrder(@PathVariable("maDonNhapHang") Integer maDonNhapHang, Model model) {
//
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
//		model.addAttribute("user", userDetails);
//
//		DonNhapHang donNhapHang = donNhapHangService.findById(maDonNhapHang);
//
//		if (donNhapHang == null) {
//			model.addAttribute("errorMessage", "Đơn nhập hàng không tồn tại.");
//			return "redirect:/admin/purchaseorder";
//		}
//
//		// Kiểm tra xem đơn nhập hàng có chi tiết không
//		List<ChiTietDonNhapHang> chiTietList = chiTietDonNhapHangService.findByDonNhapHang(donNhapHang);
//
//		if (!chiTietList.isEmpty()) {
//			// Nếu có chi tiết đơn nhập hàng thì không cho phép xóa và hiển thị thông báo
//			// lỗi
//			model.addAttribute("errorMessage", "Không thể xóa đơn nhập hàng đã có chi tiết.");
//			return "redirect:/admin/purchaseorder";
//		}
//
//		// Nếu tổng giá trị nhập hàng là 0 thì cho phép xóa
//		if (donNhapHang.getTongGiaTriNhapHang().compareTo(BigDecimal.ZERO) == 0) {
//			donNhapHang.setTrangThai(false); // Ẩn đơn nhập hàng bằng cách chuyển trạng thái
//			donNhapHangService.update(donNhapHang);
//			model.addAttribute("successMessage", "Xóa đơn nhập hàng thành công.");
//		} else {
//			model.addAttribute("errorMessage", "Không thể xóa đơn nhập hàng đã có chi tiết.");
//		}
//
//		return "redirect:/admin/purchaseorder";
//	}
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
		donNhapHang.setTrangThai(false);
		donNhapHangService.update(donNhapHang);

		// Quay lại trang danh sách đơn nhập hàng với thông báo thành công (nếu cần)
		redirectAttributes.addFlashAttribute("successMessage", "Xóa đơn nhập hàng thành công.");
		return "redirect:/admin/purchaseorder"; // Quay lại trang danh sách đơn nhập hàng
	}

}
