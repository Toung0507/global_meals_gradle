package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.Min;

/**
 * 促銷活動請求參數
 * 呼叫方負責傳入這三個值，Service 不自己去查購物車或會員狀態
 */
public class PromotionsReq {

	// 購物車 ID：用來讓回傳的 Res 對應是哪一張購物車，Service 本身不用這個 ID 去查任何東西
	// 最小值為 1，0 或負數視為無效
	@Min(value = 1, message = "Cart ID must be at least 1")
	private int cartId;

	// 會員 ID：
	//   = 1 表示訪客，訪客沒有折扣資格，Service 會直接跳過折扣判斷
	//   > 1 表示一般會員，才會去查 members 表判斷是否有折扣券
	// 最小值為 1，0 或負數視為無效
	@Min(value = 1, message = "Member ID must be at least 1")
	private int memberId;

	// 前端勾選「是否使用 8 折券」：
	//   true  = 使用者有勾選，但還要在 Service 確認 members.is_discount = 1 才會實際打折
	//           若 is_discount = 0 卻傳 true，Service 會擋下並回傳 MEMBER_COUPON_NOT_AVAILABLE
	//   false = 使用者沒勾選，即使有券也不打折
	//   boolean 只有 true/false，不需要 annotation 驗證
	private boolean useCoupon;

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public boolean isUseCoupon() {
		return useCoupon;
	}

	public void setUseCoupon(boolean useCoupon) {
		this.useCoupon = useCoupon;
	}

}
