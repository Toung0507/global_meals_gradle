package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

public class AiProductDescReq {
	private int productid;

	@NotBlank(message = ValidationMsg.PRODUCT_NAME_EMPTY)
	private String productName;

	@NotBlank(message = ValidationMsg.PRODUCT_CATEGORY_EMPTY)
	private String category;

	@NotBlank(message = ValidationMsg.PRODUCT_STYLE_EMPTY)
	private String style;

	public int getProductid() {
		return productid;
	}

	public void setProductid(int productid) {
		this.productid = productid;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
