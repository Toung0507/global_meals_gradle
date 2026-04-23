package com.example.global_meals_gradle.vo;

import java.math.BigDecimal;

public class ProductVO {

	private int id;
	private String name;
	private String category;
	private String description;
	private boolean active;
	private BigDecimal basePrice;
	private int stockQuantity;
	private int maxOrderQuantity;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }

	public BigDecimal getBasePrice() { return basePrice; }
	public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

	public int getStockQuantity() { return stockQuantity; }
	public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

	public int getMaxOrderQuantity() { return maxOrderQuantity; }
	public void setMaxOrderQuantity(int maxOrderQuantity) { this.maxOrderQuantity = maxOrderQuantity; }
}
