package com.example.global_meals_gradle.req;

import java.math.BigDecimal;
import java.util.List;

public class AiPromotionsReq {
	private int promotionsId;
	
	private String activityName;
	private List<PromotionItem> promotionItems;

	public static class PromotionItem {
		private int productId;
		private BigDecimal fullAmount;

		public int getProductId() {
			return productId;
		}

		public void setProductId(int productId) {
			this.productId = productId;
		}

		public BigDecimal getFullAmount() {
			return fullAmount;
		}

		public void setFullAmount(BigDecimal fullAmount) {
			this.fullAmount = fullAmount;
		}

	}

	public int getPromotionsId() {
		return promotionsId;
	}

	public void setPromotionsId(int promotionsId) {
		this.promotionsId = promotionsId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public List<PromotionItem> getPromotionItems() {
		return promotionItems;
	}

	public void setPromotionItems(List<PromotionItem> promotionItems) {
		this.promotionItems = promotionItems;
	}

}
