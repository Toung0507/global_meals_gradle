package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** 同步購物車商品（加入商品 / 更改數量（包括刪除單一商品） */
public class CartSyncReq {

	// null 表示新建購物車，有值表示已有購物車 id（後端 CartService 依此判斷）
	// 不加 @Min，因為合法狀態就是 null（新建）或正整數（已有）
	private Integer cartId;

	/*
	 * 已有購物車（cartId 有值，舊車）： 後端只需要去 order_cart_details 裡加一條明細，主表 order_cart 不需要動，所以
	 * globalAreaId 傳什麼都不影響，傳 null 後端一樣會忽略它。 這個欄位只在建新車的那次有用。
	 */
	@Min(value = 1, message = ValidationMsg.GLOBAL_AREA_ID_MUST_BE_POSITIVE)
	private Integer globalAreaId;
	
	private String operationType;

	// 只要這個欄位在某些情境下會是空的（沒有值），我們就必須用大寫的 Integer，如果用 int ，是用0去撈資料
	@Min(value = 1, message = ValidationMsg.STAFF_ID_MUST_BE_POSITIVE)
	private Integer staffId;

	// 訪客固定傳 1，一般會員傳自己的 id，所以最小值是 1
	@Min(value = 1, message = ValidationMsg.MEMBER_ID_MUST_BE_POSITIVE)
	private int memberId;
	// 商品ID必填（必須是正整數）；用 int 讓 Spring 無法偷塞 null，@Min(1) 擋掉 0 和負數
	@Min(value = 1, message = ValidationMsg.PRODUCT_ID_MUST_BE_POSITIVE)
	private int productId;

	@Min(value = 0, message = ValidationMsg.QUANTITY_CANT_BE_NEGATIVE)
	private int quantity;

	public Integer getCartId() {
		return cartId;
	}

	public void setCartId(Integer cartId) {
		this.cartId = cartId;
	}

	public Integer getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(Integer globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public Integer getStaffId() {
		return staffId;
	}

	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

}
