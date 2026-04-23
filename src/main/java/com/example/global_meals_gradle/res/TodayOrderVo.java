package com.example.global_meals_gradle.res;

import java.math.BigDecimal;
import java.util.List;

// POS 看板：今日單筆訂單
public class TodayOrderVo {

	private String id;
	private String orderDateId;
	private BigDecimal totalAmount;
	private String kitchenStatus;  // WAITING / COOKING / READY
	private String paymentStatus;  // COMPLETED 或 PENDING_CASH
	private String phone;
	private List<TodayOrderDetailVo> items;

	public TodayOrderVo() {}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getOrderDateId() { return orderDateId; }
	public void setOrderDateId(String orderDateId) { this.orderDateId = orderDateId; }

	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

	public String getKitchenStatus() { return kitchenStatus; }
	public void setKitchenStatus(String kitchenStatus) { this.kitchenStatus = kitchenStatus; }

	public String getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public List<TodayOrderDetailVo> getItems() { return items; }
	public void setItems(List<TodayOrderDetailVo> items) { this.items = items; }
}
