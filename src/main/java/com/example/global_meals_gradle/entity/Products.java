package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "products")
public class Products {
	
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "name")
	private String name;

	@Column(name = "category")
	private String category;
	
	// 使用 @Lob 告訴 JPA 這是大型物件 (Large Object)
    @Lob
    @Basic(fetch = FetchType.LAZY) // 只有在呼叫 getfoodImg() 時才去抓資料庫
    @Column(name = "food_img", columnDefinition = "MEDIUMBLOB")
	private byte[] foofImg;
    
    @Column(name = "region_country")
    private String regionCountry;
    
    @Column(name = "base_price", precision = 12, scale = 2) // DECIMAL(12,2)
    private BigDecimal basePrice;
    
    // 加上 columnDefinition 確保資料庫端是 UNSIGNED
    @Min(value = 0, message = ValidationMsg.QUANTITY_CANT_BE_NEGATIVE) // 庫存不能為負數
    @Column(name = "stock_quantity", columnDefinition = "INT UNSIGNED")
    private int stockQuantity;
    
    @Column(name = "max_order_quantity", columnDefinition = "INT UNSIGNED")
    private int maxOrderQuantity;
    
    @Column(name = "version")
    private int version;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_active")
    private boolean active;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public byte[] getFoofImg() {
		return foofImg;
	}

	public void setFoofImg(byte[] foofImg) {
		this.foofImg = foofImg;
	}

	public String getRegionCountry() {
		return regionCountry;
	}

	public void setRegionCountry(String regionCountry) {
		this.regionCountry = regionCountry;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}
	
}
