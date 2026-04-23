package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.NotNull;

public class UpdateBranchInventoryReq {

	@NotNull(message = "商品 ID 不能為空")
	private Integer productId;

	@NotNull(message = "分店 ID 不能為空")
	private Integer globalAreaId;

	private Double basePrice;
	private Integer stockQuantity;
	private Integer maxOrderQuantity;

	public Integer getProductId() { return productId; }
	public void setProductId(Integer productId) { this.productId = productId; }

	public Integer getGlobalAreaId() { return globalAreaId; }
	public void setGlobalAreaId(Integer globalAreaId) { this.globalAreaId = globalAreaId; }

	public Double getBasePrice() { return basePrice; }
	public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

	public Integer getStockQuantity() { return stockQuantity; }
	public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

	public Integer getMaxOrderQuantity() { return maxOrderQuantity; }
	public void setMaxOrderQuantity(Integer maxOrderQuantity) { this.maxOrderQuantity = maxOrderQuantity; }
}
