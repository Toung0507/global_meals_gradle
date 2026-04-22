package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

// 使用者選定贈品後點確認，前端呼叫 POST /cart/gift 時傳來的請求資料
public class CartSelectGiftReq {

	// 哪台購物車（必須是正整數）
	@Min(value = 1, message = ValidationMsg.CART_ID_MUST_BE_POSITIVE)
	private int cartId;

	// 是哪個會員在操作
	@Min(value = 1, message = ValidationMsg.MEMBER_ID_MUST_BE_POSITIVE)
	private int memberId;

	// 使用者選的是哪條贈品規則，對應 promotions_gifts.id 的主鍵
	// 必填！後端用這個值精準定位到「哪個活動的哪條贈品規則」
	// 優點：從這一個 ID 可以查到規則裡的 giftProductId、fullAmount、quantity、promotionsId
	// 完全不需要前端另外再傳 selectedGiftProductId 或 promotionId
	// 避免兩個活動都送同一個贈品時，不知道扣哪個活動名額的問題
	private int giftRuleId;

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

	public int getGiftRuleId() {
		return giftRuleId;
	}

	public void setGiftRuleId(int giftRuleId) {
		this.giftRuleId = giftRuleId;
	}
}
