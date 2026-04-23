package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

public class GetOrdersByPhoneRes extends BasicRes {

	private String id;

	private String orderDateId;
	
	private BigDecimal totalAmount;
	
	private String status;

	public GetOrdersByPhoneRes() {
		super();
	}

	public GetOrdersByPhoneRes(int code, String message) {
		super(code, message);
	}

	public GetOrdersByPhoneRes(int code, String message, String id, String orderDateId, BigDecimal totalAmount,
			String status) {
		super(code, message);
		this.id = id;
		this.orderDateId = orderDateId;
		this.totalAmount = totalAmount;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderDateId() {
		return orderDateId;
	}

	public void setOrderDateId(String orderDateId) {
		this.orderDateId = orderDateId;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
