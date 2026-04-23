package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

public class ProductCreateReq {

	@NotBlank(message = ValidationMsg.PRODUCT_NAME_EMPTY)
	private String name;

	@NotBlank(message = ValidationMsg.PRODUCT_CATEGORY_EMPTY)
	private String category;

	@NotBlank(message = ValidationMsg.PRODUCT_DESCRIPTION_EMPTY)
	private String description;

	// 讓你可以決定是否直接上架，預設可以給 false (先建好再手動上架)
	private boolean active = false;

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
