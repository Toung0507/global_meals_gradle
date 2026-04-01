package com.example.global_meals_gradle.req;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductCreateReq {

	@NotBlank(message = "產品名稱不能為空")
	private String name;

	@NotBlank(message = "類別不能為空")
	private String category;

	@NotBlank(message = "地區/國家不能為空")
	private String regionCountry;

	@NotNull(message = "價格不能為空")
	@DecimalMin(value = "0.0", inclusive = false, message = "價格必須大於 0")
	private BigDecimal basePrice;

	@Min(value = 0, message = "庫存不能為負數")
	private int stockQuantity;

	@Min(value = 1, message = "單次最大購買量至少為 1")
	private int maxOrderQuantity;

	private String description;

	// 讓你可以決定是否直接上架，預設可以給 false (先建好再手動上架)
	private boolean active;

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

	public String getRegionCountry() {
		return regionCountry;
	}

	public void setRegionCountry(String regionCountry) {
		this.regionCountry = regionCountry;
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

	public int getMaxOrderQuantity() {
		return maxOrderQuantity;
	}

	public void setMaxOrderQuantity(int maxOrderQuantity) {
		this.maxOrderQuantity = maxOrderQuantity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
