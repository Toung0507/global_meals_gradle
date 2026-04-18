package com.example.global_meals_gradle.req;

public class MonthlyReportReq {

	private String reportDate;
	
	private Integer branchId;
	
	private Integer regionsId;

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	public Integer getBranchId() {
		return branchId;
	}

	public void setBranchId(Integer branchId) {
		this.branchId = branchId;
	}

	public Integer getRegionsId() {
		return regionsId;
	}

	public void setRegionsId(Integer regionsId) {
		this.regionsId = regionsId;
	}
	
}
