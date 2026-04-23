package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "branch_inventory")
public class BranchInventory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "product_id")
	private int productId;

	@Column(name = "global_area_id")
	private int globalAreaId;

	// 加上 columnDefinition 確保資料庫端是 UNSIGNED
	@Min(value = 0, message = ValidationMsg.QUANTITY_CANT_BE_NEGATIVE) // 庫存不能為負數
	@Column(name = "stock_quantity", columnDefinition = "INT UNSIGNED")
	private int stockQuantity;

	@Column(name = "base_price", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal basePrice;

	@Min(value = 1, message = ValidationMsg.MAX_ORDER_QUANTITY) // 單次最大購買量至少為１
	@Column(name = "max_order_quantity", columnDefinition = "INT UNSIGNED")
	private int maxOrderQuantity;

	@Column(name = "version")
	private int version;

	@UpdateTimestamp // 每次更新資料時，Hibernate 會自動幫你填入當前時間
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}

	public int getMaxOrderQuantity() {
		return maxOrderQuantity;
	}

	public void setMaxOrderQuantity(int maxOrderQuantity) {
		this.maxOrderQuantity = maxOrderQuantity;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}