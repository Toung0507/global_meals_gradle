package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "promotions_gifts")
public class PromotionsGifts {
	
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "promotions_id")
	private int promotionsId;
	
	@Column(name = "full_amount", precision = 12, scale = 4) // DECIMAL(12,4)
	private BigDecimal fullAmount;
	
	@Column(name = "gift_product_id")
	private int giftProductId;
	
	@Column(name = "is_active")
	private boolean active = true;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPromotionsId() {
		return promotionsId;
	}

	public void setPromotionsId(int promotionsId) {
		this.promotionsId = promotionsId;
	}

	public BigDecimal getFullAmount() {
		return fullAmount;
	}

	public void setFullAmount(BigDecimal fullAmount) {
		this.fullAmount = fullAmount;
	}

	public int getGiftProductId() {
		return giftProductId;
	}

	public void setGiftProductId(int giftProductId) {
		this.giftProductId = giftProductId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	

}
