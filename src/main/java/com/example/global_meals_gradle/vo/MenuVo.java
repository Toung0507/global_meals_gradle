package com.example.global_meals_gradle.vo;

import java.math.BigDecimal;

public class MenuVo {
	// 前台菜單顯示
	private int productId;
	private String name;
	private String category;
	private String description;
	private String foodImgBase64; // 這裡是關鍵！轉成 String
	private BigDecimal basePrice;
	private int stockQuantity;
	private boolean active;

	public MenuVo() {
		super();
	}

	public MenuVo(int productId, String name, String category, String description, String foodImgBase64,
			BigDecimal basePrice, int stockQuantity, boolean active) {
		super();
		this.productId = productId;
		this.name = name;
		this.category = category;
		this.description = description;
		this.foodImgBase64 = foodImgBase64;
		this.basePrice = basePrice;
		this.stockQuantity = stockQuantity;
		this.active = active;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFoodImgBase64() {
		return foodImgBase64;
	}

	public void setFoodImgBase64(String foodImgBase64) {
		this.foodImgBase64 = foodImgBase64;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
