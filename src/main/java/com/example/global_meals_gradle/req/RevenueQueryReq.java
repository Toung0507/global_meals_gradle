package com.example.global_meals_gradle.req;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RevenueQueryReq {

	@JsonFormat(pattern = "yyyy-MM-dd")
	private String startDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private String endDate;

	// Integer，這樣沒傳時就會是 null，你可以輕鬆用 if (req.getBranchId() == null)
	
	private Integer branchId;

	private Integer regionsId;

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
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
