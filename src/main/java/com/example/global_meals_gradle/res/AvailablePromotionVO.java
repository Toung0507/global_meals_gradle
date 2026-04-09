package com.example.global_meals_gradle.res;

import java.math.BigDecimal;
import java.util.List;

// 代表「一個使用者有資格參加的贈品活動」
// 是 CartViewRes.availablePromotions 清單裡的每一個元素
// 前端用它來渲染「選擇活動」的下拉選單
public class AvailablePromotionVO {

// 活動的 ID，對應資料庫 promotions.id
// 前端用它來區分每個活動（React/Vue 的 key 用）
	private int promotionId;

// 活動名稱，對應資料庫 promotions.name，例如「夏日祭典」
// 前端把這個顯示在下拉選單給使用者看
	private String promotionName;

// 這個活動的最低消費門檻（這個活動底下所有規則裡，門檻最低的那個金額）
// 例如：活動底下有「滿300送A」和「滿500送B」，這裡就是 300
// 前端可以用它顯示「消費滿 $XXX 可參加」的提示
	private BigDecimal fullAmount;

// 這個活動底下所有的贈品選項清單
// available=true  → 前端正常顯示，使用者可以點選
// available=false → 前端灰色顯示，旁邊標注 unavailableReason
	private List<AvailableGiftVO> gifts;

	public int getPromotionId() {
		return promotionId;
	}

	public void setPromotionId(int promotionId) {
		this.promotionId = promotionId;
	}

	public String getPromotionName() {
		return promotionName;
	}

	public void setPromotionName(String promotionName) {
		this.promotionName = promotionName;
	}

	public BigDecimal getFullAmount() {
		return fullAmount;
	}

	public void setFullAmount(BigDecimal fullAmount) {
		this.fullAmount = fullAmount;
	}

	public List<AvailableGiftVO> getGifts() {
		return gifts;
	}

	public void setGifts(List<AvailableGiftVO> gifts) {
		this.gifts = gifts;
	}
}
