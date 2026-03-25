package com.example.global_meals_gradle.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "promotions")
public class Promotions {
	
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "start_time")
	private LocalDate startTime;
	
	@Column(name = "end_time")
	private LocalDate endTime;
	
	@Column(name = "max_exchange")
	private int maxExchange = -1;
	
	@Column(name = "exchange_count")
	private int exchangeCount;

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

	public int getMaxExchange() {
		return maxExchange;
	}

	public void setMaxExchange(int maxExchange) {
		this.maxExchange = maxExchange;
	}

	public int getExchangeCount() {
		return exchangeCount;
	}

	public void setExchangeCount(int exchangeCount) {
		this.exchangeCount = exchangeCount;
	}
	

}
