package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.global_meals_gradle.constants.TaxType;

import jakarta.persistence.*;

@Entity
@Table(name = "regions")
public class Regions {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "country", unique = true)
	private String country;

	@Column(name = "currency_code") // 國家匯率代碼(TWD，JPY、KRW....)
	private String currencyCode;
	
	@Column(name = "country_code") // 國家代碼(TW、JP、KR....)
	private String countryCode;

	@Column(name = "country_code", length = 3) // 國家代碼(TW、JP、KR....)
	private String countryCode;

	@Column(name = "tax_rate", precision = 5, scale = 4) // DECIMAL(5,4)
	private BigDecimal taxRate;

	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
	@Column(name = "tax_type")
	private TaxType taxType;

	@Column(name = "created_at")
	private LocalDate createdAt;

	@Column(name = "updated_at")
	private LocalDate updatedAt;

	// 折扣上限：各國貨幣單位不同，數字不同但實際價值相當
	// 例如台灣 200、日本 1000、韓國 10000
	// 對應 regions.usage_cap 欄位
	@Column(name = "usage_cap")
	private int usageCap;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	
	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public BigDecimal getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDate updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getUsageCap() {
		return usageCap;
	}

	public void setUsageCap(int usageCap) {
		this.usageCap = usageCap;
	}

}
