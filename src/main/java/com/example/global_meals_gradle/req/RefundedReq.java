package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.OrdersStatus;

/* 刪除或取消訂單 */
public class RefundedReq {

	private String orderDateId;
	
	private String id;
	
	private OrdersStatus status; // 傳入字串，例如 "CANCELLED" 或 "REFUNDED"，後端再轉成ENUM

	public String getOrderDateId() {
		return orderDateId;
	}

	public void setOrderDateId(String orderDateId) {
		this.orderDateId = orderDateId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OrdersStatus getStatus() {
		return status;
	}

	public void setStatus(OrdersStatus status) {
		this.status = status;
	}
	
}
