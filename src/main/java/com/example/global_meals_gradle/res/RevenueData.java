package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

public class RevenueData {

	private String branchName;
	
	private String regionsName;
	
	private BigDecimal totalAmount;

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
