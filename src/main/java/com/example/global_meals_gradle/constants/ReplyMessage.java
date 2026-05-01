package com.example.global_meals_gradle.constants;

public enum ReplyMessage {

	/* 昱文 */
	// Service RegionsService
	REGIONS_ID_ERROR(400, "Regions Id Error!!"), //
	REGIONS_ID_NOT_FOUND(404, "Regions Id Not Found!!"), //
	TAX_TYPE_ERROR(400, "Tax Type Error!!"), //
	TAX_RATE_ERROR(400, "Tax Rate Error!!"), //
	// Service GlobalAreaService
	GLOBAL_AREA_ID_ERROR(400, "Global Area Id Error!!"), //
	GLOBAL_AREA_ID_NOT_FOUND(404, "Global Area Id Not Found!!"), //
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
	ORDER_NOT_FOUND(404, "Order Not Found!!"), //
	STOCK_NOT_ENOUGH(400, "Stock Not Enough!!"), //
	PRODUCT_NOT_FOUND(404, "Product Not Found!!"), //
	MEMBER_NOT_FOUND(404, "Member Not Found!!"), //
	ORDER_NUMBER_NOT_FOUND(404, "Order Number Not Found!!"), //
	ORDERS_STATUS_ERROR(400, "Orders Status Error!!"), //
	DISCOUNT_ERROR(400, "Disscount Error!!"), //
	MEMBER_ERROR(400, "Member ERROR!!"), //
	TOTAL_AMOUNT_ERROR(400, "Total Amount ERROR!!"), //
	NOT_DISCOUNT_ERROR(400, "Not Discount ERROR!!"), //
	REPORTS_NOT_FOUND(404, "Reports Not Found!!"), //
	PERMISSION_DENIED(403, "Permission Denied"), // 權限不足
	BRANCHES_DIFFERENT(400, "Branches Different!!"), // 員工分店與訂單分店不同
	UPDATE_PAY_STATUS_ERROR(400, "Update Pay Status Error!!"), // 付款狀態更新失敗
	PAY_PAYMENT_METHOD_ERROR(400, "Pay Payment Method Error!!"), // 付款方式錯誤

	/* 致遠 */
	// 促銷活動相關錯誤
	PROMOTION_NOT_FOUND(404, "Promotion Not Found!!"), // 找不到符合條件的促銷活動
	PROMOTION_GIFTS_NOT_FOUND(404, "Promotion Gift Not Found!!"), // 找不到符合條件的贈品（結帳驗證失敗）
	MEMBER_COUPON_NOT_AVAILABLE(400, "Member Coupon Not Available!!"), // 會員沒有折扣券但傳入 useCoupon=true
	GLOBAL_AREA_NOT_FOUND(404, "Global Area Not Found!!"), // 找不到對應的分店
	PROMOTION_DATE_ERROR(400, "Promotion Date Error!!"), // 活動日期不合法（開始日期已過期或結束日期早於開始日期）
	PROMOTION_GIFT_PARAM_ERROR(400, "Promotion Gift Param Error!!"), // 贈品參數不合法（門檻金額 <= 0 或數量 = 0）
	PROMOTION_NAME_ERROR(400, "Promotion Name Error!!"), // 活動名稱為空或空白
	PROMOTION_IMG_REQUIRED(400, "Promotion Image Required!!"), // 建立活動時圖片為必填
	COUNTRY_ERROR(400, "Country Error!!"), // 使用折扣券時國家欄位為空

	/* 艷羽 */
	// 贈品已下架（也是滿XX送XX這個規則下架了）或不存在
	GIFT_NOT_AVAILABLE(400, "Gift Not Available!!"), //
	NOT_REACH_FULLAMOUNT(400, "Not Reach fullamount!"), //
	GIFT_SEND_LIGHT(400, "Gift Send light!!"), //
	CART_NOT_FOUND(404, "Cart Not Found!!"),
	// 購物車
	// 已結帳的購物車不允許再修改（removeItem / clearCart / syncItem 分支B 用）
	CART_ALREADY_CHECKED_OUT(400, "Cart Already Checked Out!!"),
	// operationType 傳了非法值（不在 OperationType 枚舉裡的字串）
	INVALID_OPERATION_TYPE(400, "Invalid Operation Type!!"),

	/* 景翔 */

	/* 景翔 */
	// 基本欄位錯誤
	NAME_ERROR(400, "Name Error"), // 名字錯誤
	ACCOUNT_ERROR(400, "Account Error"), // 帳號錯誤
	NEW_PASSWORD_ERROR(400, "New Password Error"), // 新密碼錯誤
	ROLE_ERROR(400, "Role Error"), // 角色錯誤
	// 員工查詢 / 操作錯誤
	STAFF_ID_NOT_FOUND(404, "Staff Id Not Found"), // 翻譯:未找到員工 ID
	ACCOUNT_NOT_FOUND(404, "Account Not Found"), // 翻譯:帳戶未找到
	ACCOUNT_DISABLED(403, "Account Disabled"), // 翻譯:帳戶已停用
	PASSWORD_MISMATCH(400, "Password Mismatch"), // 翻譯:密碼不匹配
	// 權限相關錯誤
	OPERATE_ERROR(403, "Operate Error"), // 翻譯:操作錯誤
	SELF_OPERATE_ERROR(400, "Self Operate Error "), // 翻譯:自己操作錯誤
	NOT_LOGIN(401, "Not Login"), // 未登入
	// 帳號生成錯誤
	REPEAT_ERROR(500, "Repeat Error"), // 翻譯:重複錯誤
	// 副店長相關
	DEPUTY_OPERATE_ERROR(403, "Deputy Operate Error"), // 翻譯:副店長無法執行此操作
	TARGET_NOT_STAFF(400, "Target Not Staff"), // 目標人物必須是員工
	TARGET_NOT_DEPUTY(400, "Target Not Deputy"), // 目標人物不是副店長
	PROMOTE_TARGET_ERROR(400, "Promote Target Error"), // 提升目標錯誤
	// 首次登入要修改密碼
	FIRST_LOGIN_CHANGE_PASSWORD(403, "First Login Change Password"),
	OLD_AND_NEW_PASSWORD_SAME(400, "Old And New Password Same"), // 新舊密碼相同
	INITAL_PASSWORD_SAME(400,"Inital Password Same"),// 初始密碼相同

	/* 思云 */
	// 商品表 & 庫存表
	PRODUCT_EXISTS(400, "Product name already exists!"), //
	IMAGE_TOO_LARGE(400, "Image size exceeds 5MB!"), //
	INVALID_PRICE(400, "Price must be between 1 and 5000!"), //
	BRANCH_NOT_FOUND(500, "No branches found to initialize inventory!"), //
	IMAGE_ERROR(500, "Image processing error!"), //
	SYSTEM_ERROR(500, "System error occurred: "), // 後面會動態接訊息
	PRODUCT_CREATE_SUCCESS(200, "Product created and inventory synced successfully!"), //
	PRODUCT_UPDATE_SUCCESS(200, "Product Update successfully!"), //
	PRODUCT_DELETE_SUCCESS(200, "Product delete successfully!"), //
	INVENTORY_NOT_FOUND(404, "Inventory not found for this product/branch!"), //
	INVENTORY_UPDATE_SUCCESS(200, "Inventory Update successfully!");

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
