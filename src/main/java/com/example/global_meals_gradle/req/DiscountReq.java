package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

public class DiscountReq {

	@NotBlank(message = ValidationMsg.ID_ERROR)
	private String id;

	@NotBlank(message = ValidationMsg.ORDER_DATE_ID_ERROR)
	private String orderDateId;

	private boolean isUseDiscount;  // 判斷有沒有使用9折劵

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

	public boolean isUseDiscount() {
		return isUseDiscount;
	}

	public void setUseDiscount(boolean isUseDiscount) {
		this.isUseDiscount = isUseDiscount;
	}

}
