package com.example.global_meals_gradle.res;

import java.util.List;

public class RevenueQueryRes extends BasicRes {
	
	private List<RevenueData> revenueData;

	public RevenueQueryRes() {
		super();
	}

	public RevenueQueryRes(int code, String message) {
		super(code, message);
	}

	public RevenueQueryRes(int code, String message, List<RevenueData> revenueData) {
		super(code, message);
		this.revenueData = revenueData;
	}

	public List<RevenueData> getRevenueData() {
		return revenueData;
	}

	public void setRevenueData(List<RevenueData> revenueData) {
		this.revenueData = revenueData;
	}
	
}