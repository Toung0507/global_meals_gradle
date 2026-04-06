package com.example.global_meals_gradle.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OrdersId implements Serializable {

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
