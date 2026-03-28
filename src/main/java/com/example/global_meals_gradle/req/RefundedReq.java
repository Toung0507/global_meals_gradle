package com.example.global_meals_gradle.req;

/* 刪除或取消訂單 */
public class RefundedReq {

	private String orderDateId;
	
	private String id;
	
	private String status; // 傳入字串，例如 "CANCELLED" 或 "REFUNDED"，後端再轉成ENUM

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
