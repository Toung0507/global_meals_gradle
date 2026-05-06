package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;
import jakarta.validation.constraints.NotBlank;

// POS 機現場點餐結帳
// 購物車ID 會員ID 金額 付款方式 交易序號 購物車細項 
public class PayForPOSReq extends CreateOrdersReq {
	
	@NotBlank(message = ValidationMsg.PAYMENT_METHID_ERROR)
	private String paymentMethod;

	private String transactionId;

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
