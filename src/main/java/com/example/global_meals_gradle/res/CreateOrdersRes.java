package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

/* 成立訂單回傳前端資料(訂單編號、總金額) */
public class CreateOrdersRes extends BasicRes {

	private String id;
	
	private String orderDateId;
	
	private BigDecimal totalAmount;

	public CreateOrdersRes() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CreateOrdersRes(int code, String message) {
		super(code, message);
		// TODO Auto-generated constructor stub
	}

	public CreateOrdersRes(int code, String message, String id, String orderDateId, BigDecimal totalAmount) {
		super(code, message);
		this.id = id;
		this.orderDateId = orderDateId;
		this.totalAmount = totalAmount;
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
	
}
