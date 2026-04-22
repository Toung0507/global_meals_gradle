package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRates {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "currency_code", unique = true)
	private String currencyCode;

	@Column(name = "rate_to_twd", precision = 12, scale = 6) // DECIMAL(12,6)
	private BigDecimal rateToTwd;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public BigDecimal getRateToTwd() {
		return rateToTwd;
	}

	public void setRateToTwd(BigDecimal rateToTwd) {
		this.rateToTwd = rateToTwd;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
