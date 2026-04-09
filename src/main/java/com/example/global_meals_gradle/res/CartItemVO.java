package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

/** 購物車內單一商品的展示物件 (View Object) */
public class CartItemVO {

	private int detailId;

	private int productId;

	/** 商品名稱 (從 Products 表關聯查詢出來的) */
	private String productName;

	private int quantity;

	/** 單價 (當時加入購物車的價格快照) */
	private BigDecimal price;

	/** 是否為贈品 (true = 不能被手動刪除的滿額贈) */
	private boolean isGift;

	/** 滿額贈的說明 (EX: "滿1000贈薯條") */
	private String discountNote;

	/** 單品的小計金額 (單價 × 數量，已幫前端先算好！) */
	private BigDecimal lineTotal;

	public int getDetailId() {
		return detailId;
	}

	public void setDetailId(int detailId) {
		this.detailId = detailId;
	}

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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public boolean isGift() {
		return isGift;
	}

	public void setGift(boolean gift) {
		isGift = gift;
	}

	public String getDiscountNote() {
		return discountNote;
	}

	public void setDiscountNote(String discountNote) {
		this.discountNote = discountNote;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

	public void setLineTotal(BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}
}
