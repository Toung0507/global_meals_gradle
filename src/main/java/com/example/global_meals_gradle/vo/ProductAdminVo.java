package com.example.global_meals_gradle.vo;

public class ProductAdminVo {
	private int id;
	private String name;

	private int categoryId; // 給前端搜尋或帶入下拉選單用的 ID
	private String category; // 給前端顯示用的名稱

	private int styleId; // 給前端搜尋或帶入下拉選單用的 ID
	private String style; // 給前端顯示用的名稱

	private String description;
	private boolean active;
	private String foodImgBase64; // 這裡是關鍵！轉成 String

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getStyleId() {
		return styleId;
	}

	public void setStyleId(int styleId) {
		this.styleId = styleId;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
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

	public String getFoodImgBase64() {
		return foodImgBase64;
	}

	public void setFoodImgBase64(String foodImgBase64) {
		this.foodImgBase64 = foodImgBase64;
	}

}
