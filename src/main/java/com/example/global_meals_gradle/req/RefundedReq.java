package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/* 刪除或取消訂單 */
public class RefundedReq {

	/*
	 * @NotBlank: 限制屬性值不能是 1.空子串 2.全空白字串 3.null message
	 * 是指當屬性值違反限制時得到的訊息，等號後面的值必須是常數(final)
	 */
	@NotBlank(message = ValidationMsg.ORDER_DATE_ID_ERROR)
	private String orderDateId;

	@NotBlank(message = ValidationMsg.ID_ERROR)
	private String id;

	@NotNull(message = ValidationMsg.STATUS_ERROR)
	private String ordersStatus;
	
	private String payStatus;

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

	public String getOrdersStatus() {
		return ordersStatus;
	}

	public void setOrdersStatus(String ordersStatus) {
		this.ordersStatus = ordersStatus;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}
	
}
