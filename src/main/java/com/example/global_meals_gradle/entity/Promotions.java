package com.example.global_meals_gradle.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "promotions")
public class Promotions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 對應 DB 的 AUTO_INCREMENT，新增時自動產生 id
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
	

}
