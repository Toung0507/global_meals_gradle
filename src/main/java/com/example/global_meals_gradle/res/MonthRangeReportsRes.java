package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.MonthlyReportDetailVo;

public class MonthRangeReportsRes extends BasicRes {

	private List<MonthlyReportDetailVo> reportList;

	public MonthRangeReportsRes() {
		super();
	}

	public MonthRangeReportsRes(int code, String message) {
		super(code, message);
	}

	public MonthRangeReportsRes(int code, String message, List<MonthlyReportDetailVo> reportList) {
		super(code, message);
		this.reportList = reportList;
	}

	public List<MonthlyReportDetailVo> getReportList() {
		return reportList;
	}

	public void setReportList(List<MonthlyReportDetailVo> reportList) {
		this.reportList = reportList;
	}
	
}
