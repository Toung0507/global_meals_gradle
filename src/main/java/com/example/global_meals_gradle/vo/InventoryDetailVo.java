package com.example.global_meals_gradle.vo;

import java.math.BigDecimal;

public class InventoryDetailVo {

	private int productId;
	private String productName; // 補上這欄位，方便分店長確認他在改什麼，但在管理者 AdminProductRes 底下，這個欄位設定為 Null
	private int globalAreaId;
	private String branchName; // 關鍵：我們幫前端查好的分店名稱
	private BigDecimal basePrice;
	private BigDecimal costPrice;
	private int stockQuantity;
	private int maxOrderQuantity;
	private boolean active;

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public int getMaxOrderQuantity() {
		return maxOrderQuantity;
	}

	public void setMaxOrderQuantity(int maxOrderQuantity) {
		this.maxOrderQuantity = maxOrderQuantity;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
