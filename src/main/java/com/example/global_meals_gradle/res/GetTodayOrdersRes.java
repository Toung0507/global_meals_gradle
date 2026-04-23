package com.example.global_meals_gradle.res;

import java.util.List;

// POS 看板：今日訂單清單回傳
public class GetTodayOrdersRes extends BasicRes {

	private List<TodayOrderVo> orders;

	public GetTodayOrdersRes(int code, String message) {
		super(code, message);
	}

	public GetTodayOrdersRes(int code, String message, List<TodayOrderVo> orders) {
		super(code, message);
		this.orders = orders;
	}

	public List<TodayOrderVo> getOrders() { return orders; }
	public void setOrders(List<TodayOrderVo> orders) { this.orders = orders; }
}
