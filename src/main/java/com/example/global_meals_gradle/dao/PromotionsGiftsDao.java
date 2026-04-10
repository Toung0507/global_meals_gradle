package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.global_meals_gradle.entity.PromotionsGifts;

public interface PromotionsGiftsDao extends JpaRepository<PromotionsGifts, Integer> {

	/**
	 * 根據原始總金額，找出「金額最高門檻且達標」的那一筆贈品活動
	 *
	 * 條件說明： :total >= gifts.full_amount → 消費金額有達到這個贈品的門檻 gifts.is_active = 1 →
	 * 這筆贈品活動本身是啟用的（未手動下架） prom.is_active = 1 → 對應的促銷活動是啟用的 prom.start_time <=
	 * CURRENT_DATE → 促銷活動已經開始 prom.end_time >= CURRENT_DATE → 促銷活動還沒結束
	 * (gifts.quantity = -1 OR gifts.quantity > 0) → quantity = -1 表示無限供應，> 0 表示還有庫存
	 * 兩種情況都可以送贈品，= 0 表示已送完不能送
	 *
	 * ORDER BY gifts.full_amount DESC LIMIT 1： 取「門檻金額最高的那一筆」 例如：滿 300 送 A、滿 1000 送
	 * B，花 1200 只拿 B（取最高門檻）
	 *
	 * 回傳 PromotionsGifts entity（不含 products.name，名稱另外由 ProductsTempDao 查）
	 */
	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
			+ "JOIN promotions AS prom ON gifts.promotions_id = prom.id " + "WHERE :total >= gifts.full_amount "
			+ "AND gifts.is_active = 1 " + "AND prom.is_active = 1 " + "AND prom.start_time <= CURRENT_DATE "
			+ "AND prom.end_time >= CURRENT_DATE " + "AND (gifts.quantity = -1 OR gifts.quantity > 0) "
			+ "ORDER BY gifts.full_amount DESC " + "LIMIT 1", nativeQuery = true)
	PromotionsGifts findTopQualifiedGift(@Param("total") BigDecimal total);

	/* 核心邏輯：取得所有目前上架的有效期內的活動的上架中的贈品門檻 /規則*/
//	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
//		    + "JOIN promotions AS prom ON gifts.promotions_id = prom.id "
//		    + "WHERE gifts.is_active = 1 "
//		    + "AND prom.is_active = 1 "
//		    + "AND prom.start_time <= CURRENT_DATE "
//		    + "AND prom.end_time >= CURRENT_DATE", nativeQuery = true)
//		public List<PromotionsGifts> findAllActiveGifts();

	/* 根據商品id取的門檻資料 */
	@Query(value = "SELECT full_amount FROM promotions_gifts WHERE gift_product_id = ?1 AND is_active = 1", nativeQuery = true)
	public BigDecimal findFullAmountByGiftProductId(int giftProductId);

	/*
	 * 根據「贈品商品 ID」找到對應的目前上架的規則。用途：在 getCartView() 步驟3 驗證已選贈品是否還有效。
	 * 例如：使用者選了大盤雞（giftProductId = 101），後端確認這條規則是否仍在有效期間內且啟用。
	 *
	 * ⚠️ 加上 ORDER BY full_amount ASC LIMIT 1 的原因：
	 *    若同一個贈品商品（例如大盤雞）被設定在「夏日祭典」和「週年慶」兩個活動裡，
	 *    SQL 會回傳兩筆資料，JPA 把多筆資料塞進單一物件時會拋出
	 *    IncorrectResultSizeDataAccessException（預期 1 筆，實際 N 筆），導致程式崩潰。
	 *    加上 LIMIT 1 後只取一筆；ORDER BY full_amount ASC 優先取門檻最低的那條，
	 *    確保消費者只要達到最低門檻的活動就算有效，不會因為取到高門檻的規則而被誤判失效。
	 */
	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
		    + "JOIN promotions AS prom ON gifts.promotions_id = prom.id "
		    + "WHERE gifts.gift_product_id = ?1 "
		    + "AND gifts.is_active = 1 "
		    + "AND prom.is_active = 1 "
		    + "AND prom.start_time <= CURRENT_DATE "
		    + "AND prom.end_time >= CURRENT_DATE "
		    + "ORDER BY gifts.full_amount ASC "
		    + "LIMIT 1", nativeQuery = true)
		PromotionsGifts findActiveRuleByGiftProductId(int giftProductId);

// 根據「活動 ID」撈出這個活動底下所有上架的贈品規則
// 使用時機：CartService 步驟4，確認了某個活動後，找這個活動底下有哪些贈品可以送
	@Query(value = "SELECT * FROM promotions_gifts WHERE promotions_id = ?1 AND is_active = 1",
	       nativeQuery = true)
	List<PromotionsGifts> findGiftsByPromotionId(int promotionId);

// 根據「贈品規則 ID（主鍵）」找到對應的上架有效規則
// 使用時機：selectGift()，前端傳來 giftRuleId，後端用主鍵精準定位這條規則
// 同時 JOIN promotions 確認這條規則對應的活動是否仍在有效期間內且啟用
// 任一條件不符合就回傳 null
	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
	        + "JOIN promotions AS prom ON gifts.promotions_id = prom.id "
	        + "WHERE gifts.id = ?1 "
	        + "AND gifts.is_active = 1 "
	        + "AND prom.is_active = 1 "
	        + "AND prom.start_time <= CURRENT_DATE "
	        + "AND prom.end_time >= CURRENT_DATE",
	        nativeQuery = true)
	PromotionsGifts findActiveRuleByGiftRuleId(int giftRuleId);
}