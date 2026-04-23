package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.MonthlyReportDetailVo;

public class MonthRangeReportsRes extends BasicRes {

	private List<MonthlyReportDetailVo> currentMonth;

	public MonthRangeReportsRes() {
		super();
	}

	public MonthRangeReportsRes(int code, String message) {
		super(code, message);
	}

	public MonthRangeReportsRes(int code, String message, List<MonthlyReportDetailVo> currentMonth) {
		super(code, message);
		this.currentMonth = currentMonth;
	}

	public List<MonthlyReportDetailVo> getCurrentMonth() {
		return currentMonth;
	}

	public void setCurrentMonth(List<MonthlyReportDetailVo> currentMonth) {
		this.currentMonth = currentMonth;
	}
	
}
