package com.example.global_meals_gradle.vo;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

public class OrdersIdVo {

	@NotBlank(message = ValidationMsg.ORDER_DATE_ID_ERROR)
	private String orderDateId;

	@NotBlank(message = ValidationMsg.ID_ERROR)
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
