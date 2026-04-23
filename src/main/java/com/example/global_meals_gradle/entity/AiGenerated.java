package com.example.global_meals_gradle.entity;

import java.time.LocalDateTime;

import com.example.global_meals_gradle.constants.AiType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_generated")
public class AiGenerated {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Enumerated(EnumType.STRING)
	@Column(name = "ai_type")
	private AiType aiType;

	@Column(name = "reference_id")
	private int referenceId;

	@Lob
	@Column(name = "generated_description", columnDefinition = "TEXT")
	private String generatedDescription;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	public AiGenerated() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public AiType getAiType() {
		return aiType;
	}

	public void setAiType(AiType aiType) {
		this.aiType = aiType;
	}

	public int getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(int referenceId) {
		this.referenceId = referenceId;
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
