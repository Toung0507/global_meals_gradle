package com.example.global_meals_gradle.req;

import com.fasterxml.jackson.annotation.JsonFormat;

public class MonthRangeReportsReq {

	@JsonFormat(pattern = "yyyy-MM")
	private String startMonth;
	
	@JsonFormat(pattern = "yyyy-MM")
	private String endMonth;

	public String getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}

	public String getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(String endMonth) {
		this.endMonth = endMonth;
	}
}
