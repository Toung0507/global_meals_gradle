package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotions_gifts")
public class PromotionsGifts {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 對應 DB 的 AUTO_INCREMENT，新增時由資料庫自動產生 id，不需手動設定
	@Column(name = "id")
	private int id;

	@Column(name = "promotions_id")
	private int promotionsId;

	@Column(name = "full_amount", precision = 12, scale = 4) // DECIMAL(12,4)
	private BigDecimal fullAmount;

	@Column(name = "quantity")
	private int quantity;

	@Column(name = "gift_product_id")
	private int giftProductId;

	@Column(name = "is_active")
	private boolean active = true;

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

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
