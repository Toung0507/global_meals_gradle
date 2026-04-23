package com.example.global_meals_gradle.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "promotions")
public class Promotions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "name")
	private String name;

	@Column(name = "start_time")
	private LocalDate startTime;

	@Column(name = "end_time")
	private LocalDate endTime;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	// NULL = 全球活動（老闆建立，所有分店可見）; 有值 = 分店專屬活動
	@Column(name = "global_area_id")
	private Integer globalAreaId;

	// 日文活動名稱（可為 null，前端顯示時 fallback 至 name）
	@Column(name = "name_jp")
	private String nameJP;

	// 韓文活動名稱（可為 null，前端顯示時 fallback 至 name）
	@Column(name = "name_kr")
	private String nameKR;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "promotion_img", columnDefinition = "MEDIUMBLOB")
	private byte[] promotionImg;


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

	public LocalDate getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDate startTime) {
		this.startTime = startTime;
	}

	public LocalDate getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDate endTime) {
		this.endTime = endTime;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getGlobalAreaId() { return globalAreaId; }
	public void setGlobalAreaId(Integer globalAreaId) { this.globalAreaId = globalAreaId; }

	public String getNameJP() { return nameJP; }
	public void setNameJP(String nameJP) { this.nameJP = nameJP; }

	public String getNameKR() { return nameKR; }
	public void setNameKR(String nameKR) { this.nameKR = nameKR; }

	public byte[] getPromotionImg() {
		return promotionImg;
	}

	public void setPromotionImg(byte[] promotionImg) {
		this.promotionImg = promotionImg;
	}

}
