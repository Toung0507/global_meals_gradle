package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.global_meals_gradle.entity.PromotionsGifts;

public interface PromotionsGiftsDao extends JpaRepository<PromotionsGifts, Integer> {

	/**
	 * 根據原始總金額，找出所有達標的贈品活動（讓使用者從中選一個）
	 *
	 * 條件說明： :total >= gifts.full_amount → 消費金額有達到這個贈品的門檻 gifts.is_active = 1 →
	 * 這筆贈品活動本身是啟用的（未手動下架） prom.is_active = 1 → 對應的促銷活動是啟用的 prom.start_time <=
	 * CURRENT_DATE → 促銷活動已經開始 prom.end_time >= CURRENT_DATE → 促銷活動還沒結束
	 * (gifts.quantity = -1 OR gifts.quantity > 0) → quantity = -1 表示無限供應 quantity >
	 * 0 表示還有庫存 quantity = 0 表示已送完，不能選
	 *
	 * ORDER BY gifts.full_amount DESC： 金額高的排前面，讓使用者看到最高等級的贈品優先顯示
	 *
	 * 回傳 List<PromotionsGifts>（名稱另外由 ProductsTempDao 查）
	 */
	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
			+ "JOIN promotions AS prom ON gifts.promotions_id = prom.id " + "WHERE :total >= gifts.full_amount "
			+ "AND gifts.is_active = 1 " + "AND prom.is_active = 1 " + "AND prom.start_time <= CURRENT_DATE "
			+ "AND prom.end_time >= CURRENT_DATE " + "AND (gifts.quantity = -1 OR gifts.quantity > 0) "
			+ "ORDER BY gifts.full_amount DESC", nativeQuery = true)
	List<PromotionsGifts> findAllQualifiedGifts(@Param("total") BigDecimal total);

	/**
	 * 驗證使用者選擇的贈品是否合法
	 *
	 * 條件說明： gifts.id = :selectedGiftId → 就是使用者選的那一筆 :total >= gifts.full_amount →
	 * 消費金額還是要達到這筆贈品的門檻 防止使用者查完清單後金額被改動再來結帳 gifts.is_active = 1 → 這筆贈品活動還是啟用的
	 * prom.is_active = 1 → 對應的促銷活動還是啟用的 prom.start_time <= CURRENT_DATE → 促銷活動還在期間內
	 * prom.end_time >= CURRENT_DATE → 促銷活動還沒結束 (gifts.quantity = -1 OR
	 * gifts.quantity > 0) → 還有庫存
	 *
	 * 回傳 null 表示不合法，不為 null 表示合法可以扣減
	 */
	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
			+ "JOIN promotions AS prom ON gifts.promotions_id = prom.id " + "WHERE gifts.id = :selectedGiftId "
			+ "AND :total >= gifts.full_amount " + "AND gifts.is_active = 1 " + "AND prom.is_active = 1 "
			+ "AND prom.start_time <= CURRENT_DATE " + "AND prom.end_time >= CURRENT_DATE "
			+ "AND (gifts.quantity = -1 OR gifts.quantity > 0)", nativeQuery = true)
	PromotionsGifts validateSelectedGift(@Param("selectedGiftId") int selectedGiftId, @Param("total") BigDecimal total);

	/**
	 * 扣減贈品配額，若扣完（quantity = 0）則自動把 is_active 設為 0
	 *
	 * 條件說明： quantity > 0 → 確保還有庫存才扣，防止扣成負數 quantity = quantity - 1 → 扣 1 is_active
	 * = CASE ... → 扣完後若剩 0，同時把這筆贈品活動下架
	 *
	 * 用一條 SQL 完成，避免先扣再判斷中間有其他請求插入的競爭問題
	 */
	@Modifying
	@jakarta.transaction.Transactional
	@Query(value = "UPDATE promotions_gifts " + "SET quantity = quantity - 1, "
			+ "    is_active = CASE WHEN quantity - 1 = 0 THEN 0 ELSE 1 END "
			+ "WHERE id = :id AND quantity > 0", nativeQuery = true)
	void decreaseQuantityAndDeactivateIfEmpty(@Param("id") int id);

	/**
	 * 關閉活動時，批次把底下所有贈品的 is_active 設為 0
	 *
	 * 只在關閉活動（is_active = 0）時使用 開啟活動時不同步贈品，讓前端自己決定要開哪些贈品
	 */
	@Modifying
	@jakarta.transaction.Transactional
	@Query(value = "UPDATE promotions_gifts SET is_active = 0 "
			+ "WHERE promotions_id = :promotionsId", nativeQuery = true)
	void deactivateAllByPromotionsId(@Param("promotionsId") int promotionsId);

	/**
	 * 依 promotions_id 刪除所有對應的贈品資料 用於刪除促銷活動時，先把底下的贈品全部真刪除
	 */
	@Modifying
	@jakarta.transaction.Transactional
	@Query(value = "DELETE FROM promotions_gifts WHERE promotions_id = :promotionsId", nativeQuery = true)
	void deleteByPromotionsId(@Param("promotionsId") int promotionsId);

	/**
	 * 依促銷活動 ID 查出該活動底下的所有贈品（不論啟用/停用）
	 *
	 * 這是 Spring Data JPA 的「衍生查詢（Derived Query）」方法： JPA 根據方法名稱自動解析並產生對應 SQL 等效
	 * SQL：SELECT * FROM promotions_gifts WHERE promotions_id = ?
	 *
	 * 使用場景：GET /promotions/list 時，查詢每個促銷活動底下的所有贈品清單
	 */
	List<PromotionsGifts> findByPromotionsId(int promotionsId);

	@Query("SELECT g FROM PromotionsGifts g JOIN Promotions p ON g.promotionsId = p.id "
			+ "WHERE g.active = true AND p.active = true "
			+ "AND p.startTime <= CURRENT_DATE AND p.endTime >= CURRENT_DATE " + "ORDER BY g.fullAmount DESC")
	List<PromotionsGifts> findAllActiveGiftsOrdered();

	@Query("SELECT g.fullAmount FROM PromotionsGifts g WHERE g.giftProductId = :giftProductId AND g.active = true")
	BigDecimal findFullAmountByGiftProductId(@Param("giftProductId") int giftProductId);

	// 以下 CartService 有用到
	/*
	 * 根據「贈品商品 ID」找到對應的目前 上架的規則 用途：在 selectGift() 裡驗證使用者選的贈品是否還有效
	 * 例如：使用者選了大盤雞（giftProductId = 101），後端查這條規則是否還 is_active = 1
	 */
	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
			+ "JOIN promotions AS prom ON gifts.promotions_id = prom.id " + "WHERE gifts.gift_product_id = ?1 "
			+ "AND gifts.is_active = 1 " + "AND prom.is_active = 1 " + "AND prom.start_time <= CURRENT_DATE "
			+ "AND prom.end_time >= CURRENT_DATE", nativeQuery = true)
	PromotionsGifts findActiveRuleByGiftProductId(int giftProductId);

	// 根據「活動 ID」撈出這個活動底下所有上架的贈品規則
	// 使用時機：CartService 步驟4，確認了某個活動後，找這個活動底下有哪些贈品可以送
	@Query(value = "SELECT * FROM promotions_gifts WHERE promotions_id = ?1 AND is_active = 1", nativeQuery = true)
	List<PromotionsGifts> findGiftsByPromotionId(int promotionId);

	// 根據「贈品規則 ID（主鍵）」找到對應的上架有效規則
	// 使用時機：selectGift()，前端傳來 giftRuleId，後端用主鍵精準定位這條規則
	// 同時 JOIN promotions 確認這條規則對應的活動是否仍在有效期間內且啟用
	// 任一條件不符合就回傳 null
	@Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts "
			+ "JOIN promotions AS prom ON gifts.promotions_id = prom.id " + "WHERE gifts.id = ?1 "
			+ "AND gifts.is_active = 1 " + "AND prom.is_active = 1 " + "AND prom.start_time <= CURRENT_DATE "
			+ "AND prom.end_time >= CURRENT_DATE", nativeQuery = true)
	PromotionsGifts findActiveRuleByGiftRuleId(int giftRuleId);

}