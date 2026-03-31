package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

/* 付款完成: 會傳訂單編號、付款方法、序號 */
public class PayReq {

	@NotBlank(message = ValidationMsg.ID_ERROR)
	private String id;
	
	@NotBlank(message = ValidationMsg.ORDER_DATE_ID_ERROR)
	private String orderDateId;
	
	@NotBlank(message = ValidationMsg.PAYMENT_METHID_ERROR)
	private String paymentMethod;
	
	@NotBlank(message = ValidationMsg.TRANSACTION_ID_ERROR)
	private String transactionId;

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

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
}
