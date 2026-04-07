package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

public class UpdateGlobalAreaReq extends CreateGlobalAreaReq {

	@Min(value = 1, message = ValidationMsg.GLOBAL_AREA_ID_ERROR)
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
