package com.example.global_meals_gradle.req;

import java.math.BigDecimal;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CashPayOnSiteReq {

	@NotBlank(message = ValidationMsg.ID_ERROR)
	private String id;

	@NotBlank(message = ValidationMsg.ORDER_DATE_ID_ERROR)
	private String orderDateId;

	@NotNull(message = ValidationMsg.ORDER_TOTAL_AMOUNT_NOT_NULL)
	private BigDecimal totalAmount;

	private String paymentMethod;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderDateId() {
		return orderDateId;
	}

	public void setOrderDateId(String orderDateId) {
		this.orderDateId = orderDateId;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

}
