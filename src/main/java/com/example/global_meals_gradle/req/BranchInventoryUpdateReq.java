package com.example.global_meals_gradle.req;

import java.math.BigDecimal;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public class BranchInventoryUpdateReq {

	@Min(value = 1, message = ValidationMsg.PRODUCT_ID_MUST_BE_POSITIVE)
	private int productId;

	@Min(value = 1, message = ValidationMsg.PRODUCT_AREA_ID_INVALID)
	private int globalAreaId;

	@Min(value = 0, message = ValidationMsg.QUANTITY_CANT_BE_NEGATIVE)
	private int stockQuantity;

	@DecimalMin(value = "0.01", message = ValidationMsg.PRODUCT_PRICE_INVALID)
	private BigDecimal basePrice;

	@DecimalMin(value = "0.01", message = ValidationMsg.PRODUCT_COST_PRICE_INVALID)
	private BigDecimal costPrice;

	@Min(value = 1, message = ValidationMsg.MAX_ORDER_QUANTITY)
	private int maxOrderQuantity;

	private boolean active;

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

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public int getMaxOrderQuantity() {
		return maxOrderQuantity;
	}

	public void setMaxOrderQuantity(int maxOrderQuantity) {
		this.maxOrderQuantity = maxOrderQuantity;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
