package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

public class CartSelectGiftReq {

	@Min(value = 1, message = ValidationMsg.CART_ID_MUST_BE_POSITIVE)
	private int cartId;

	private int memberId;
//	選不要贈品就是Null
	private Integer selectedGiftProductId;

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

	public Integer getSelectedGiftProductId() {
		return selectedGiftProductId;
	}

	public void setSelectedGiftProductId(Integer selectedGiftProductId) {
		this.selectedGiftProductId = selectedGiftProductId;
	}

}
