package com.example.global_meals_gradle.constants;

public enum ReplyMessage {

	SUCCESS(200, "Success!!"), //
	ORDER_NOT_FOUND(400, "Order Not Found!!"), //
	STOCK_NOT_ENOUGH(400, "Stock Not Enough!!"), //
	PRODUCT_NOT_FOUND(400, "Product Not Found!!");

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
