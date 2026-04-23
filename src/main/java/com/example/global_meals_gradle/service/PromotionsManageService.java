package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.dao.PromotionsDao;
import com.example.global_meals_gradle.dao.PromotionsGiftsDao;
import com.example.global_meals_gradle.entity.Promotions;
import com.example.global_meals_gradle.entity.PromotionsGifts;
import com.example.global_meals_gradle.req.PromotionsManageReq;
import com.example.global_meals_gradle.res.PromotionsListRes;
import com.example.global_meals_gradle.vo.GiftDetailVo;
import com.example.global_meals_gradle.vo.PromotionDetailVo;

@Service
public class PromotionsManageService {

	// 操作 promotions 表：新增活動、刪除活動、開關活動、確認活動是否存在
	@Autowired
	private PromotionsDao promotionsDao;

	// 操作 promotions_gifts 表：新增贈品、關閉所有贈品、依 promotions_id 刪除所有贈品
	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;

	// 查詢 products 表：確認 gift_product_id 是否有對應的商品存在
	@Autowired
	private ProductsDao productsDao;

	// =============================================
	// 方法零：查詢所有促銷活動（含各自的贈品清單）
	// =============================================

	/**
	 * 取得所有促銷活動及其贈品清單（供管理端 GET /promotions/list 使用）
	 *
	 * 流程：
	 *   1. 從 promotions 表撈出全部活動（不篩選，包含啟用與停用）
	 *   2. 針對每個活動，查 promotions_gifts 表取得底下所有贈品規則
	 *   3. 每筆贈品用 gift_product_id 去 products 表查商品名稱
	 *   4. 組裝成 PromotionDetailVo 清單後封裝回傳
	 *
	 * 不使用 @Transactional（只有讀取，不需要交易保護）
	 *
	 * @return PromotionsListRes 包含 code、message 和完整活動清單
	 */
	public PromotionsListRes getList() {
		return getList(null); // 管理端不傳 globalAreaId → 回傳所有活動
	}

	/**
	 * 依分店 ID 篩選活動清單
	 * globalAreaId = null → 管理端全查（findAll）
	 * globalAreaId 有值  → 客戶端：回傳全球活動 + 該分店專屬活動
	 */
	public PromotionsListRes getList(Integer globalAreaId) {

		List<Promotions> allPromotions;
		if (globalAreaId != null && globalAreaId > 0) {
			// 客戶端：只回傳「全球活動 OR 分店專屬活動」且目前有效的
			allPromotions = promotionsDao.findActiveByAreaOrGlobal(globalAreaId);
		} else {
			// 管理端：不篩選，回傳全部（含停用）
			allPromotions = promotionsDao.findAll();
		}

		// 準備組裝回傳用的 VO 清單
		List<PromotionDetailVo> result = new ArrayList<>();

		for (Promotions p : allPromotions) {

			// 把 Promotions entity 的欄位逐一填入 PromotionDetailVo
			PromotionDetailVo vo = new PromotionDetailVo();
			vo.setId(p.getId());
			vo.setName(p.getName());
			vo.setNameJP(p.getNameJP());
			vo.setNameKR(p.getNameKR());
			vo.setGlobalAreaId(p.getGlobalAreaId());
			vo.setStartTime(p.getStartTime());
			vo.setEndTime(p.getEndTime());
			vo.setActive(p.isActive());
			vo.setDescription(p.getDescription());
			// 有圖片時轉為 Base64 字串；無圖片為 null
			if (p.getPromotionImg() != null && p.getPromotionImg().length > 0) {
				vo.setPromotionImg(Base64.getEncoder().encodeToString(p.getPromotionImg()));
			}

			// 查此活動底下所有贈品規則（包含停用的，讓管理端看到完整狀態）
			// findByPromotionsId 使用 native SQL：SELECT * FROM promotions_gifts WHERE promotions_id = ?
			List<PromotionsGifts> gifts = promotionsGiftsDao.findByPromotionsId(p.getId());

			// 把每筆 PromotionsGifts entity 轉換成 GiftDetailVo
			List<GiftDetailVo> giftVos = new ArrayList<>();
			for (PromotionsGifts g : gifts) {

				GiftDetailVo gvo = new GiftDetailVo();
				gvo.setId(g.getId());                     // 贈品規則 ID
				gvo.setFullAmount(g.getFullAmount());      // 消費門檻
				gvo.setQuantity(g.getQuantity());          // 剩餘庫存（-1 = 無限）
				gvo.setGiftProductId(g.getGiftProductId()); // 對應商品 ID

				// 用 gift_product_id 查商品名稱（避免把 MEDIUMBLOB 圖片也撈出來）
				// findNameById 查不到時回傳 null，給預設值 "活動贈品"
				String name = productsDao.findNameById(g.getGiftProductId());
				gvo.setProductName(name != null ? name : "活動贈品");

				gvo.setActive(g.isActive()); // 是否還有效（庫存耗盡時為 false）

				giftVos.add(gvo);
			}

			// 把組好的贈品清單放進活動 VO
			vo.setGifts(giftVos);

			result.add(vo);
		}

		// 用 SUCCESS 封裝並回傳（code = 200, message = "Success!!"）
		return new PromotionsListRes(
				ReplyMessage.SUCCESS.getCode(),
				ReplyMessage.SUCCESS.getMessage(),
				result);
	}

	// =============================================
	// 方法一：新增贈品至活動（寫入 promotions_gifts 表）
	// =============================================

	/**
	 * 新增一筆贈品至指定促銷活動
	 *
	 * 驗證規則：
	 *   1. promotionsId 必須在 promotions 表中存在
	 *   2. fullAmount 必須大於 0
	 *   3. quantity 不能為 0（-1 表示無限，>= 1 表示有限）
	 *   4. giftProductId 必須在 products 表中有對應的名稱
	 *
	 * is_active 新增時固定為 true（1），不由呼叫方決定
	 *
	 * @param req 包含 promotionsId、fullAmount、quantity、giftProductId
	 */
	@Transactional
	public void addPromotionGift(PromotionsManageReq req) {

		// 驗證 promotionsId 是否存在於 promotions 表
		// existsById 是 JPA 內建方法，回傳 boolean
		if (!promotionsDao.existsById(req.getPromotionsId())) {
			throw new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage());
		}

		// 驗證 fullAmount 必須大於 0（null 或 <= 0 都不合法）
		if (req.getFullAmount() == null || req.getFullAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new RuntimeException(ReplyMessage.PROMOTION_GIFT_PARAM_ERROR.getMessage());
		}

		// 驗證 quantity 不能為 0
		//   -1  → 無限，合法
		//   >= 1 → 有限，合法
		//   0   → 不合法
		if (req.getQuantity() == 0) {
			throw new RuntimeException(ReplyMessage.PROMOTION_GIFT_PARAM_ERROR.getMessage());
		}

		// 驗證 giftProductId 必須 >= 1（0 或負數無意義）
		if (req.getGiftProductId() < 1) {
			throw new RuntimeException(ReplyMessage.PROMOTION_GIFT_PARAM_ERROR.getMessage());
		}

		// 驗證 giftProductId 在 products 表中有對應的商品名稱
		// findNameById 查不到時回傳 null
		String productName = productsDao.findNameById(req.getGiftProductId());
		if (productName == null) {
			throw new RuntimeException(ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}

		// 組成 PromotionsGifts entity 準備寫入
		PromotionsGifts gift = new PromotionsGifts();
		gift.setPromotionsId(req.getPromotionsId());
		gift.setFullAmount(req.getFullAmount());
		gift.setQuantity(req.getQuantity());
		gift.setGiftProductId(req.getGiftProductId());

		// 新增時固定啟用
		gift.setActive(true);

		// JPA save() 寫入，id 由 AUTO_INCREMENT 自動產生
		promotionsGiftsDao.save(gift);
	}

	// =============================================
	// 方法二：開關促銷活動
	// =============================================

	/**
	 * 切換促銷活動的啟用狀態
	 *
	 * 流程：
	 *   1. 確認這個 promotionsId 存在
	 *   2. 關閉（active = false）→ 同步把底下所有 promotions_gifts.is_active 設為 0
	 *   3. 開啟（active = true）  → 只改 promotions.is_active，贈品不動
	 *
	 * @param req 包含 promotionsId、active
	 */
	@Transactional
	public void togglePromotion(PromotionsManageReq req) {

		// 確認這個活動存在，不存在就擋下
		Promotions promotion = promotionsDao.findById(req.getPromotionsId())
				.orElseThrow(() -> new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage()));

		// 更新 promotions.is_active
		promotion.setActive(req.isActive());
		promotionsDao.save(promotion);

		// 關閉活動時，同步把底下所有贈品的 is_active 設為 0
		// 開啟活動時不動贈品，讓前端自己決定要開哪些贈品
		if (!req.isActive()) {
			promotionsGiftsDao.deactivateAllByPromotionsId(req.getPromotionsId());
		}
	}

	// =============================================
	// 方法三：刪除促銷活動（真刪除）
	// =============================================

	/**
	 * 刪除一筆促銷活動，並同時刪除其底下所有贈品
	 *
	 * 流程：
	 *   1. 確認這個 promotionsId 存在
	 *   2. 先刪 promotions_gifts 中所有 promotions_id 符合的資料
	 *   3. 再刪 promotions 本身
	 *
	 * 步驟 2 必須在步驟 3 之前，否則若有 FK 限制會報錯
	 * （目前 DB 沒設 FK，但順序正確是好習慣）
	 *
	 * @param promotionsId 要刪除的促銷活動 ID（promotions.id）
	 */
	@Transactional
	public void deletePromotion(int promotionsId) {

		// 確認這個活動存在，不存在就擋下
		if (!promotionsDao.existsById(promotionsId)) {
			throw new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage());
		}

		// 先刪除這個活動底下所有的贈品（promotions_gifts 表）
		promotionsGiftsDao.deleteByPromotionsId(promotionsId);

		// 再刪除促銷活動本身（promotions 表）
		promotionsDao.deleteById(promotionsId);
	}

	// =============================================
	// 方法三點五：更新活動文案與封面圖片
	// =============================================

	/**
	 * 更新指定促銷活動的 description（文案）與 promotion_img（封面圖）
	 *
	 * 流程：
	 *   1. 確認 promotionsId 存在
	 *   2. 更新 description
	 *   3. 若 promotionImg 非空，解 Base64 後寫入 DB；否則不動圖片
	 *
	 * @param req 包含 promotionsId、description、promotionImg（Base64 或 data URL）
	 */
	@Transactional
	public void updatePromotionInfo(PromotionsManageReq req) {

		Promotions p = promotionsDao.findById(req.getPromotionsId())
				.orElseThrow(() -> new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage()));

		p.setDescription(req.getDescription());

		String b64 = req.getPromotionImg();
		if (b64 != null && !b64.isBlank()) {
			// 去除 data URL 前綴（例：data:image/jpeg;base64,）
			if (b64.contains(",")) {
				b64 = b64.substring(b64.indexOf(",") + 1);
			}
			p.setPromotionImg(Base64.getDecoder().decode(b64));
		}

		promotionsDao.save(p);
	}

	
	// =============================================
	// 方法三點七：更新活動基本資訊（名稱、日期）
	// =============================================
	@Transactional
	public void updatePromotionBasic(PromotionsManageReq req) {

	    Promotions p = promotionsDao.findById(req.getPromotionsId())
	            .orElseThrow(() -> new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage()));

	    // 名稱不能為空
	    if (req.getName() == null || req.getName().isBlank()) {
	        throw new RuntimeException(ReplyMessage.PROMOTION_NAME_ERROR.getMessage());
	    }

	    // 結束日期必須晚於開始日期
	    if (req.getStartTime() != null && req.getEndTime() != null
	            && !req.getEndTime().isAfter(req.getStartTime())) {
	        throw new RuntimeException(ReplyMessage.PROMOTION_DATE_ERROR.getMessage());
	    }

	    p.setName(req.getName());
	    if (req.getStartTime() != null) p.setStartTime(req.getStartTime());
	    if (req.getEndTime() != null)   p.setEndTime(req.getEndTime());

	    promotionsDao.save(p);
	}
	
	
	// =============================================
	// 方法四：一次建立促銷活動 + 贈品規則（POST /promotions/create 使用）
	// =============================================

	/**
	 * 新增促銷活動，並選擇性地同時新增一筆贈品規則
	 *
	 * 與 addPromotion() + addPromotionGift() 的差別：
	 *   - 兩個步驟在同一個 @Transactional 裡，任一失敗整筆一起 rollback
	 *   - 若前端沒有要加贈品（giftProductId = 0），只建立活動
	 *   - 若前端有帶贈品欄位（giftProductId > 0），一次完成活動 + 贈品
	 *
	 * 驗證規則（活動）：
	 *   1. startTime 不能早於今天
	 *   2. endTime 必須晚於 startTime
	 *
	 * 驗證規則（贈品，只在 giftProductId > 0 時執行）：
	 *   3. fullAmount 必須大於 0
	 *   4. quantity 不能為 0（-1 = 無限，>= 1 = 有限）
	 *   5. giftProductId 在 products 表中必須有對應商品
	 *
	 * @param req 包含活動欄位（name, startTime, endTime）
	 *            以及可選的贈品欄位（giftProductId, fullAmount, quantity）
	 */
	// =============================================
	// 方法五：上傳促銷活動圖片（POST /promotions/uploadImage/{id} 使用）
	// =============================================

	/**
	 * 將圖片存入指定促銷活動
	 *
	 * @param promotionsId 目標活動 ID
	 * @param imageBytes   前端上傳的圖片位元組
	 */
	@Transactional
	public void uploadImage(int promotionsId, byte[] imageBytes) {

		Promotions promotion = promotionsDao.findById(promotionsId)
				.orElseThrow(() -> new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage()));

		promotion.setPromotionImg(imageBytes);
		promotionsDao.save(promotion);
	}

	// =============================================
	// 方法六：取得促銷活動圖片（GET /promotions/image/{id} 使用）
	// =============================================

	/**
	 * 取出指定促銷活動的圖片位元組
	 *
	 * 回傳 null 表示該活動沒有圖片
	 *
	 * @param promotionsId 目標活動 ID
	 * @return 圖片 byte[]，或 null
	 */
	public byte[] getImage(int promotionsId) {

		Promotions promotion = promotionsDao.findById(promotionsId)
				.orElseThrow(() -> new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage()));

		return promotion.getPromotionImg();
	}

	// =============================================
	// 方法四：一次建立促銷活動 + 贈品規則（POST /promotions/create 使用）
	// =============================================

	@Transactional
	public int createPromotionWithGift(PromotionsManageReq req) {

		// ── 步驟一：驗證並建立促銷活動 ──

		// 活動名稱不能為空或空白
		if (req.getName() == null || req.getName().isBlank()) {
			throw new RuntimeException(ReplyMessage.PROMOTION_NAME_ERROR.getMessage());
		}

		// startTime / endTime 不能為 null（前端必須傳入）
		if (req.getStartTime() == null || req.getEndTime() == null) {
			throw new RuntimeException(ReplyMessage.PROMOTION_DATE_ERROR.getMessage());
		}

		// 開始日期不能早於今天（業務規則：不允許建立已過期的活動）
		if (req.getStartTime().isBefore(LocalDate.now())) {
			throw new RuntimeException(ReplyMessage.PROMOTION_DATE_ERROR.getMessage());
		}

		// 結束日期必須晚於開始日期（防止建立無效時間區間）
		if (!req.getEndTime().isAfter(req.getStartTime())) {
			throw new RuntimeException(ReplyMessage.PROMOTION_DATE_ERROR.getMessage());
		}

		// 組裝 Promotions entity 準備寫入
		Promotions promotion = new Promotions();
		promotion.setName(req.getName());
		promotion.setNameJP(req.getNameJP());
		promotion.setNameKR(req.getNameKR());
		// globalAreaId = 0 或 null → 全球活動（老闆建立）; > 0 → 分店專屬
		Integer areaId = req.getGlobalAreaId();
		promotion.setGlobalAreaId((areaId != null && areaId > 0) ? areaId : null);
		promotion.setStartTime(req.getStartTime());
		promotion.setEndTime(req.getEndTime());
		promotion.setActive(true); // 新建時固定啟用

		// save() 寫入並回傳帶有 AUTO_INCREMENT id 的 entity
		// 後續新增贈品時需要用這個 id（promotions_id FK）
		Promotions saved = promotionsDao.save(promotion);

		// ── 步驟二：若有帶贈品欄位，同步建立贈品規則 ──

		// giftProductId = 0 表示前端沒有要加贈品，跳過整段
		if (req.getGiftProductId() > 0) {

			// 消費門檻必須大於 0（0 或負數沒有意義）
			if (req.getFullAmount() == null || req.getFullAmount().compareTo(BigDecimal.ZERO) <= 0) {
				throw new RuntimeException(ReplyMessage.PROMOTION_GIFT_PARAM_ERROR.getMessage());
			}

			// quantity = 0 不合法（-1 = 無限，>= 1 = 有限，0 = 無效）
			if (req.getQuantity() == 0) {
				throw new RuntimeException(ReplyMessage.PROMOTION_GIFT_PARAM_ERROR.getMessage());
			}

			// 確認 giftProductId 在 products 表中有對應的商品名稱
			// findNameById 查不到時回傳 null
			String productName = productsDao.findNameById(req.getGiftProductId());
			if (productName == null) {
				throw new RuntimeException(ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
			}

			// 組裝 PromotionsGifts entity
			PromotionsGifts gift = new PromotionsGifts();
			gift.setPromotionsId(saved.getId()); // 使用剛才儲存後自動產生的活動 ID
			gift.setFullAmount(req.getFullAmount());
			gift.setQuantity(req.getQuantity());
			gift.setGiftProductId(req.getGiftProductId());
			gift.setActive(true); // 新建時固定啟用

			// 寫入 promotions_gifts 表，id 由 @GeneratedValue 自動產生
			promotionsGiftsDao.save(gift);
		}
		 return saved.getId(); // ← 新增這行
	}

}
