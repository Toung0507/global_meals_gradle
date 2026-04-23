package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductCreateReq {

	@NotBlank(message = "產品名稱不能為空")
	private String name;

	@NotBlank(message = "類別不能為空")
	private String category;

	private String description;

	@NotNull(message = "分店 ID 不能為空")
	private Integer globalAreaId;

	@NotNull(message = "價格不能為空")
	private Double basePrice;

	private int stockQuantity;

	private Integer maxOrderQuantity;

	private boolean active;

	private String imageBase64;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public Integer getGlobalAreaId() { return globalAreaId; }
	public void setGlobalAreaId(Integer globalAreaId) { this.globalAreaId = globalAreaId; }

	public Double getBasePrice() { return basePrice; }
	public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

	public int getStockQuantity() { return stockQuantity; }
	public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

	public Integer getMaxOrderQuantity() { return maxOrderQuantity; }
	public void setMaxOrderQuantity(Integer maxOrderQuantity) { this.maxOrderQuantity = maxOrderQuantity; }

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }

	public String getImageBase64() { return imageBase64; }
	public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
