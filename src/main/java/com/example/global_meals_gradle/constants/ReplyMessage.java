package com.example.global_meals_gradle.constants;

public enum ReplyMessage {
	
	// 昱文
	// Service RegionsService
	REGIONS_ID_ERROR(400, "Regions Id Error!!"),//
	// Service GlobalAreaService
	GLOBAL_AREA_ID_ERROR(400, "Global Area Id Error!!"),//
	
	
	// 劭穎
	SUCCESS(200, "Success!!"),
	ORDER_NOT_FOUND(400, "Order Not Found!!"),
	STOCK_NOT_ENOUGH(400, "Stock Not Enough!!"),
	PRODUCT_NOT_FOUND(400, "Product Not Found!!"),
	MEMBER_NOT_FOUND(400, "Member Not Found!!"),
	ORDER_NUMBER_NOT_FOUND(400, "Order Number Not Found!!"),

	ORDERS_STATUS_ERROR(400, "Orders Status Error!!"),
	
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