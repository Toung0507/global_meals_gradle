package com.example.global_meals_gradle.res;

public class MonthlyProductsSalesVo {
	private String productName; // 商品名稱（從 products 表撈）
	private int totalQuantity; // 這個月賣出了幾份（從 order_cart_details 加總）
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public int getTotalQuantity() {
		return totalQuantity;
	}
	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	
}
