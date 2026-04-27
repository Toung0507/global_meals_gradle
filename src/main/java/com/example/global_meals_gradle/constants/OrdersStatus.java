package com.example.global_meals_gradle.constants;

import com.fasterxml.jackson.annotation.JsonFormat;

// 讓 Jackson 反序列化時忽略大小寫
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public enum OrdersStatus {

	PREPARING("PREPARING"), // 製作中(初始狀態)
	COMPLETED("COMPLETED"), // 餐點完成
	CANCELLED("CANCELLED"); // 已取消(退款跟取消)

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
