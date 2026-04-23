package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.MonthlyReportDetailVo;

public class MonthlyReportRes extends BasicRes {

	private List<MonthlyReportDetailVo> currentData;
	
	private List<MonthlyReportDetailVo> lastData;

	public MonthlyReportRes() {
		super();
	}

	public MonthlyReportRes(int code, String message) {
		super(code, message);
	}

	public MonthlyReportRes(int code, String message, List<MonthlyReportDetailVo> currentData,
			List<MonthlyReportDetailVo> lastData) {
		super(code, message);
		this.currentData = currentData;
		this.lastData = lastData;
	}

	public List<MonthlyReportDetailVo> getCurrentData() {
		return currentData;
	}

	public void setCurrentData(List<MonthlyReportDetailVo> currentData) {
		this.currentData = currentData;
	}

	public List<MonthlyReportDetailVo> getLastData() {
		return lastData;
	}

	public void setLastData(List<MonthlyReportDetailVo> lastData) {
		this.lastData = lastData;
	}
	
}
