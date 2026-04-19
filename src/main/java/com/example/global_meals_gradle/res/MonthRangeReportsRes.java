package com.example.global_meals_gradle.res;

import java.util.List;

public class MonthRangeReportsRes extends BasicRes {

	private List<MonthlyReportDetail> currentMonth;

	public MonthRangeReportsRes() {
		super();
	}

	public MonthRangeReportsRes(int code, String message) {
		super(code, message);
	}

	public MonthRangeReportsRes(int code, String message, List<MonthlyReportDetail> currentMonth) {
		super(code, message);
		this.currentMonth = currentMonth;
	}

	public List<MonthlyReportDetail> getCurrentMonth() {
		return currentMonth;
	}

	public void setCurrentMonth(List<MonthlyReportDetail> currentMonth) {
		this.currentMonth = currentMonth;
	}
	
}
