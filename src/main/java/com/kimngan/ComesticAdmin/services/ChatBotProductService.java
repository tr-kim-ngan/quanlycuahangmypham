package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChatBotProductService {

	@Autowired
	private OpenAIService openAIService;

	@Autowired
	private SanPhamRepository sanPhamRepository;

	public String askProductBot(String prompt) {
		List<String> greetings = List.of("hi", "hello", "chào", "alo", "ê", "hey", "yo", "tôi", "bạn");
		String normalized = prompt.toLowerCase().trim();

		// 1. Xử lý chào hỏi
		if (greetings.stream().anyMatch(normalized::equals)) {
			return "Chào bạn! Mình là trợ lý ảo của Kim Ngân Cosmetic. Bạn cần tư vấn sản phẩm gì ạ?";
		}

		// 2. Hỏi về khuyến mãi sắp diễn ra
		if (normalized.contains("sắp") && (normalized.contains("khuyến mãi") || normalized.contains("giảm giá")
				|| normalized.contains("ưu đãi"))) {
			List<SanPham> upcoming = sanPhamRepository.findAllActiveWithUpcomingPromotions();
			if (upcoming.isEmpty()) {
				return "Hiện tại chưa có chương trình khuyến mãi sắp diễn ra tại Kim Ngân Cosmetic.";
			}
			return formatResponse(upcoming, prompt, false); // ❗ false = sắp khuyến mãi
		}

		// 3. Hỏi về khuyến mãi hiện tại
		if (normalized.contains("khuyến mãi") || normalized.contains("giảm giá") || normalized.contains("ưu đãi")) {
			List<SanPham> found = sanPhamRepository.findAllActiveProductsWithAllInfo();
			return formatResponse(found, prompt, true); // ❗ true = đang khuyến mãi
		}

		// 4. Intent detection
		String intentPrompt = "Hãy xác định xem nội dung sau có phải là yêu cầu tìm sản phẩm mỹ phẩm hay không. "
				+ "Nếu có, chỉ trả lời đúng một từ là: CÓ. Nếu không thì trả lời: KHÔNG.\n" + "Nội dung: " + prompt;
		String intent = openAIService.askChatbot(intentPrompt);
		if (intent.toLowerCase().contains("không")) {
			return "Bạn có thể nói rõ hơn về nhu cầu của mình được không ạ? Ví dụ như loại sản phẩm, mục đích sử dụng, hoặc vấn đề bạn đang gặp phải.";
		}
		// Lấy toàn bộ sản phẩm đang hoạt động
		List<SanPham> all = sanPhamRepository.findAllWithTenMoTaTrangThaiTrue();

		// Tách cụm từ trong câu hỏi (ưu tiên tìm cụm trước)

		List<String> knownPhrases = List.of(
				// Các loại son
				"son kem", "son lì", "son dưỡng", "son bóng", "son tint", "son nước", "son thỏi", "son nhung",
				"son đất", "son cam cháy",

				// Dưỡng da cơ bản
				"sữa rửa mặt", "nước tẩy trang", "nước hoa hồng", "toner", "kem dưỡng", "kem dưỡng trắng",
				"kem dưỡng ẩm", "kem dưỡng da",

				// Chống nắng & tẩy da chết
				"kem chống nắng", "xịt chống nắng", "tẩy tế bào chết", "tẩy da chết hóa học", "tẩy da chết vật lý",

				// Mặt nạ
				"mặt nạ giấy", "mặt nạ ngủ", "mặt nạ đất sét", "mặt nạ cấp ẩm", "mặt nạ dưỡng trắng",

				// Serum & treatment
				"serum trị mụn", "serum vitamin c", "serum cấp ẩm", "serum chống lão hóa", "serum phục hồi",
				"tinh chất dưỡng da", "tinh chất chống nắng",

				// Trang điểm nền
				"kem nền", "phấn phủ", "phấn nước", "kem che khuyết điểm", "kem lót", "cushion", "foundation", "primer",

				// Trang điểm mắt - mày - má
				"phấn mắt", "kẻ mắt", "chì kẻ mày", "mascara", "phấn má hồng", "kem má", "gel kẻ mắt",

				// Làm sạch & hỗ trợ
				"nước tẩy trang", "nước hoa hồng", "xịt khoáng", "xịt dưỡng", "xịt phục hồi", "dầu tẩy trang",
				"gel rửa mặt",

				// Dành cho da cụ thể
				"cho da dầu", "cho da khô", "cho da nhạy cảm", "cho da mụn", "cho da hỗn hợp", "cho da thường",

				// Vấn đề da
				"trị mụn", "dưỡng trắng", "cấp ẩm", "làm dịu da", "se khít lỗ chân lông", "phục hồi da",
				"chống lão hóa", "trị thâm", "trị nám", "giảm mụn", "ngừa mụn",

				// Sản phẩm làm sạch cá nhân
				"sữa tắm", "dầu gội", "sữa dưỡng thể", "kem dưỡng thể", "xịt khử mùi", "kem trị thâm nách",

				"trị thâm", "trị mụn", "trị mụn ẩn", "giảm thâm", "mờ thâm", "trị mụn đầu đen", "mụn viêm",
				"mụn trứng cá", "mụn đỏ", "mụn sưng", "mụn ẩn", "ngừa mụn");

		// Ưu tiên tìm theo cụm từ trước
		List<SanPham> matchedByPhrase = all.stream().filter(sp -> {
			String content = (sp.getTenSanPham() + " " + sp.getMoTa()).toLowerCase();
			return knownPhrases.stream().filter(normalized::contains) // chỉ xét cụm nào có trong input
					.anyMatch(content::contains); // và cũng có trong tên/mô tả sản phẩm
		}).toList();

		// Nếu tìm được theo cụm, trả về luôn
		if (!matchedByPhrase.isEmpty()) {
			return formatResponse(matchedByPhrase, prompt, true);
		}

		// Nếu không có thì fallback về tìm theo từng từ như cũ
		String[] words = normalized.split("\\s+");
		Set<String> wordSet = new HashSet<>(Arrays.asList(words));

		List<SanPham> matchedByWord = all.stream().filter(sp -> {
			String content = (sp.getTenSanPham() + " " + sp.getMoTa()).toLowerCase();
			return wordSet.stream().anyMatch(content::contains);
		}).toList();

		return matchedByWord.isEmpty() ? "Xin lỗi, Kim Ngân Cosmetic hiện chưa có sản phẩm phù hợp với yêu cầu của bạn."
				: formatResponse(matchedByWord, prompt, true);

	}

	private String formatResponse(List<SanPham> products, String prompt, boolean useCurrentPromotion) {
	//	List<SanPham> limitedProducts = products.size() > 4 ? products.subList(0, 4) : products;
		// Ưu tiên theo độ dài tên hoặc có nhiều từ khóa trùng
		List<SanPham> limitedProducts = products.stream()
		        .sorted(Comparator.comparingInt(sp -> -(sp.getTenSanPham() + sp.getMoTa()).length())) // ví dụ: sắp theo mô tả dài nhất
		        .limit(4)
		        .toList();

		StringBuilder context = new StringBuilder("Dưới đây là danh sách sản phẩm trong cửa hàng Kim Ngân Cosmetic:\n");
		LocalDate today = LocalDate.now();

		for (SanPham sp : limitedProducts) {
			BigDecimal donGia = sp.getDonGiaBan();
			String giaHienThi = "";

			if (useCurrentPromotion) {
				// Đang khuyến mãi
				Optional<KhuyenMai> km = sp.getKhuyenMais().stream().filter(k -> Boolean.TRUE.equals(k.getTrangThai()))
						.filter(k -> !k.getNgayBatDau().toLocalDate().isAfter(today)
								&& !k.getNgayKetThuc().toLocalDate().isBefore(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				if (km.isPresent()) {
					BigDecimal phanTram = km.get().getPhanTramGiamGia();
					BigDecimal giaSauGiam = donGia.subtract(donGia.multiply(phanTram).divide(BigDecimal.valueOf(100)));
					giaHienThi = "Giá khuyến mãi: " + giaSauGiam.stripTrailingZeros().toPlainString() + "₫ (giảm "
							+ phanTram.intValue() + "%)";
				} else {
					giaHienThi = "Giá: " + donGia.stripTrailingZeros().toPlainString() + "₫";
				}

			} else {
				// Sắp khuyến mãi
				Optional<KhuyenMai> km = sp.getKhuyenMais().stream().filter(k -> Boolean.TRUE.equals(k.getTrangThai()))
						.filter(k -> k.getNgayBatDau().toLocalDate().isAfter(today))
						.max(Comparator.comparing(KhuyenMai::getPhanTramGiamGia));

				if (km.isPresent()) {
					BigDecimal phanTram = km.get().getPhanTramGiamGia();
					BigDecimal giamGia = donGia.multiply(phanTram).divide(BigDecimal.valueOf(100));
					BigDecimal giaSauGiam = donGia.subtract(giamGia);

					giaHienThi = "Giá hiện tại: " + donGia.stripTrailingZeros().toPlainString() + "₫ (sắp giảm "
							+ phanTram.intValue() + "% → còn " + giaSauGiam.stripTrailingZeros().toPlainString() + "₫)";
				} else {
					giaHienThi = "Giá: " + donGia.stripTrailingZeros().toPlainString() + "₫";
				}

			}

			// Rút gọn mô tả sản phẩm
			String moTaRutGon = sp.getMoTa() != null && sp.getMoTa().length() > 100
					? sp.getMoTa().substring(0, 100) + "..."
					: sp.getMoTa();

			context.append("Tên: ").append(sp.getTenSanPham()).append("\n ").append(moTaRutGon).append("\n  ")
					.append(giaHienThi).append("\n\n");
		}

		String fullPrompt = "Khách hàng hỏi: " + prompt + "\n"
				+ "Bạn là nhân viên tư vấn sản phẩm của cửa hàng mỹ phẩm Kim Ngân Cosmetic. "
				+ "Dưới đây là danh sách sản phẩm có sẵn trong cửa hàng. "
				+ "Chỉ sử dụng đúng thông tin trong danh sách này để trả lời, không được bịa thêm sản phẩm hoặc thông tin khác. "
				+ "Trả lời thân thiện, tự nhiên, dễ hiểu, ngắn gọn, không dài dòng. "
				+ "Không dùng cụm như 'dựa vào danh sách bên dưới', mà hãy trình bày như một người thật đang gợi ý sản phẩm.\n\n"
				+ context;

		return openAIService.askChatbot(fullPrompt);
	}

}
