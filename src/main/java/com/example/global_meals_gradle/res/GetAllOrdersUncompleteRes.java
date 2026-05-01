package com.example.global_meals_gradle.res;

import java.util.List;

public class GetAllOrdersUncompleteRes extends BasicRes {

	private List<OrderInfo> ordersList;

	public GetAllOrdersUncompleteRes() { super(); }

	public GetAllOrdersUncompleteRes(int code, String message) { super(code, message); }

	public GetAllOrdersUncompleteRes(int code, String message, List<OrderInfo> ordersList) {
		super(code, message);
		this.ordersList = ordersList;
	}

	public List<OrderInfo> getOrdersList() { return ordersList; }
	public void setOrdersList(List<OrderInfo> ordersList) { this.ordersList = ordersList; }

	public static class OrderInfo {
		private String orderDateId;
		private String id;
		private String orderStatus;

		public String getOrderDateId() { return orderDateId; }
		public void setOrderDateId(String orderDateId) { this.orderDateId = orderDateId; }

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }

		public String getOrderStatus() { return orderStatus; }
		public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
	}
}
