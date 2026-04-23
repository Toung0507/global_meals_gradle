package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

public class MonthlyReportDetail {

	private String reportDate;
	
	private String branchName;
	
	private String regionsName;
	
	private BigDecimal totalAmount;

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getRegionsName() {
		return regionsName;
	}

	public void setRegionsName(String regionsName) {
		this.regionsName = regionsName;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
	
}
