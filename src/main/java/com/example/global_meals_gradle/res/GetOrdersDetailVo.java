package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

// 查詢訂單用的內層的訂單產品明細
public class GetOrdersDetailVo {

	private String name; // 產品名稱 Products

	private int quantity;

	private BigDecimal price;

	private boolean gift;

	private String discountNote;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public boolean isGift() {
		return gift;
	}

	public void setGift(boolean gift) {
		this.gift = gift;
	}

	public String getDiscountNote() {
		return discountNote;
	}

	public void setDiscountNote(String discountNote) {
		this.discountNote = discountNote;
	}

}
