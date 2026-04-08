package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.PromotionsDao;
import com.example.global_meals_gradle.dao.PromotionsGiftsDao;
import com.example.global_meals_gradle.dao.ProductsTempDao;
import com.example.global_meals_gradle.entity.Promotions;
import com.example.global_meals_gradle.entity.PromotionsGifts;
import com.example.global_meals_gradle.req.PromotionsManageReq;

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
	private ProductsTempDao productsTempDao;

	// =============================================
	// 方法一：新增促銷活動（寫入 promotions 表）
	// =============================================

	/**
	 * 新增一筆促銷活動
	 *
	 * 驗證規則（在 Service 裡做，annotation 做不到的邏輯）：
	 *   1. startTime 不能早於今天
	 *   2. endTime 必須晚於 startTime
	 *
	 * is_active 新增時固定為 true（1），不由呼叫方決定
	 *
	 * @param req 包含 name、startTime、endTime
	 */
	@Transactional
	public void addPromotion(PromotionsManageReq req) {

		// 驗證 startTime 不能早於今天
		if (req.getStartTime().isBefore(LocalDate.now())) {
			throw new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage());
		}

		// 驗證 endTime 必須晚於 startTime
		if (!req.getEndTime().isAfter(req.getStartTime())) {
			throw new RuntimeException(ReplyMessage.PROMOTION_NOT_FOUND.getMessage());
		}

		// 組成 Promotions entity 準備寫入
		Promotions promotion = new Promotions();
		promotion.setName(req.getName());
		promotion.setStartTime(req.getStartTime());
		promotion.setEndTime(req.getEndTime());

		// 新增時固定啟用，前端之後可透過 togglePromotion 切換 is_active
		promotion.setActive(true);

		// JPA save() 會因為 @GeneratedValue 自動帶入 AUTO_INCREMENT 產生的 id
		promotionsDao.save(promotion);
	}

	// =============================================
	// 方法二：新增贈品至活動（寫入 promotions_gifts 表）
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

		// 驗證 fullAmount 必須大於 0
		if (req.getFullAmount() == null || req.getFullAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
			throw new RuntimeException(ReplyMessage.PROMOTION_GIFTS_NOT_FOUND.getMessage());
		}

		// 驗證 quantity 不能為 0
		//   -1  → 無限，合法
		//   >= 1 → 有限，合法
		//   0   → 不合法
		if (req.getQuantity() == 0) {
			throw new RuntimeException(ReplyMessage.PROMOTION_GIFTS_NOT_FOUND.getMessage());
		}

		// 驗證 giftProductId 在 products 表中有對應的商品名稱
		// findNameById 查不到時回傳 null
		String productName = productsTempDao.findNameById(req.getGiftProductId());
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
	// 方法三：開關促銷活動
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
	// 方法四：刪除促銷活動（真刪除）
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

}
