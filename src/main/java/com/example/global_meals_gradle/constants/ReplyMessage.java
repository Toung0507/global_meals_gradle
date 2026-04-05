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
	NOT_DISCOUNT_ERROR(400, "Not Discount ERROR!!"),

	/* 艷羽 */
	// 贈品已下架（也是滿XX送XX這個規則下架了）或不存在
	GIFT_NOT_AVAILABLE(400, "Gift Not Available!!"), 
	NOT_REACH_FULLAMOUNT(400, "Not Reach fullamount!"),
	GIFT_SEND_LIGHT(400, "Gift Send light!!"), 
	CART_NOT_FOUND(400, "Cart Not Found!!"),

	
	// 促銷活動相關錯誤-致遠//////////
	PROMOTION_NOT_FOUND(400, "Promotion Not Found!!"),           // 找不到符合條件的促銷活動
	PROMOTION_GIFTS_NOT_FOUND(400, "Promotion Gift Not Found!!"), // 找不到符合條件的贈品
	MEMBER_COUPON_NOT_AVAILABLE(400, "Member Coupon Not Available!!"); // 會員沒有折扣券但傳入 useCoupon=true
	//////////////////////////////////////
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
