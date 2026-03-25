package com.example.global_meals_gradle.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_generated")
public class AiGenerated {
	
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "product_id")
	private int productId;
	
	// 使用 @Lob 處理長文字
    @Lob
    @Column(name = "generated_description", columnDefinition = "TEXT")
	private String generatedDescription;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

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

	public String getGeneratedDescription() {
		return generatedDescription;
	}

	public void setGeneratedDescription(String generatedDescription) {
		this.generatedDescription = generatedDescription;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
    

}
