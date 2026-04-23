package com.example.global_meals_gradle.res;

// 今日訂單中的單一商品明細
public class TodayOrderDetailVo {

	private String productName;
	private int quantity;
	private boolean gift;

	public TodayOrderDetailVo() {}

	public TodayOrderDetailVo(String productName, int quantity, boolean gift) {
		this.productName = productName;
		this.quantity = quantity;
		this.gift = gift;
	}

	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }

	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }

	public boolean isGift() { return gift; }
	public void setGift(boolean gift) { this.gift = gift; }
}
