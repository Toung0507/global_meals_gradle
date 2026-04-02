package com.example.global_meals_gradle.constants;

public enum ReplyMessage {

	/* 劭頴 */
	SUCCESS(200, "Success!!"),
	ORDER_NOT_FOUND(400, "Order Not Found!!"),
	STOCK_NOT_ENOUGH(400, "Stock Not Enough!!"),
	PRODUCT_NOT_FOUND(400, "Product Not Found!!"),
	MEMBER_NOT_FOUND(400, "Member Not Found!!"),
	ORDER_NUMBER_NOT_FOUND(400, "Order Number Not Found!!"),
	ORDERS_STATUS_ERROR(400, "Orders Status Error!!"),
	DISCOUNT_ERROR(400, "Disscount Error!!"),
	MEMBER_ERROR(400, "Member ERROR!!"),
	TOTAL_AMOUNT_ERROR(400, "Total Amount ERROR!!"),
	NOT_DISCOUNT_ERROR(400, "Not Discount ERROR!!");
	
	
	private int code;

	private String message;

	private ReplyMessage(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
