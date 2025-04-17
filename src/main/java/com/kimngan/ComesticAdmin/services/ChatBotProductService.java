package com.kimngan.ComesticAdmin.services;

import com.kimngan.ComesticAdmin.entity.KhuyenMai;
import com.kimngan.ComesticAdmin.entity.SanPham;
import com.kimngan.ComesticAdmin.repository.SanPhamRepository;
import java.util.Date;

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

		// Token hóa: tách từng từ trong câu hỏi
		String[] words = normalized.split("\\s+");
		Set<String> wordSet = new HashSet<>(Arrays.asList(words));

		// Lấy toàn bộ sản phẩm đang hoạt động
		List<SanPham> all = sanPhamRepository.findAllWithTenMoTaTrangThaiTrue();

		// Lọc sản phẩm: nếu tên hoặc mô tả chứa bất kỳ từ nào trong input
		List<SanPham> matched = all.stream().filter(sp -> {
			String content = (sp.getTenSanPham() + " " + sp.getMoTa()).toLowerCase();
			return wordSet.stream().anyMatch(content::contains);
		}).toList();

		return matched.isEmpty() ? "Xin lỗi, Kim Ngân Cosmetic hiện chưa có sản phẩm phù hợp với yêu cầu của bạn."
				: formatResponse(matched, prompt, true);
	}

//    private String formatResponse(List<SanPham> products, String prompt) {
//        StringBuilder context = new StringBuilder("Dưới đây là danh sách sản phẩm trong cửa hàng Kim Ngân Cosmetic:\n");
//        for (SanPham sp : products) {
//            boolean hasPromotion = sp.getKhuyenMais() != null && !sp.getKhuyenMais().isEmpty();
//            context
//                   .append("  Tên: ").append(sp.getTenSanPham()).append("\n")
//                   .append("  Mô tả: ").append(sp.getMoTa()).append("\n")
//                   .append("  Khuyến mãi: ").append(hasPromotion ? "Có" : "Không").append("\n\n");
//        }
//
//        String fullPrompt = "Người dùng hỏi: " + prompt + "\n"
//                + "Bạn là trợ lý tư vấn sản phẩm của cửa hàng Kim Ngân Cosmetic. "
//                + "Dựa vào danh sách sản phẩm dưới đây, hãy trả lời câu hỏi của khách một cách thân thiện và chuyên nghiệp:\n"
//                + context;
//
//        return openAIService.askChatbot(fullPrompt);
//    }
	private String formatResponse(List<SanPham> products, String prompt, boolean useCurrentPromotion) {
	    List<SanPham> limitedProducts = products.size() > 4 ? products.subList(0, 4) : products;

		
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

			   // ✂️ Rút gọn mô tả sản phẩm
	        String moTaRutGon = sp.getMoTa() != null && sp.getMoTa().length() > 100
	                ? sp.getMoTa().substring(0, 100) + "..."
	                : sp.getMoTa();

	        context.append("Tên: ").append(sp.getTenSanPham())
	               .append("\n ").append(moTaRutGon)
	               .append("\n  ").append(giaHienThi).append("\n\n");
		}

		String fullPrompt = "Khách hàng hỏi: " + prompt + "\n"
				+ "Bạn là nhân viên tư vấn sản phẩm của cửa hàng mỹ phẩm Kim Ngân Cosmetic. "
				+ "Hãy trả lời khách một cách thân thiện, tự nhiên, dễ hiểu. "
				+ "Không dùng cụm như 'dựa vào danh sách bên dưới'.\n\n" + context;

		return openAIService.askChatbot(fullPrompt);
	}

//    private String formatResponse(List<SanPham> products, String prompt) {
//        StringBuilder context = new StringBuilder("Dưới đây là danh sách sản phẩm trong cửa hàng Kim Ngân Cosmetic:\n");
//
//        for (SanPham sp : products) {
//            boolean hasPromotion = sp.getKhuyenMais() != null && !sp.getKhuyenMais().isEmpty();
//            BigDecimal donGia = sp.getDonGiaBan();
//            String giaHienThi = "";
//
//            if (hasPromotion) {
//                // Giảm theo % từ khuyến mãi đầu tiên
//            	  Optional<KhuyenMai> km = sp.getKhuyenMais().stream().findFirst();
//                  if (km.isPresent()) {
//                      BigDecimal phanTram = km.get().getPhanTramGiamGia();
//
//                      BigDecimal giamGia = donGia.multiply(phanTram).divide(BigDecimal.valueOf(100));
//                      BigDecimal giaSauGiam = donGia.subtract(giamGia);
//
//                      giaHienThi = "Giá khuyến mãi: " + giaSauGiam.stripTrailingZeros().toPlainString() + "₫ (giảm " + phanTram.intValue() + "%)";
//                  }
//            } 
//            
//            if (giaHienThi.isEmpty()) {
//                giaHienThi = "Giá: " + donGia.stripTrailingZeros().toPlainString() + "₫";
//            }
//
//            context.append("- Tên: ").append(sp.getTenSanPham()).append("\n")
//                   .append("  Mô tả: ").append(sp.getMoTa()).append("\n")
//                   .append("  ").append(giaHienThi).append("\n\n");
//        }
//
//        String fullPrompt = "Người dùng hỏi: " + prompt + "\n"
//                + "Bạn là trợ lý tư vấn sản phẩm của cửa hàng Kim Ngân Cosmetic. "
//                + "Dựa vào danh sách sản phẩm dưới đây, hãy trả lời câu hỏi của khách một cách thân thiện và chuyên nghiệp:\n"
//                + context;
//
//        return openAIService.askChatbot(fullPrompt);
//    }

}
