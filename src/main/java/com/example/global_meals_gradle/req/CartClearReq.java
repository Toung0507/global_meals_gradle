package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

public class CartClearReq {

	// 用 Integer（可 null）代替 primitive int，避免 JSON null 被反序列化為 0 觸發 @Min
	private Integer cartId;

	@Min(value = 1, message = ValidationMsg.MEMBER_ID_MUST_BE_POSITIVE)
	private int memberId;

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

}
