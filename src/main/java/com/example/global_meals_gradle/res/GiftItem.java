package com.example.global_meals_gradle.res;

public class GiftItem {
	private int productId; // 贈品 ID
	private String productName; // 從 products 表關聯抓到的名稱
	private int quantity; // 數量 (固定為 1)

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
