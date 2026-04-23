package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.NotBlank;

public class PaymentInitReq {

	@NotBlank(message = "orderDateId 不能為空")
	private String orderDateId;

	@NotBlank(message = "id 不能為空")
	private String id;

	public String getOrderDateId() { return orderDateId; }
	public void setOrderDateId(String orderDateId) { this.orderDateId = orderDateId; }

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
}
