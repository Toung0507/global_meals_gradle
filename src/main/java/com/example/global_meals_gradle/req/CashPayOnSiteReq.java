package com.example.global_meals_gradle.req;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CashPayOnSiteReq {

	@NotBlank
	private String id;

	@NotBlank
	private String orderDateId;

	@NotNull
	private BigDecimal totalAmount;

	@NotBlank
	private String paymentMethod; // 固定傳 "CASH"

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getOrderDateId() { return orderDateId; }
	public void setOrderDateId(String orderDateId) { this.orderDateId = orderDateId; }

	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

	public String getPaymentMethod() { return paymentMethod; }
	public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
