package com.example.global_meals_gradle.res;

import java.math.BigDecimal;
import java.util.List;

/**
 * 促銷活動回傳結果
 * Service 計算完畢後封裝成這個物件回給前端
 */
public class PromotionsRes {

	// 購物車 ID：原封不動從 Req 帶過來，讓前端知道這個結果對應哪張購物車
	private int cartId;

	// 本次結帳觸發的所有贈品活動 ID (promotions.id)
	// 例如：花 1200 得到 B 贈品，B 贈品對應的 promotions.id 就會放進來
	// 若沒有任何贈品達標，這裡是空 List
	private List<Integer> appliedPromotionIds;

	// 折扣名稱：
	//   有使用 8 折券 → "會員 8 折優惠"
	//   沒有使用     → 空字串 ""
	private String appliedDiscountName;

	// 原始總金額：從呼叫方傳進來，直接放進去，不做任何計算
	// 用來讓前端顯示「折扣前金額」
	private BigDecimal originalAmount;

	// 最終金額：折扣後無條件進位的整數
	//   有折扣 → originalAmount * 0.8，無條件進位
	//   無折扣 → originalAmount 無條件進位
	private int finalAmount;

	// 贈品清單：每個 GiftItem 包含贈品商品 ID、名稱、數量
	// 若沒有任何贈品達標，這裡是空 List
	private List<GiftItem> receivedGifts;

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public List<Integer> getAppliedPromotionIds() {
		return appliedPromotionIds;
	}

	public void setAppliedPromotionIds(List<Integer> appliedPromotionIds) {
		this.appliedPromotionIds = appliedPromotionIds;
	}

	public String getAppliedDiscountName() {
		return appliedDiscountName;
	}

	public void setAppliedDiscountName(String appliedDiscountName) {
		this.appliedDiscountName = appliedDiscountName;
	}

	public BigDecimal getOriginalAmount() {
		return originalAmount;
	}

	public void setOriginalAmount(BigDecimal originalAmount) {
		this.originalAmount = originalAmount;
	}

	public int getFinalAmount() {
		return finalAmount;
	}

	public void setFinalAmount(int finalAmount) {
		this.finalAmount = finalAmount;
	}

	public List<GiftItem> getReceivedGifts() {
		return receivedGifts;
	}

	public void setReceivedGifts(List<GiftItem> receivedGifts) {
		this.receivedGifts = receivedGifts;
	}

}
