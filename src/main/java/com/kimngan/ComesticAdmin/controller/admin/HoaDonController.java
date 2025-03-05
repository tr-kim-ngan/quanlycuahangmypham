package com.kimngan.ComesticAdmin.controller.admin;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

@Controller
@RequestMapping("/admin")
public class HoaDonController {

	@Autowired
	private HoaDonService hoaDonService;

	@Autowired
	private DonHangService donHangService;

	@GetMapping("/hoadon")
	public String getHoaDons(HttpServletRequest request, Model model,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		if (page < 0) {
			page = 0;
		}

		Page<HoaDon> hoaDonPage = Page.empty();
		// Cấu hình sắp xếp giảm dần theo maHoaDon
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maHoaDon"));

		// Kiểm tra nếu ngày bắt đầu sau ngày kết thúc
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			model.addAttribute("errorMessage", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
		} else {
			// Lọc theo trạng thái nếu được cung cấp
			if (status != null && !status.isEmpty()) {
				hoaDonPage = hoaDonService.searchByStatus(status, pageRequest);
				model.addAttribute("selectedStatus", status);
			}
			// Nếu có từ khóa tìm kiếm theo ngày xuất
			else if (startDate != null && endDate != null) {
				LocalDateTime startDateTime = startDate.atStartOfDay();
				LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
				hoaDonPage = hoaDonService.searchByNgayXuat(startDateTime, endDateTime, pageRequest);
				model.addAttribute("startDate", startDate);
				model.addAttribute("endDate", endDate);
			}
			// Nếu không có từ khóa tìm kiếm, lấy tất cả hóa đơn
			else {
				hoaDonPage = hoaDonService.getAllHoaDons(pageRequest);
			}

			// Kiểm tra nếu không có kết quả
			if (hoaDonPage.isEmpty()) {
				model.addAttribute("message", "Không tìm thấy kết quả nào cho thông tin tìm kiếm của bạn.");
			}
		}

		// Định dạng ngày thành chuỗi để truyền vào model
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedStartDate = (startDate != null) ? startDate.format(formatter) : "";
		String formattedEndDate = (endDate != null) ? endDate.format(formatter) : "";
		boolean isEmpty = hoaDonPage.isEmpty();
		// Định dạng tiền tệ và truyền trực tiếp vào model
		// Định dạng tiền tệ trực tiếp vào `HoaDon` khi thêm vào model

		// model.addAttribute("hoaDons", formattedHoaDons);
		// DecimalFormat currencyFormat = new DecimalFormat("#,### đ");

		for (HoaDon hoaDon : hoaDonPage.getContent()) {
			BigDecimal tongGiaTriSanPham = BigDecimal.ZERO;
			BigDecimal phiVanChuyen = hoaDon.getDonHang().getPhiVanChuyen(); // Lấy phí vận chuyển

			// Tính tổng giá trị sản phẩm
			for (ChiTietDonHang chiTiet : hoaDon.getDonHang().getChiTietDonHangs()) {
				BigDecimal thanhTien = chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong()));
				tongGiaTriSanPham = tongGiaTriSanPham.add(thanhTien);
			}

			// Tính tổng giá trị đơn hàng
			BigDecimal tongGiaTriDonHang = tongGiaTriSanPham.add(phiVanChuyen);
			// Thêm vào model
			hoaDon.setTongTien(tongGiaTriDonHang); // Gán tổng đơn hàng vào hoaDon
			hoaDonService.updateHoaDon(hoaDon);

		}

		model.addAttribute("isEmpty", isEmpty);
		model.addAttribute("formattedStartDate", formattedStartDate);
		model.addAttribute("formattedEndDate", formattedEndDate);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("hoaDons", hoaDonPage.getContent());
		// model.addAttribute("hoaDons", formattedHoaDons);
		// model.addAttribute("hoaDons", formattedHoaDons);
		model.addAttribute("currentPage", hoaDonPage.getNumber());
		model.addAttribute("totalPages", hoaDonPage.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/admin/hoadon");
		model.addAttribute("requestUri", request.getRequestURI());

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/hoadon/index";
	}

	// Xem chi tiết hóa đơn
	@GetMapping("/hoadon/{maDonHang}")
	public String viewHoaDon(@PathVariable("maDonHang") Integer maDonHang, HttpServletRequest request, Model model,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

		// Điều hướng về trang danh sách hóa đơn nếu nhấn tìm kiếm
		if (status != null || startDate != null || endDate != null) {
			String redirectUrl = "/admin/hoadon?";

			if (status != null) {
				try {
					redirectUrl += "status=" + URLEncoder.encode(status, StandardCharsets.UTF_8.toString()) + "&";
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (startDate != null) {
				redirectUrl += "startDate=" + startDate + "&";
			}

			if (endDate != null) {
				redirectUrl += "endDate=" + endDate + "&";
			}

			redirectUrl += "page=" + page + "&size=" + size;

			return "redirect:" + redirectUrl;
		}

		// Lấy thông tin đơn hàng
		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			return "redirect:/admin/orders"; // Nếu đơn hàng không tồn tại, chuyển về danh sách đơn hàng
		}

		// Lấy thông tin hóa đơn liên kết với đơn hàng
		HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
		if (hoaDon == null) {
			model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
			return "redirect:/admin/orders"; // Nếu hóa đơn không tồn tại, chuyển về danh sách đơn hàng
		}

		// Định dạng ngày tháng để hiển thị
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		String formattedNgayXuat = hoaDon.getNgayXuatHoaDon().format(formatter);

		// Định dạng tiền tệ
		DecimalFormat currencyFormat = new DecimalFormat("#,###.## VND");
		String formattedTongTien = currencyFormat.format(hoaDon.getTongTien());

		// Lấy phí vận chuyển từ đơn hàng
		BigDecimal phiVanChuyen = donHang.getPhiVanChuyen();
		String formattedPhiVanChuyen = currencyFormat.format(phiVanChuyen);

		// Tính tổng giá trị sản phẩm và tổng giá trị đơn hàng
		BigDecimal tongGiaTriSanPham = BigDecimal.ZERO;

		// Lấy chi tiết đơn hàng và định dạng tiền cho mỗi chi tiết
		List<Map<String, String>> formattedChiTietDonHang = new ArrayList<>();
		for (ChiTietDonHang chiTiet : hoaDon.getDonHang().getChiTietDonHangs()) {
			Map<String, String> chiTietMap = new HashMap<>();
			chiTietMap.put("maSanPham", chiTiet.getSanPham().getMaSanPham().toString());
			chiTietMap.put("hinhAnh", chiTiet.getSanPham().getHinhAnh());
			chiTietMap.put("tenSanPham", chiTiet.getSanPham().getTenSanPham());
			chiTietMap.put("soLuong", String.valueOf(chiTiet.getSoLuong()));

			// Định dạng giá trị tiền
			String giaTaiThoiDiemDat = currencyFormat.format(chiTiet.getGiaTaiThoiDiemDat());
//			String thanhTien = currencyFormat
//					.format(chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong())));

//			chiTietMap.put("giaTaiThoiDiemDat", giaTaiThoiDiemDat);
//			chiTietMap.put("thanhTien", thanhTien);
			BigDecimal thanhTien = chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong()));
			tongGiaTriSanPham = tongGiaTriSanPham.add(thanhTien);
			chiTietMap.put("giaTaiThoiDiemDat", currencyFormat.format(chiTiet.getGiaTaiThoiDiemDat()));
			chiTietMap.put("thanhTien", currencyFormat.format(thanhTien));

			formattedChiTietDonHang.add(chiTietMap);
		}

		BigDecimal tongGiaTriDonHang = tongGiaTriSanPham.add(phiVanChuyen);
		String formattedTongGiaTriSanPham = currencyFormat.format(tongGiaTriSanPham);
		String formattedTongGiaTriDonHang = currencyFormat.format(tongGiaTriDonHang);

		model.addAttribute("formattedPhiVanChuyen", formattedPhiVanChuyen);
		model.addAttribute("formattedTongGiaTriSanPham", formattedTongGiaTriSanPham);
		model.addAttribute("formattedTongGiaTriDonHang", formattedTongGiaTriDonHang);

		// Đưa dữ liệu vào model
		model.addAttribute("hoaDon", hoaDon);
		model.addAttribute("formattedTongTien", formattedTongTien);

		model.addAttribute("formattedNgayXuat", formattedNgayXuat);
		model.addAttribute("formattedChiTietDonHang", formattedChiTietDonHang);
		model.addAttribute("status", status);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("requestUri", request.getRequestURI());
		model.addAttribute("formattedTrangThaiThanhToan",
				hoaDon.getTrangThaiThanhToan().equals("Chưa xác nhận") ? "Chưa thanh toán" : "Đã thanh toán");

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "admin/hoadon/view"; // Trả về view để hiển thị chi tiết hóa đơn
	}

	@PostMapping("/hoadon/xacnhan/{maHoaDon}")
	public String xacNhanThanhToanHoaDon(@PathVariable("maHoaDon") Integer maHoaDon,
			RedirectAttributes redirectAttributes) {
		try {
			hoaDonService.xacNhanThanhToan(maHoaDon);
			redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán hóa đơn thành công.");
		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		// Chuyển hướng về chi tiết hóa đơn dựa vào mã đơn hàng liên kết với hóa đơn
		HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
		Integer maDonHang = hoaDon.getDonHang().getMaDonHang();
		return "redirect:/admin/hoadon/" + maDonHang;
	}

	@GetMapping("/hoadon/export/{id}")
	public void exportToPDF(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
		response.setContentType("application/pdf");

		HoaDon hoaDon = hoaDonService.findById(id);
		DonHang donHang = hoaDon.getDonHang();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		String fileName = "HoaDon_" + hoaDon.getMaHoaDon() + "_" + hoaDon.getNgayXuatHoaDon().format(formatter)
				+ ".pdf";
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		InputStream fontStream = getClass().getResourceAsStream("/fonts/TIMES.TTF");
		PDFont font = PDType0Font.load(document, fontStream);

		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		// Tiêu đề
		contentStream.beginText();
		contentStream.setFont(font, 16);
		contentStream.newLineAtOffset(220, 750);
		contentStream.showText("HÓA ĐƠN");
		contentStream.endText();

		// Bố cục 2 cột
		float leftColumnX = 50;
		float rightColumnX = 300;
		float infoStartY = 700;
		float lineHeight = 15;

		// Cột trái
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.newLineAtOffset(leftColumnX, infoStartY);
		contentStream.setLeading(lineHeight);
		contentStream.showText("Người Nhận: " + hoaDon.getTenNguoiNhan());
		contentStream.newLine();
		contentStream.showText("Địa Chỉ: " + hoaDon.getDiaChiGiaoHang());
		contentStream.newLine();
		contentStream.showText("Số Điện Thoại: " + hoaDon.getSoDienThoaiNhanHang());
		contentStream.newLine();
		contentStream.showText("Trạng Thái Thanh Toán: " + hoaDon.getTrangThaiThanhToan());
		contentStream.endText();

		// Cột phải
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.newLineAtOffset(rightColumnX, infoStartY);
		contentStream.setLeading(lineHeight);
		contentStream.showText("Mã Hóa Đơn: " + hoaDon.getMaHoaDon());
		contentStream.newLine();
		contentStream.showText("Ngày Xuất: " + hoaDon.getNgayXuatHoaDon().format(formatter));
		contentStream.newLine();
		contentStream.showText("Tổng Tiền: " + new DecimalFormat("#,###.## VND").format(hoaDon.getTongTien()));
		contentStream.endText();

		// Khoảng cách trước khi thêm bảng
		float startY = infoStartY - 70;

		// Bảng chi tiết sản phẩm
		float[] columnWidths = { 50, 80, 150, 70, 100, 100 }; // Cột hình ảnh nhỏ hơn
		float cellHeight = 50;
		String[] headers = { "Mã SP", "Hình ảnh", "Tên sản phẩm", "Số lượng", "Giá", "Thành tiền" };

		// Chiều cao của hàng (tăng để phần dưới header cao hơn)
		float headerCellHeight = 30; // Tăng chiều cao tiêu đề

		// Vẽ tiêu đề bảng với chiều cao lớn hơn
		drawRow(contentStream, font, leftColumnX, startY, headerCellHeight, columnWidths, headers, null, document);
		startY -= headerCellHeight; // Trừ đi chiều cao của tiêu đề

		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		if (donHang.getChiTietDonHangs() != null && !donHang.getChiTietDonHangs().isEmpty()) {
			for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
				String imagePath = "src/main/resources/static/upload/" + chiTiet.getSanPham().getHinhAnh();
				String[] rowData = { chiTiet.getSanPham().getMaSanPham().toString(), "", // Cột hình ảnh để trống, hiển
																							// thị bằng imagePath
						chiTiet.getSanPham().getTenSanPham(), String.valueOf(chiTiet.getSoLuong()),
						decimalFormat.format(chiTiet.getGiaTaiThoiDiemDat()) + " VND",
						decimalFormat
								.format(chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong())))
								+ " VND" };
				drawRow(contentStream, font, leftColumnX, startY, cellHeight, columnWidths, rowData, imagePath,
						document);
				startY -= cellHeight;
			}
		} else {
			contentStream.beginText();
			contentStream.setFont(font, 12);
			contentStream.newLineAtOffset(leftColumnX, startY);
			contentStream.showText("Không có sản phẩm nào trong hóa đơn.");
			contentStream.endText();
		}

		contentStream.close();
		document.save(response.getOutputStream());
		document.close();
	}

	private void drawRow(PDPageContentStream contentStream, PDFont font, float startX, float startY, float cellHeight,
			float[] columnWidths, String[] content, String imagePath, PDDocument document) throws IOException {
		float currentX = startX;

		// Vẽ khung của dòng
		contentStream.setLineWidth(0.75f);
		for (float width : columnWidths) {
			contentStream.addRect(currentX, startY - cellHeight, width, cellHeight);
			currentX += width;
		}
		contentStream.stroke();

		currentX = startX;

		for (int i = 0; i < content.length; i++) {
			if (i == 1 && imagePath != null) { // Cột hình ảnh
				PDImageXObject image = PDImageXObject.createFromFile(imagePath, document);
				float imageWidth = columnWidths[i] - 10; // Chiều rộng ảnh
				float aspectRatio = (float) image.getHeight() / image.getWidth();
				float imageHeight = imageWidth * aspectRatio;
				if (imageHeight > cellHeight - 10) {
					imageHeight = cellHeight - 10;
					imageWidth = imageHeight / aspectRatio;
				}
				float imageX = currentX + (columnWidths[i] - imageWidth) / 2; // Căn giữa hình ảnh trong cột
				float imageY = startY - cellHeight + (cellHeight - imageHeight) / 2;
				contentStream.drawImage(image, imageX, imageY, imageWidth, imageHeight);
			} else { // Các cột khác
				float textX = currentX + 5; // Padding bên trái
				float textY = startY - 15; // Căn lề trên
				contentStream.beginText();
				contentStream.setFont(font, 13);
				contentStream.newLineAtOffset(textX, textY);

				List<String> lines = splitTextIntoLines(content[i], columnWidths[i] - 10, font, 13);
				for (String line : lines) {
					contentStream.showText(line);
					contentStream.newLineAtOffset(0, -14); // Xuống dòng
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
		if (currentLine.length() > 0) {
			lines.add(currentLine.toString());
		}

		return lines;
	}

}
