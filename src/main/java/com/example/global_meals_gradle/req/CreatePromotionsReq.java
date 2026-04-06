package com.example.global_meals_gradle.req;

import java.time.LocalDate;
import java.util.List;

import com.example.global_meals_gradle.entity.PromotionsGifts;

/* 建立優惠活動 */
public class CreatePromotionsReq {

	private String name;

	private LocalDate startTime;

	private LocalDate endTime;

	private int maxExchange = -1;

	private List<PromotionsGifts> promotionsGiftsList;

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

	public List<PromotionsGifts> getPromotionsGiftsList() {
		return promotionsGiftsList;
	}

	public void setPromotionsGiftsList(List<PromotionsGifts> promotionsGiftsList) {
		this.promotionsGiftsList = promotionsGiftsList;
	}

}
