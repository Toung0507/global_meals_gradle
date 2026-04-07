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
     * 條件說明：
     *   :total >= gifts.full_amount          → 消費金額有達到這個贈品的門檻
     *   gifts.is_active = 1                  → 這筆贈品活動本身是啟用的（未手動下架）
     *   prom.is_active = 1                   → 對應的促銷活動是啟用的
     *   prom.start_time <= CURRENT_DATE      → 促銷活動已經開始
     *   prom.end_time >= CURRENT_DATE        → 促銷活動還沒結束
     *   (gifts.quantity = -1 OR gifts.quantity > 0)
     *                                        → quantity = -1 表示無限供應，> 0 表示還有庫存
     *                                          兩種情況都可以送贈品，= 0 表示已送完不能送
     *
     * ORDER BY gifts.full_amount DESC LIMIT 1：
     *   取「門檻金額最高的那一筆」
     *   例如：滿 300 送 A、滿 1000 送 B，花 1200 只拿 B（取最高門檻）
     *
     * 回傳 PromotionsGifts entity（不含 products.name，名稱另外由 ProductsTempDao 查）
     */
    @Query(value = "SELECT gifts.* FROM promotions_gifts AS gifts " +
                   "JOIN promotions AS prom ON gifts.promotions_id = prom.id " +
                   "WHERE :total >= gifts.full_amount " +
                   "AND gifts.is_active = 1 " +
                   "AND prom.is_active = 1 " +
                   "AND prom.start_time <= CURRENT_DATE " +
                   "AND prom.end_time >= CURRENT_DATE " +
                   "AND (gifts.quantity = -1 OR gifts.quantity > 0) " +
                   "ORDER BY gifts.full_amount DESC " +
                   "LIMIT 1",
			nativeQuery = true)
	PromotionsGifts findTopQualifiedGift(@Param("total") BigDecimal total);

	/* 核心邏輯：取得所有目前上架中的贈品門檻 */
	@Query(value = "SELECT * FROM promotions_gifts WHERE is_active = 1", nativeQuery = true)
	public List<PromotionsGifts> findAllActiveGifts();

	/*
	 * 根據「贈品商品 ID」找到對應的目前 上架的規則 
	 * 用途：在 selectGift() 裡驗證使用者選的贈品是否還有效
	 * 例如：使用者選了大盤雞（giftProductId = 101），後端查這條規則是否還 is_active = 1
	 */
	@Query(value = "SELECT * FROM promotions_gifts WHERE gift_product_id = ?1 AND is_active = 1", nativeQuery = true)
	PromotionsGifts findActiveRuleByGiftProductId(int giftProductId);
}