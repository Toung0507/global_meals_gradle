package com.example.global_meals_gradle.res;

import java.math.BigDecimal;
import java.util.List;

public class PromotionsRes {
	private int cartId;                        // 購物車 ID
    private List<Integer> appliedPromotionIds; // 所有參與到的贈品活動 ID
    private String appliedDiscountName;        // 若用券則為 "會員 8 折優惠"，否則為空
    private BigDecimal originalAmount;         // 原始總價
    private int finalAmount;                   // 最終金額 (折扣後且無條件進位)
    private List<GiftItem> receivedGifts;      // 贈品清單
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
