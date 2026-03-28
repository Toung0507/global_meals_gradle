package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.OrdersStatus;

/* 付款完成: 會傳付款方法、序號、狀態 */
public class PayReq {

	private String id;
	
	private String orderDateId;
	
	private String paymentMethod;
	
	private String transactionId;
	
	private OrdersStatus status;

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

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public OrdersStatus getStatus() {
		return status;
	}

	public void setStatus(OrdersStatus status) {
		this.status = status;
	}
	
}
