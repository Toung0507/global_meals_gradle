package com.example.global_meals_gradle.constants;

import com.fasterxml.jackson.annotation.JsonFormat;

//讓 Jackson 反序列化時忽略大小寫
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public enum PayStatus {

	UNPAID("UNPAID"), // 未付款
	PAID("PAID"), // 已付款
	REFUND_PROCESSING("REFUND PROCESSING"), // 退款審核中
	REFUNDED("REFUNDED"); // 退款

	private String payStatus;

	private PayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

}
