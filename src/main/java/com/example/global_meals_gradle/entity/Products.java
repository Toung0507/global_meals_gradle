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

	// 多個商品對應到一個分類
	@ManyToOne(fetch = FetchType.EAGER) // 這是一個關聯。我現在這個商品物件裡，想塞進去另一個分類物件。
	@JoinColumn(name = "category_id") // 分類物件是誰，請去 products 表裡面看 category_id 那一欄存的數字，再拿那個數字去 category 表找對應的人。
	private Category category;

	// 多個商品對應到一個風格
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "style_id") // 對應資料庫的 style_id 外鍵
	private Style style;
	
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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
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