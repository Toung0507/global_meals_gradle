package com.example.global_meals_gradle.constants;

import com.fasterxml.jackson.annotation.JsonFormat;

// 讓 Jackson 反序列化時忽略大小寫
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public enum OrdersStatus {

	PREPARING("PREPARING"), // 備餐中（初始狀態，尚未開始製作）
	COOKING("COOKING"),    // 製作中（廚房已開始製作）
	READY("READY"), // 餐點完成/待取餐
	PICKED_UP("PICKED UP"), // 已取餐
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
