package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

/* 回傳總金額(用於有無優惠劵api) */
public class TotalAmountRes extends BasicRes {

	private BigDecimal taxAmount;

	public TotalAmountRes() {
		super();
	}

	public TotalAmountRes(int code, String message) {
		super(code, message);
	}

	public TotalAmountRes(int code, String message, BigDecimal taxAmount) {
		super(code, message);
		this.taxAmount = taxAmount;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

}
