package com.example.global_meals_gradle.req;

public class PromotionsReq {
	
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
	private int cartId;        // 購物車 ID
	private int memberId;      // 會員 ID (訪客預設為 1)
    private boolean useCoupon; // 使用者是否勾選「使用 8 折券」

}
