package com.example.global_meals_gradle.req;

/* 刪除或取消訂單 */
public class RefundedReq {

	private String orderDateId;
	
	private String id;

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
	
}
