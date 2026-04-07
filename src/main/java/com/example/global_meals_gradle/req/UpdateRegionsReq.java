package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

public class UpdateRegionsReq extends CreateRegionsReq{
	
	@Min(value = 1, message = ValidationMsg.REGIONS_ID_ERROR)
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
