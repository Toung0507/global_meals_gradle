package com.example.global_meals_gradle.vo;

import java.math.BigDecimal;

public class AvailableGiftVo {

	// 這條贈品規則的 ID（對應 promotions_gifts.id）
	// 若無，前端警告：清單裡的每一個小孩，都應該要有一個獨一無二的 key！
	// 這樣才好重新顯示畫面的時候只渲染更動的那一塊，識別的出來
	private int giftRuleId;

	/**
	 * 贈品的商品 ID <br>
	 * 來源：對應資料庫 promotions_gifts.gift_product_id (促銷規則中指定的贈品) <br>
	 * 本質：對應資料庫 products.id (實際商品的身分證) <br>
	 * 作用：前端會將此 ID 傳回 API (POST /cart/gift)，讓後端將該商品以贈品形式加入購物車。
	 */
	private int giftProductId;

	private String giftProductName;

	private BigDecimal fullAmount;

	private boolean available;

	private String unavailableReason;

	public int getGiftRuleId() {
		return giftRuleId;
	}

	public void setGiftRuleId(int giftRuleId) {
		this.giftRuleId = giftRuleId;
	}

	public int getGiftProductId() {
		return giftProductId;
	}

	public void setGiftProductId(int giftProductId) {
		this.giftProductId = giftProductId;
	}

	public String getGiftProductName() {
		return giftProductName;
	}

	public void setGiftProductName(String giftProductName) {
		this.giftProductName = giftProductName;
	}

	public BigDecimal getFullAmount() {
		return fullAmount;
	}

	public void setFullAmount(BigDecimal fullAmount) {
		this.fullAmount = fullAmount;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getUnavailableReason() {
		return unavailableReason;
	}

	public void setUnavailableReason(String unavailableReason) {
		this.unavailableReason = unavailableReason;
	}

}
