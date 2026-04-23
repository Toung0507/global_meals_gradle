package com.example.global_meals_gradle.vo;

import java.math.BigDecimal;

public class BranchInventoryVO {

	private int id;
	private int productId;
	private String productName;
	private String category;
	private int globalAreaId;
	private int stockQuantity;
	private BigDecimal basePrice;
	private int maxOrderQuantity;
	private int version;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public int getProductId() { return productId; }
	public void setProductId(int productId) { this.productId = productId; }

	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public int getGlobalAreaId() { return globalAreaId; }
	public void setGlobalAreaId(int globalAreaId) { this.globalAreaId = globalAreaId; }

	public int getStockQuantity() { return stockQuantity; }
	public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

	public BigDecimal getBasePrice() { return basePrice; }
	public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

	public int getMaxOrderQuantity() { return maxOrderQuantity; }
	public void setMaxOrderQuantity(int maxOrderQuantity) { this.maxOrderQuantity = maxOrderQuantity; }

	public int getVersion() { return version; }
	public void setVersion(int version) { this.version = version; }
}
