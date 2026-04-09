package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 促銷活動管理用的請求參數，涵蓋以下四種用途：
 *   - 新增促銷活動（promotions 表）
 *   - 新增贈品至活動（promotions_gifts 表）
 *   - 開關促銷活動（promotions.is_active）
 *   - 刪除促銷活動（入參只用 promotionsId）
 */
public class PromotionsManageReq {

	// =============================================
	// 新增促銷活動（promotions 表）用的欄位
	// =============================================

	// 活動名稱：必填，不能空白
	@NotBlank(message = "Promotion name is required")
	private String name;

	// 活動開始日期：必填，不能早於今天（在 Service 裡驗證）
	@NotNull(message = "Start time is required")
	private LocalDate startTime;

	// 活動結束日期：必填，必須晚於 startTime（在 Service 裡驗證）
	@NotNull(message = "End time is required")
	private LocalDate endTime;

	// =============================================
	// 新增贈品至活動（promotions_gifts 表）用的欄位
	// =============================================

	// 對應的促銷活動 ID（promotions.id）：
	// 新增贈品和開關活動時都需要這個欄位
	// 在 Service 裡驗證這個 ID 是否真的存在於 promotions 表
	@Min(value = 1, message = "Promotions ID must be at least 1")
	private int promotionsId;

	// 消費門檻金額：必填，必須大於 0
	// 例如：滿 300 才送贈品，這裡填 300
	@NotNull(message = "Full amount is required")
	private BigDecimal fullAmount;

	// 贈品配額數量：
	//   -1  → 無限供應（預設值），不會自動扣減
	//   >= 1 → 有限供應，每次結帳扣 1，扣到 0 自動下架
	//   0   → 不合法，在 Service 裡擋掉
	private int quantity = -1;

	// 贈品對應的商品 ID（products.id）：必填，最小為 1
	// 在 Service 裡查 products 表確認此 ID 有對應的商品名稱
	@Min(value = 1, message = "Gift product ID must be at least 1")
	private int giftProductId;

	// =============================================
	// 開關促銷活動用的欄位
	// =============================================

	// 活動開關狀態：
	//   true  → 開啟活動（promotions.is_active = 1），只改 promotions，贈品不動
	//   false → 關閉活動（promotions.is_active = 0），同步把底下所有贈品 is_active 設為 0
	private boolean active;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDate startTime) {
		this.startTime = startTime;
	}

	public LocalDate getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDate endTime) {
		this.endTime = endTime;
	}

	public int getPromotionsId() {
		return promotionsId;
	}

	public void setPromotionsId(int promotionsId) {
		this.promotionsId = promotionsId;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
