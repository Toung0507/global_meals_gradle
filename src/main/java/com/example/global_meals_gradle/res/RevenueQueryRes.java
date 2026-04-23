package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.RevenueDataVo;

public class RevenueQueryRes extends BasicRes {
	
	private List<RevenueDataVo> revenueData;

	public RevenueQueryRes() {
		super();
	}

	public RevenueQueryRes(int code, String message) {
		super(code, message);
	}

	public RevenueQueryRes(int code, String message, List<RevenueDataVo> revenueData) {
		super(code, message);
		this.revenueData = revenueData;
	}

	public List<RevenueDataVo> getRevenueData() {
		return revenueData;
	}

	public void setRevenueData(List<RevenueDataVo> revenueData) {
		this.revenueData = revenueData;
	}
	
}