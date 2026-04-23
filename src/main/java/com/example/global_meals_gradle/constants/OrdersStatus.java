package com.example.global_meals_gradle.constants;

import com.fasterxml.jackson.annotation.JsonFormat;

//讓 Jackson 反序列化時忽略大小寫
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public enum OrdersStatus {

	UNPAID("UNPAID"), //
	PENDING_CASH("PENDING_CASH"), // 客戶端現金訂單：已建立但等待現場收款
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
