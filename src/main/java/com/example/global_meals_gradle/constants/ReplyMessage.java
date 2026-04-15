package com.example.global_meals_gradle.constants;

public enum ReplyMessage {

	// 昱文
	// Service RegionsService
	REGIONS_ID_ERROR(400, "Regions Id Error!!"), //
	// Service GlobalAreaService
	GLOBAL_AREA_ID_ERROR(400, "Global Area Id Error!!"), //
	// Service MembersService
	PHONE_ERROR(400, "Phone Error!!"), //
	PHONE_HAS_EXISTED(400, "Phone Has Existed!!"), // 電話號碼已存在
	PASSWORD_ERROR(400, "Password Error!!"), //
	PASSWORD_NOT_ENTERED(400, "Password Not Entered!!"), // 密碼未填寫
	OLDPASSWORD_ERROR(400, "OldPassword Error!!"), //
	PHONE_OR_PASSWORD_ERROR(400, "Phone Or Password Error!!"), //
	UPDATE_FAILED(400, "Update Failed!!"), //
	GUEST_CANT_UPDATE(400, "Guest Cant Update!!"), // 訪客無法更新
	
	/* 劭頴 */
	SUCCESS(200, "Success!!"), //
	ORDER_NOT_FOUND(400, "Order Not Found!!"), //
	STOCK_NOT_ENOUGH(400, "Stock Not Enough!!"), //
	PRODUCT_NOT_FOUND(400, "Product Not Found!!"), //
	MEMBER_NOT_FOUND(400, "Member Not Found!!"), //
	ORDER_NUMBER_NOT_FOUND(400, "Order Number Not Found!!"), //
	ORDERS_STATUS_ERROR(400, "Orders Status Error!!"), //
	DISCOUNT_ERROR(400, "Disscount Error!!"), //
	MEMBER_ERROR(400, "Member ERROR!!"), //
	TOTAL_AMOUNT_ERROR(400, "Total Amount ERROR!!"), //
	NOT_DISCOUNT_ERROR(400, "Not Discount ERROR!!"), //

	// 促銷活動相關錯誤-致遠
	PROMOTION_NOT_FOUND(404, "Promotion Not Found!!"), // 找不到符合條件的促銷活動
	PROMOTION_GIFTS_NOT_FOUND(404, "Promotion Gift Not Found!!"), // 找不到符合條件的贈品
	MEMBER_COUPON_NOT_AVAILABLE(400, "Member Coupon Not Available!!"), // 會員沒有折扣券但傳入 useCoupon=true

	/* 艷羽 */
	// 贈品已下架（也是滿XX送XX這個規則下架了）或不存在
	GIFT_NOT_AVAILABLE(400, "Gift Not Available!!"), //
	NOT_REACH_FULLAMOUNT(400, "Not Reach fullamount!"), //
	GIFT_SEND_LIGHT(400, "Gift Send light!!"), //
	CART_NOT_FOUND(404, "Cart Not Found!!"),
	
	/*景翔*/
	// 基本欄位錯誤
	NAME_ERROR(400, "Name Error"), //名字錯誤
	ACCOUNT_ERROR(400, "Account Error"), //帳號錯誤
	NEW_PASSWORD_ERROR(400, "New Password Error"), //新密碼錯誤
	ROLE_ERROR(400, "Role Error"), //角色錯誤
	// 員工查詢 / 操作錯誤
	STAFF_ID_NOT_FOUND(404, "Staff Id Not Found"), //翻譯:未找到員工 ID
	ACCOUNT_NOT_FOUND(404, "Account Not Found"), //翻譯:帳戶未找到
	ACCOUNT_DISABLED(403, "Account Disabled"), //翻譯:帳戶已停用
	PASSWORD_MISMATCH(400, "Password Mismatch"), //翻譯:密碼不匹配
	// 權限相關錯誤
	OPERATE_ERROR(403, "Operate Error"), //翻譯:操作錯誤
	SELF_OPERATE_ERROR(400, "Self Operate Error "), //翻譯:自己操作錯誤
	NOT_LOGIN(401, "Not Login"), //未登入
	// 帳號生成錯誤
	REPEAT_ERROR(500, "Repeat Error"), // 翻譯:重複錯誤
	// 副店長相關
	DEPUTY_OPERATE_ERROR(403, "Deputy Operate Error"), //翻譯:副店長無法執行此操作
	TARGET_NOT_STAFF(400, "Target Not Staff"), //目標人物必須是員工
	TARGET_NOT_DEPUTY(400, "Target Not Deputy"), //目標人物不是副店長
	;
	

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