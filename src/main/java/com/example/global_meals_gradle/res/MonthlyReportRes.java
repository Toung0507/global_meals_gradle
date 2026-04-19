package com.example.global_meals_gradle.res;

import java.util.List;

public class MonthlyReportRes extends BasicRes {

	private List<MonthlyReportDetail> currentData;
	
	private List<MonthlyReportDetail> lastData;

	public MonthlyReportRes() {
		super();
	}

	public MonthlyReportRes(int code, String message) {
		super(code, message);
	}

	public MonthlyReportRes(int code, String message, List<MonthlyReportDetail> currentData,
			List<MonthlyReportDetail> lastData) {
		super(code, message);
		this.currentData = currentData;
		this.lastData = lastData;
	}

	public List<MonthlyReportDetail> getCurrentData() {
		return currentData;
	}

	public void setCurrentData(List<MonthlyReportDetail> currentData) {
		this.currentData = currentData;
	}

	public List<MonthlyReportDetail> getLastData() {
		return lastData;
	}

	public void setLastData(List<MonthlyReportDetail> lastData) {
		this.lastData = lastData;
	}
	
}
