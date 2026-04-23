package com.example.global_meals_gradle.res;

public class LinePayRes extends BasicRes {

	private String paymentUrl;

	public LinePayRes(int code, String message, String paymentUrl) {
		super(code, message);
		this.paymentUrl = paymentUrl;
	}

	public String getPaymentUrl() { return paymentUrl; }
	public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}
