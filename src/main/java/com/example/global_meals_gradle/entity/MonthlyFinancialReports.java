package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monthly_financial_reports")
public class MonthlyFinancialReports {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "report_date")
	private String reportDate;

	@Column(name = "branch_id")
	private int branchId;

	@Column(name = "regions_id")
	private int regionsId;

	@Column(name = "total_amount", precision = 12, scale = 2)
	private BigDecimal totalAmount;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	public int getBranchId() {
		return branchId;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}

	public int getRegionsId() {
		return regionsId;
	}

	public void setRegionsId(int regionsId) {
		this.regionsId = regionsId;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
}
