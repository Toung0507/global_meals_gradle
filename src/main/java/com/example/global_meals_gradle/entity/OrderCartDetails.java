package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "order_cart_details")
public class OrderCartDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 當資料庫的id欄位設為AI時，Entity 也必須告訴 JPA：「這個欄位的值由資料庫自己產生」。如果不加，JPA
														// 就會認為你要手動塞一個 ID 給它，導致新增資料時出錯。
	@Column(name = "id")
	private int id;

	@Column(name = "order_cart_id")
	private int orderCartId;

	@Column(name = "product_id")
	private int productId;

	@Column(name = "quantity")
	private int quantity;

	@Column(name = "price", precision = 12, scale = 4) // DECIMAL(12,4)
	private BigDecimal price;

	@Column(name = "is_gift")
	private boolean gift;

	@Column(name = "discount_note")
	private String discountNote;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrderCartId() {
		return orderCartId;
	}

	public void setOrderCartId(int orderCartId) {
		this.orderCartId = orderCartId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
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
