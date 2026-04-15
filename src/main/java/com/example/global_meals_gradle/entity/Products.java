package com.example.global_meals_gradle.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Products {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 為了跟 service 的 .save() 做搭配，跟 JPA 說我這一行一定是自動給號
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
	private byte[] foodImg;

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

	public byte[] getFoodImg() {
		return foodImg;
	}

	public void setFoodImg(byte[] foodImg) {
		this.foodImg = foodImg;
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
