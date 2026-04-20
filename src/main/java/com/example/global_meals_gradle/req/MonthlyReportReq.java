package com.example.global_meals_gradle.req;

import com.fasterxml.jackson.annotation.JsonFormat;

public class MonthlyReportReq {

	@JsonFormat(pattern = "yyyy-MM")
	private String reportDate;

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}
}
