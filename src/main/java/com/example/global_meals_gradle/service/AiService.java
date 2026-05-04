package com.example.global_meals_gradle.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference; // 以防爆出黃底線的
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.example.global_meals_gradle.constants.AiType;
import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.StaffRole;
import com.example.global_meals_gradle.dao.AiGeneratedDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.entity.AiGenerated;
import com.example.global_meals_gradle.entity.Products;
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

	@Autowired
	private ProductsDao productsDao;

	@Value("${ai.product.key}")
	private String productKey;

	@Value("${ai.promo.key}")
	private String promoKey;

	@Autowired
	private AiGeneratedDao aiGeneratedDao;

	private static final String SESSION_KEY = "loginStaff";

	private String validModelName;

	private static final org.slf4j.Logger log = org.slf4j //
			.LoggerFactory.getLogger(AiService.class);
	
	// 啟動時自動檢查模型名稱
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void initModelName() {
		String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + productKey;
		try {
			Map<String, Object> response = restClient.get().uri(url).retrieve()
					.body(new ParameterizedTypeReference<Map<String, Object>>() {
					});

			List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");

			// 修正邏輯：優先尋找 gemini-2.5-flash (你清單中的第一個)
			this.validModelName = models.stream().map(m -> (String) m.get("name"))
					.filter(name -> name.contains("gemini-2.5-flash")) // 改為搜尋 2.5
					.findFirst().orElse("models/gemini-2.5-flash"); // 預設改為 2.5

			System.out.println(">>> 成功選定模型: " + this.validModelName);
		} catch (Exception e) {
			System.err.println(">>> 初始化失敗: " + e.getMessage());
			this.validModelName = "models/gemini-2.5-flash"; // 預設改為 2.5
		}
	}

	// 1. 生成商品描述
	@Transactional(rollbackFor = Exception.class)
	public AiRes generateProductDesc(AiProductDescReq req, MultipartFile file, HttpSession session) {
	    // 1. 權限檢查
	    AiRes authRes = validateAdminAccess(session);
	    if (authRes != null)
	        return new AiRes(authRes.getCode(), authRes.getMessage());
	    
	    try {
			// 2. 處理圖片
			String mimeType = file.getContentType();
			if (mimeType == null || !mimeType.startsWith("image/")) {
				mimeType = "image/jpeg";
			}
			
			byte[] imageBytes = file.getBytes();
			
		    // 2. 構造更強大的指令 (加入 Style)
		    String prompt = String.format(
		            "你是一位專業的五星級餐廳菜單文案撰寫員。請為以下商品撰寫一段誘人的短文案。\n\n" +
		            "【商品資訊】\n" +
		            "● 商品名稱：%s\n" +
		            "● 商品類別：%s\n" +
		            "● 烹飪風格：%s\n\n" +
		            "【撰寫規則】\n" +
		            "1. 字數限制：嚴格控制在 30 至 50 個中文字之間。\n" +
		            "2. 文案語調：請根據『%s』的風格，精確描述食材口感、香氣與味覺層次。\n" + 
		            "3. 禁令：直接輸出文案內容。禁止任何開場白（如：這是一段...）、禁止結尾客套話、禁止使用引號。\n" +
		            "4. 目標：讓顧客讀完後立刻想點這道菜。",
		            req.getProductName(), req.getCategory(), req.getStyle(), req.getStyle()
		    );
	   
	    	String content = callAiApiWithImage(prompt, imageBytes, mimeType,productKey);
	        int productId = req.getProductid();

	        // 3. 紀錄 AI 使用日誌
	        saveAiLog(AiType.PRODUCT_DESC, productId, content);

	        return new AiRes(ReplyMessage.SUCCESS.getCode(),
	                ReplyMessage.SUCCESS.getMessage(), content);
	                
	    } catch (Exception e) {
	        // 記得！既然你有全域攔截，這裡可以 throw 或手動回滾
	        log.error("AI 生成文案失敗：", e);
	        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	        return new AiRes(ReplyMessage.SYSTEM_ERROR.getCode(), "AI 服務暫時無法連線，請稍等 3 分鐘再嘗試");
	    }
	}

	// 2. 生成活動文案 (含圖片)
	@Transactional(rollbackFor = Exception.class)
	public AiRes generatePromoCopy(AiPromotionsReq req, MultipartFile file, HttpSession session) {
		AiRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return new AiRes(authRes.getCode(), authRes.getMessage());

		try {
			// 1. 整理商品資訊 (這是最關鍵的一步)
			StringBuilder productDetails = new StringBuilder();
			if (req.getPromotionItems() != null) {
				for (AiPromotionsReq.PromotionItem item : req.getPromotionItems()) {
					Products p = productsDao.findById(item.getProductId());
					if (p != null) {
						productDetails.append("- 商品：").append(p.getName()).append("\n").append("  描述：")
								.append(p.getDescription()).append("\n").append("  門檻：滿額 ").append(item.getFullAmount())
								.append(" 元即可獲得\n");
					}
				}
			}

			// 2. 處理圖片
			String mimeType = file.getContentType();
			if (mimeType == null || !mimeType.startsWith("image/")) {
				mimeType = "image/jpeg";
			}
			byte[] imageBytes = file.getBytes();

			// 3. 更新 Prompt，注入商品細節
			String prompt = String.format("請根據圖片內容、活動名稱『%s』以及以下商品活動細節，撰寫一篇吸睛的社群活動宣傳文案。\n\n" //
					+ "【活動商品細節】:\n%s\n\n" + "要求：\n" //
					+ "1. 風格：活潑、有吸引力，能引發互動。\n" //
					+ "2. 字數：嚴格控制在 80 個中文字以內 (約 3-4 句短句)。\n" //
					+ "3. 內容：突顯活動亮點與吸引人處，結合商品特色與門檻優惠。\n" //
					+ "4. 格式：直接輸出文案內容，不要包含任何開場白。", //
					req.getActivityName(), productDetails.toString());

			String content = callAiApiWithImage(prompt, imageBytes, mimeType,promoKey);

			// 存檔與回傳
			saveAiLog(AiType.PROMO_COPY, req.getPromotionsId(), content);

			return new AiRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), content);

		} catch (IOException e) {
			return new AiRes(ReplyMessage.SYSTEM_ERROR.getCode(),
					ReplyMessage.SYSTEM_ERROR.getMessage() + e.getMessage());
		} catch (Exception e) {
			return new AiRes(ReplyMessage.SYSTEM_ERROR.getCode(), "處理失敗: " + e.getMessage());
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

	// 呼叫純文字 API - 商品應該也得要包含圖片，而非純文字
	//	private String callAiApi(String prompt) {
	//		int maxRetries = 3; // 設定最大重試次數
	//		Exception lastException = null;
	//
	//		for (int i = 0; i < maxRetries; i++) {
	//			try {
	//				// 1. 組建請求目標 URL，將 API Key 拼接在路徑參數中，告訴 Google 我們是誰 ==> 這個在哪邊可以看到相關文件
	//				String url = "https://generativelanguage.googleapis.com/v1beta/" + validModelName //
	//						+ ":generateContent?key=" + productKey;
	//
	//				// 2. 建立一個 Map 來存放 JSON 資料
	//				// Gemini API 規定的 JSON 格式是 {"contents": [{"parts": [{"text": "你的問題"}]}]}
	//				// Map.of 是 Java 9 之後提供的快速產生不可變 Map 的語法
	//				Map<String, Object> requestBody = Map.of("contents", //
	//						List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
	//
	//				// 3. 開始發送 POST 請求
	//				// restClient.post()：建立一個 POST 方法的請求物件
	//				// .uri(url)：設定發送目的地
	//				// .body(requestBody)：將上面的 Map 自動序列化為 JSON 字串並塞入 Request Body
	//				// .retrieve()：正式啟動 HTTP 請求
	//				// .body(...)：這是關鍵，將回傳的 JSON 反序列化回 Java 物件
	//				// ParameterizedTypeReference 的用途是 類型擦除 (Type Erasure)
	//				Map<String, Object> response = restClient.post().uri(url).body(requestBody).retrieve()
	//						// 關鍵點：ParameterizedTypeReference<Map<String, Object>>() {}
	//						// 這是一個匿名類別的實例，用來保存泛型資訊，避免 Java 發生類型擦除
	//						.body(new ParameterizedTypeReference<Map<String, Object>>() {
	//						});
	//
	//				// 4. 將取得的 Map 傳入解析方法，取出裡面的文字內容
	//				return extractTextFromResponse(response);
	//
	//			} catch (Exception e) {
	//				lastException = e;
	//				System.err.println(">>> AI 呼叫失敗，正在進行第 " + (i + 1) + " 次重試... 錯誤: " + e.getMessage());
	//				try {
	//					Thread.sleep(1000); // 失敗後暫停 1 秒再重試
	//				} catch (InterruptedException ie) {
	//					Thread.currentThread().interrupt();
	//				}
	//			}
	//		}
	//
	//		// 如果三次都失敗，拋出最後一次記錄到的錯誤
	//		throw new RuntimeException("AI 服務多次呼叫失敗，請稍後再試。最後一次錯誤: " + lastException.getMessage());
	//	}

	// 呼叫 AI (用圖片 + 文字)
	private String callAiApiWithImage(String prompt, byte[] imageBytes, String mimeType, String apiKey) {
		int maxRetries = 3; // 設定最大重試次數
		Exception lastException = null;

		for (int i = 0; i < maxRetries; i++) {
			try {
				// 1. 設定 API 端點 (與純文字 API 相同)
				String url = "https://generativelanguage.googleapis.com/v1beta/" + validModelName //
						+ ":generateContent?key=" + apiKey;

				// 2. Base64 編碼
				// JSON 本身是文字格式，無法直接承載「二進位 (Binary)」的圖片原始資料
				// 因此必須將圖片轉成 Base64 字串，才能安全地放入 JSON 中傳輸
				String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

				// 3. 組裝多模態 (Multimodal) JSON Payload
				// Gemini API 的格式規定：在 parts 陣列中，可以同時存在 text 物件與 inline_data 物件
				// 這就是為什麼我們把 prompt 和 圖片數據放在同一個 List 裡面
				Map<String, Object> requestBody = Map.of("contents", //
						List.of(Map.of("parts", List.of(Map.of("text", prompt), // 文字部分
								Map.of("inline_data", Map.of("mime_type", //
										mimeType, // 動態設定圖片格式 (例如 image/jpeg)
										"data", base64Image // 剛才轉好的 Base64 字串
								))))));

				// 4. 發送請求並接收回應 (同樣使用 ParameterizedTypeReference 確保型別安全)
				Map<String, Object> response = restClient.post().uri(url).body(requestBody).retrieve()
						.body(new ParameterizedTypeReference<Map<String, Object>>() {
						});

				// 5. 解析回應
				return extractTextFromResponse(response);

			} catch (Exception e) {
				lastException = e;
				log.warn(">>> AI 呼叫失敗，重試中... ({}/{})", i + 1, maxRetries);
				try {
					Thread.sleep(3000); // 失敗後暫停 3 秒再重試
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}

		// 如果三次都失敗，拋出最後一次記錄到的錯誤
		throw new RuntimeException("AI 圖片服務多次呼叫失敗，請稍後再試。最後一次錯誤: " + lastException.getMessage());
	}

	// 確認解析狀況
	@SuppressWarnings("unchecked") // 告訴編譯器：我清楚這裡在做 Map 強制轉型，這不是錯誤
	private String extractTextFromResponse(Map<String, Object> response) {
		try {
			// 1. 取得 "candidates" 列表 (AI 可能會回傳多個候選答案，通常 index 0 就是最佳答案)
			List<Map<String, Object>> candidates = //
					(List<Map<String, Object>>) response.get("candidates");

			// 2. 防呆檢查：如果 API 回傳沒有候選內容，直接回報錯誤，避免程式崩潰
			if (candidates == null || candidates.isEmpty()) {
				return "AI 回應異常 (無內容回傳)";
			}

			// 3. 取得 candidates[0] 裡面的 "content" 物件 (這層包含了角色與內容)
			Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

			// 4. 取得 "parts" 列表 (Gemini 的設計中，一個回應可以包含多個部分，例如文字 + 圖片資訊)
			List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

			// 5. 取出 parts[0] 中的 "text" 欄位，這就是 AI 生成的純文字結果
			return (String) parts.get(0).get("text");
			/*
			 * response：拿到整個巨大包裹（整個 JSON）。 .get("candidates")：打開外包裝，拿出候選清單（List）。
			 * .get(0)：取出第一個最優質的回應（Map）。 .get("content")：拿出內容層（Map）。
			 * .get("parts")：取出組件清單（List）。 .get(0)：拿出第一個組件（通常就是我們的純文字）。
			 * .get("text")：最後這一步，才終於取出了你心心念念的那一行文字！
			 */
		} catch (Exception e) {
			// 6. 萬一上面的取值過程中發生任何錯誤 (例如結構改變、取值為 null 等)，統一在此攔截
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