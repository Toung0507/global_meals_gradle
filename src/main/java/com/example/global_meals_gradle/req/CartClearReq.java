package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

public class CartClearReq {
	
	@Min(value = 1, message = ValidationMsg.CART_ID_MUST_BE_POSITIVE)
	private int cartId;
	
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
