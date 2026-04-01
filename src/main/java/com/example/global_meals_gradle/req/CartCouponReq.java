package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.Min;

/**
 * 切換折價券使用狀態 對應 API：POST /api/cart/coupon-----------放在訂單那邊使用了
 * 
 */
//public class CartCouponReq {
//
//	// 哪台購物車要套用折價券
//	@Min(value = 1, message = "購物車 ID 必須大於 0")
//	private int cartId;
//
//	// 哪位會員（只有會員才有折價券，訪客不會呼叫這支 API）
//	@Min(value = 1, message = "會員 ID 必須大於 0")
//	private int memberId;
//
//	// true = 套用折價券（小計自動打折）；false = 不使用
//	private boolean useCoupon;
//
//	public int getCartId() {
//		return cartId;
//	}
//
//	public void setCartId(int cartId) {
//		this.cartId = cartId;
//	}
//
//	public int getMemberId() {
//		return memberId;
//	}
//
//	public void setMemberId(int memberId) {
//		this.memberId = memberId;
//	}
//
//	public boolean isUseCoupon() {
//		return useCoupon;
//	}
//
//	public void setUseCoupon(boolean useCoupon) {
//		this.useCoupon = useCoupon;
//	}
//}
