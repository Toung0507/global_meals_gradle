package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

/** 移除購物車內單一指定商品 */
// 在req裡面驗證是必要的
public class CartRemoveReq {

	@Min(value = 1, message = ValidationMsg.CART_ID_MUST_BE_POSITIVE)
	private int cartId;

	@Min(value = 1, message = ValidationMsg.PRODUCT_ID_MUST_BE_POSITIVE)
	private int productId;
	@Min(value = 1, message = ValidationMsg.MEMBER_ID_MUST_BE_POSITIVE)
	private int memberId;

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

}
