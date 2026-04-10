package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

/**
 * 單一贈品詳細資訊（供 GET /promotions/list 使用）
 *
 * 放在 PromotionDetailVo.gifts 清單裡，代表某個促銷活動底下的一筆贈品規則
 * 與 GiftItem 的差別：
 *   GiftItem      → 結帳時讓使用者「選擇」的可選贈品（只給還有效的）
 *   GiftDetailVo  → 管理端「清單」用，顯示所有贈品，包含已停用的
 */
public class GiftDetailVo {

	// promotions_gifts 表的 id，管理端要對特定贈品操作時傳回來
	private int id;

	// 消費門檻金額：達到這個金額才能領取此贈品
	// 對應 promotions_gifts.full_amount
	private BigDecimal fullAmount;

	// 贈品配額：
	//   -1 → 無限供應
	//   > 0 → 還有幾個可以領
	//   = 0 → 已送完（理論上 is_active 也會同步被設為 false）
	private int quantity;

	// 贈品商品 ID，對應 products.id
	private int giftProductId;

	// 贈品商品名稱，從 products 表查出來
	// 查不到時（例如商品已被刪除）顯示 "活動贈品"
	private String productName;

	// 這筆贈品規則是否啟用：
	//   true  → 目前有效，結帳時可以選
	//   false → 已停用（庫存耗盡或手動關閉）
	private boolean active;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigDecimal getFullAmount() {
		return fullAmount;
	}

	public void setFullAmount(BigDecimal fullAmount) {
		this.fullAmount = fullAmount;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getGiftProductId() {
		return giftProductId;
	}

	public void setGiftProductId(int giftProductId) {
		this.giftProductId = giftProductId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
