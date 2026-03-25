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
	
	@Column(name = "country")
	private String country;
	
	@Column(name = "countrt_code")
	private String countrtCode;
	
	@Column(name = "tax_rate", precision = 5, scale = 4) // DECIMAL(5,4)
	private BigDecimal taxRate;
	
	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
    @Column(name = "tax_type")
	private TaxType taxType;
	
	@Column(name = "created_at")
	private LocalDate createdAt;
	
	@Column(name = "updated_at")
	private LocalDate updatedAt;

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

	public String getCountrtCode() {
		return countrtCode;
	}

	public void setCountrtCode(String countrtCode) {
		this.countrtCode = countrtCode;
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
	
	
	
	

}
