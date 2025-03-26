package com.kimngan.ComesticAdmin.controller.seller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kimngan.ComesticAdmin.entity.ChiTietDonHang;
import com.kimngan.ComesticAdmin.entity.DonHang;
import com.kimngan.ComesticAdmin.entity.HoaDon;
import com.kimngan.ComesticAdmin.entity.NguoiDungDetails;
import com.kimngan.ComesticAdmin.services.DonHangService;
import com.kimngan.ComesticAdmin.services.HoaDonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/seller")
public class HoaDonSellerController {

	@Autowired
	private HoaDonService hoaDonService;

	@Autowired
	private DonHangService donHangService;

	@GetMapping("/hoadon")
	public String getSellerHoaDons(HttpServletRequest request, Model model,

			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		if (page < 0) {
			page = 0;
		}

		Page<HoaDon> hoaDonPage = Page.empty();
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maHoaDon"));

		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			model.addAttribute("errorMessage", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
		} else {
			if (status != null && !status.isEmpty()) {
				hoaDonPage = hoaDonService.searchByStatus(status, pageRequest);
				model.addAttribute("selectedStatus", status);
			} else if (startDate != null && endDate != null) {
				LocalDateTime startDateTime = startDate.atStartOfDay();
				LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
				hoaDonPage = hoaDonService.searchByNgayXuat(startDateTime, endDateTime, pageRequest);
				model.addAttribute("startDate", startDate);
				model.addAttribute("endDate", endDate);
			} else {
				hoaDonPage = hoaDonService.getAllHoaDons(pageRequest);
			}

			if (hoaDonPage.isEmpty()) {
				model.addAttribute("message", "Không tìm thấy kết quả nào cho thông tin tìm kiếm của bạn.");
			}
		}

		// Tính tổng giá trị đơn hàng và cập nhật
		for (HoaDon hoaDon : hoaDonPage.getContent()) {
			BigDecimal tongGiaTriSanPham = BigDecimal.ZERO;
			BigDecimal phiVanChuyen = hoaDon.getDonHang().getPhiVanChuyen();

			for (ChiTietDonHang chiTiet : hoaDon.getDonHang().getChiTietDonHangs()) {
				BigDecimal thanhTien = chiTiet.getGiaTaiThoiDiemDat().multiply(new BigDecimal(chiTiet.getSoLuong()));
				tongGiaTriSanPham = tongGiaTriSanPham.add(thanhTien);
			}

			BigDecimal tongGiaTriDonHang = tongGiaTriSanPham.add(phiVanChuyen);
			hoaDon.setTongTien(tongGiaTriDonHang);
			hoaDonService.updateHoaDon(hoaDon);
		}

		if (status != null && !status.isEmpty()) {
			if (status.equals("paid")) {
				// Gộp cả 2 trạng thái được xem là "Đã thanh toán"
				List<String> thanhToanStatuses = List.of("Đã hoàn thành", "Đã xác nhận");
				hoaDonPage = hoaDonService.searchByMultipleStatuses(thanhToanStatuses, pageRequest);
			} else {
				// Các trạng thái còn lại như "Chưa xác nhận"
				hoaDonPage = hoaDonService.searchByStatus(status, pageRequest);
			}
			model.addAttribute("selectedStatus", status);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedStartDate = (startDate != null) ? startDate.format(formatter) : "";
		String formattedEndDate = (endDate != null) ? endDate.format(formatter) : "";

		model.addAttribute("isEmpty", hoaDonPage.isEmpty());
		model.addAttribute("formattedStartDate", formattedStartDate);
		model.addAttribute("formattedEndDate", formattedEndDate);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("hoaDons", hoaDonPage.getContent());
		model.addAttribute("currentPage", hoaDonPage.getNumber());
		model.addAttribute("totalPages", hoaDonPage.getTotalPages());
		model.addAttribute("size", size);
		model.addAttribute("searchAction", "/seller/hoadon");
		model.addAttribute("requestUri", request.getRequestURI());

		// Thêm thông tin người dùng vào model
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "seller/hoadon/index";
	}

	@GetMapping("/hoadon/{maDonHang}")
	public String viewSellerHoaDon(@PathVariable("maDonHang") Integer maDonHang, HttpServletRequest request,
			Model model, @RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

		if (status != null || startDate != null || endDate != null) {
			String redirectUrl = "/seller/hoadon?";

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

		DonHang donHang = donHangService.getDonHangById(maDonHang);
		if (donHang == null) {
			return "redirect:/seller/orders";
		}

		HoaDon hoaDon = hoaDonService.getHoaDonByDonHang(donHang);
		if (hoaDon == null) {
			model.addAttribute("errorMessage", "Không tìm thấy hóa đơn cho đơn hàng này.");
			return "redirect:/seller/orders";
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		String formattedNgayXuat = hoaDon.getNgayXuatHoaDon().format(formatter);

		DecimalFormat currencyFormat = new DecimalFormat("#,###.## VND");
		String formattedTongTien = currencyFormat.format(hoaDon.getTongTien());

		BigDecimal phiVanChuyen = donHang.getPhiVanChuyen();
		String formattedPhiVanChuyen = currencyFormat.format(phiVanChuyen);

		BigDecimal tongGiaTriSanPham = BigDecimal.ZERO;
		List<Map<String, String>> formattedChiTietDonHang = new ArrayList<>();

		for (ChiTietDonHang chiTiet : hoaDon.getDonHang().getChiTietDonHangs()) {
			Map<String, String> chiTietMap = new HashMap<>();
			chiTietMap.put("maSanPham", chiTiet.getSanPham().getMaSanPham().toString());
			chiTietMap.put("hinhAnh", chiTiet.getSanPham().getHinhAnh());
			chiTietMap.put("tenSanPham", chiTiet.getSanPham().getTenSanPham());
			chiTietMap.put("soLuong", String.valueOf(chiTiet.getSoLuong()));

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

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		NguoiDungDetails userDetails = (NguoiDungDetails) authentication.getPrincipal();
		model.addAttribute("user", userDetails);

		return "seller/hoadon/view";
	}

	@PostMapping("/hoadon/xacnhan/{maHoaDon}")
	public String xacNhanThanhToanHoaDonSeller(@PathVariable("maHoaDon") Integer maHoaDon,
			RedirectAttributes redirectAttributes) {
		try {
			hoaDonService.xacNhanThanhToan(maHoaDon);
			redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán hóa đơn thành công.");
		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		// Lấy mã đơn hàng để redirect về chi tiết hóa đơn
		HoaDon hoaDon = hoaDonService.getHoaDonById(maHoaDon);
		Integer maDonHang = hoaDon.getDonHang().getMaDonHang();
		return "redirect:/seller/hoadon/" + maDonHang;
	}

	@GetMapping("/hoadon/export/{id}")
	public void exportToPDFSeller(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
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

		float startY = infoStartY - 70;
		float[] columnWidths = { 50, 80, 150, 70, 100, 100 };
		float cellHeight = 50;
		String[] headers = { "Mã SP", "Hình ảnh", "Tên sản phẩm", "Số lượng", "Giá", "Thành tiền" };
		float headerCellHeight = 30;

		drawRow(contentStream, font, leftColumnX, startY, headerCellHeight, columnWidths, headers, null, document);
		startY -= headerCellHeight;

		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		if (donHang.getChiTietDonHangs() != null && !donHang.getChiTietDonHangs().isEmpty()) {
			for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangs()) {
				String imagePath = "src/main/resources/static/upload/" + chiTiet.getSanPham().getHinhAnh();
				String[] rowData = { chiTiet.getSanPham().getMaSanPham().toString(), "",
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
