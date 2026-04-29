package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

public class GetOrdersByPhoneRes extends BasicRes {

	private String id;

	private String orderDateId;
	
	private BigDecimal totalAmount;
	
	private String ordersStatus;
	
	private String payStatus;

	public GetOrdersByPhoneRes() {
		super();
	}

	public GetOrdersByPhoneRes(int code, String message) {
		super(code, message);
	}
	
	public GetOrdersByPhoneRes(int code, String message, String id, String orderDateId, BigDecimal totalAmount,
			String ordersStatus, String payStatus) {
		super(code, message);
		this.id = id;
		this.orderDateId = orderDateId;
		this.totalAmount = totalAmount;
		this.ordersStatus = ordersStatus;
		this.payStatus = payStatus;
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

	public String getOrdersStatus() {
		return ordersStatus;
	}

	public void setOrdersStatus(String ordersStatus) {
		this.ordersStatus = ordersStatus;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

}
