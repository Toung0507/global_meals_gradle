package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;
import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Min;

public class UpdateRegionsUsageCapReq extends UpsertRegionsTaxReq{
	
	@Min(value = 1, message = ValidationMsg.REGIONS_ID_ERROR)
	private int id;
	
	@Min(value = 0, message = "Usage cap must be >= 0")
    @JsonAlias("usage_cap")
    private int usageCap;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUsageCap() {
		return usageCap;
	}

	public void setUsageCap(int usageCap) {
		this.usageCap = usageCap;
	}
	
}
