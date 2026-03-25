package com.example.global_meals_gradle.constants;

public enum OrdersStatus {
	
	COMPLETED("COMPLETED"), //
	CANCELLED("CANCELLED"), //
	REFUNDED("REFUNDED");
	
	private String ordersStatus;

	private OrdersStatus(String ordersStatus) {
		this.ordersStatus = ordersStatus;
	}

	public String getOrdersStatus() {
		return ordersStatus;
	}

	public void setOrdersStatus(String ordersStatus) {
		this.ordersStatus = ordersStatus;
	}
	

}
