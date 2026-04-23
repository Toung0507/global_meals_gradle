package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.NotBlank;

// POS 廚房狀態更新請求：WAITING → COOKING → READY
public class KitchenStatusReq {

	@NotBlank
	private String id;           // orders.id（4 碼流水號）

	@NotBlank
	private String orderDateId;  // orders.order_date_id（YYYYMMDD）

	@NotBlank
	private String kitchenStatus; // WAITING / COOKING / READY

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getOrderDateId() { return orderDateId; }
	public void setOrderDateId(String orderDateId) { this.orderDateId = orderDateId; }

	public String getKitchenStatus() { return kitchenStatus; }
	public void setKitchenStatus(String kitchenStatus) { this.kitchenStatus = kitchenStatus; }
}
