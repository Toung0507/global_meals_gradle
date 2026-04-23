package com.example.global_meals_gradle.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference; // 以防爆出黃底線的
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.example.global_meals_gradle.constants.AiType;
import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.StaffRole;
import com.example.global_meals_gradle.dao.AiGeneratedDao;
import com.example.global_meals_gradle.entity.AiGenerated;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.AiProductDescReq;
import com.example.global_meals_gradle.req.AiPromotionsReq;
import com.example.global_meals_gradle.res.AiRes;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;

@Service
public class AiService {

	@Autowired
	private RestClient restClient;

	@Value("${ai.product.key}")
	private String productKey;

	@Value("${ai.promo.key}")
	private String promoKey;

	@Autowired
	private AiGeneratedDao aiGeneratedDao;

	private static final String SESSION_KEY = "loginStaff";

	private String validModelName;

	// 啟動時自動檢查模型名稱
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void initModelName() {
	    String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + productKey;
	    try {
	        Map<String, Object> response = restClient.get().uri(url).retrieve()
	                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

	        List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");

	        // 修正邏輯：優先尋找 gemini-2.5-flash (你清單中的第一個)
	        this.validModelName = models.stream()
	                .map(m -> (String) m.get("name"))
	                .filter(name -> name.contains("gemini-2.5-flash")) // 改為搜尋 2.5
	                .findFirst()
	                .orElse("models/gemini-2.5-flash"); // 預設改為 2.5

	        System.out.println(">>> 成功選定模型: " + this.validModelName);
	    } catch (Exception e) {
	        System.err.println(">>> 初始化失敗: " + e.getMessage());
	        this.validModelName = "models/gemini-2.5-flash"; // 預設改為 2.5
	    }
	}

	// 1. 生成商品描述
	@Transactional(rollbackFor = Exception.class)
	public AiRes generateProductDesc(AiProductDescReq req, HttpSession session) {
//		AiRes authRes = validateAdminAccess(session);
//		if (authRes != null)
//			return new AiRes(authRes.getCode(), authRes.getMessage());

		String prompt = String.format(
			    "請為以下商品撰寫一段適用於『實體菜單』的短文案。請遵守以下規則：\n" +
			    "1. 字數：請嚴格控制在 50 個中文字以內。\n" +
			    "2. 風格：誘人、簡潔、直接描述口感與風味，不要寫過多的推銷廢話。\n" +
			    "3. 輸出：直接給出描述內容，不要包含任何開場白（例如：好的，這是一段...）。\n\n" +
			    "商品名稱：%s\n" +
			    "商品類別：%s",
			    req.getProductName(), req.getCategory()
			);

		String content = callAiApi(prompt);
		int productId = req.getProductid();

		saveAiLog(AiType.PRODUCT_DESC, productId, content);

		return new AiRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), content);
	}

	// 2. 生成活動文案 (含圖片)
	@Transactional(rollbackFor = Exception.class)
	public AiRes generatePromoCopy(AiPromotionsReq req, MultipartFile file, HttpSession session) {
		AiRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return new AiRes(authRes.getCode(), authRes.getMessage());

		try {
			String mimeType = file.getContentType();
			if (mimeType == null || !mimeType.startsWith("image/")) {
				mimeType = "image/jpeg";
			}

			byte[] imageBytes = file.getBytes();
			String prompt = String.format(
				    "請根據圖片內容與活動名稱『%s』，撰寫一篇吸睛的社群活動宣傳文案。\n" +
				    "要求：\n" +
				    "1. 風格：活潑、有吸引力，能引發互動。\n" +
				    "2. 字數：嚴格控制在 80 個中文字以內 (約 3-4 句短句)。\n" +
				    "3. 內容：突顯活動亮點與吸引人處，不要寫冗長的廢話。\n" +
				    "4. 格式：直接輸出文案內容，不要包含任何開場白 (如：這是一篇文案...)。",
				    req.getActivityName()
				);

			String content = callAiApiWithImage(prompt, imageBytes, mimeType);
			int promotionsId = req.getPromotionsId();

			saveAiLog(AiType.PROMO_COPY, promotionsId, content);

			return new AiRes(ReplyMessage.SUCCESS.getCode(), //
					ReplyMessage.SUCCESS.getMessage(), content);

		} catch (IOException e) {
			return new AiRes(ReplyMessage.SYSTEM_ERROR.getCode(), //
					ReplyMessage.SYSTEM_ERROR.getMessage() + e.getMessage());
		}
	}

	// --- 私有共用方法 ---

	private void saveAiLog(AiType type, Integer refId, String content) {
		AiGenerated log = new AiGenerated();
		log.setAiType(type);
		log.setReferenceId(refId);
		log.setGeneratedDescription(content);
		aiGeneratedDao.save(log);
	}

	// 呼叫純文字 API
	private String callAiApi(String prompt) {
		String url = "https://generativelanguage.googleapis.com/v1beta/" + validModelName //
				+ ":generateContent?key=" + productKey;

		Map<String, Object> requestBody = Map.of("contents", //
				List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

		Map<String, Object> response = restClient.post().uri(url).body(requestBody).retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {
				});

		return extractTextFromResponse(response);
	}

	// 呼叫 AI (用圖片 + 文字)
	private String callAiApiWithImage(String prompt, byte[] imageBytes, String mimeType) {
		String url = "https://generativelanguage.googleapis.com/v1beta/" + validModelName //
				+ ":generateContent?key=" + promoKey;
		String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

		Map<String, Object> requestBody = Map.of("contents", //
				List.of(Map.of("parts", List.of(Map.of("text", prompt), // 文字部分
						Map.of("inline_data", Map.of("mime_type", //
								mimeType, // 動態設定圖片格式 (例如 image/jpeg)
								"data", base64Image // 剛才轉好的 Base64 字串
						))))));

		Map<String, Object> response = restClient.post().uri(url).body(requestBody).retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {
				});

		return extractTextFromResponse(response);
	}

	@SuppressWarnings("unchecked")
	private String extractTextFromResponse(Map<String, Object> response) {
		try {
			List<Map<String, Object>> candidates = //
					(List<Map<String, Object>>) response.get("candidates");

			if (candidates == null || candidates.isEmpty()) {
				return "AI 回應異常 (無內容回傳)";
			}

			Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

			List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

			return (String) parts.get(0).get("text");
		} catch (Exception e) {
			return "AI 解析錯誤: " + e.getMessage();
		}
	}

	private AiRes validateAdminAccess(HttpSession session) {
		Staff staff = (Staff) session.getAttribute(SESSION_KEY);
		if (staff == null) {
			return new AiRes(ReplyMessage.NOT_LOGIN.getCode(), ReplyMessage.NOT_LOGIN.getMessage());
		}
		if (!staff.isStatus()) {
			return new AiRes(ReplyMessage.ACCOUNT_DISABLED.getCode(), //
					ReplyMessage.ACCOUNT_DISABLED.getMessage());
		}
		if (staff.getRole() != StaffRole.ADMIN) {
			return new AiRes(ReplyMessage.OPERATE_ERROR.getCode(), "權限不足，僅限管理員操作");
		}
		return null;
	}
}
