package com.example.global_meals_gradle.req;

public class UpdateKitchenStatusReq {

	private String id;
	private String orderDateId;
	private String kitchenStatus; // COOKING or READY

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getOrderDateId() { return orderDateId; }
	public void setOrderDateId(String orderDateId) { this.orderDateId = orderDateId; }

	public String getKitchenStatus() { return kitchenStatus; }
	public void setKitchenStatus(String kitchenStatus) { this.kitchenStatus = kitchenStatus; }
}
