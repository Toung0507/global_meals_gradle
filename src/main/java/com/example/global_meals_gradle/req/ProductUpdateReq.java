package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

public class ProductUpdateReq extends ProductCreateReq {
	
	@Min(value = 1, message = ValidationMsg.PRODUCT_ID_MUST_BE_POSITIVE)
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
