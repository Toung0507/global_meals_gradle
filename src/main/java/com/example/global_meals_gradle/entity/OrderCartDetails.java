package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "order_cart_details")
public class OrderCartDetails {
	
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "order_cart_date_id")
	private String orderCartDateId;
	
	@Column(name = "order_cart_id")
	private String orderCartId;
	
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

	public String getOrderCartDateId() {
		return orderCartDateId;
	}

	public void setOrderCartDateId(String orderCartDateId) {
		this.orderCartDateId = orderCartDateId;
	}

	public String getOrderCartId() {
		return orderCartId;
	}

	public void setOrderCartId(String orderCartId) {
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
