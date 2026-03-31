package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.Min;

/** 移除購物車內單一指定商品 */
// 在req裡面驗證是必要的
public class CartRemoveReq {

	@Min(value = 1, message = "購物車 ID 必須大於 0")
	private int cartId;

	@Min(value = 1, message = "商品 ID 必須大於 0")
	private int productId;

	private Integer memberId;

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

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

}
