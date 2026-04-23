package com.example.global_meals_gradle.req;

import java.math.BigDecimal;

import com.example.global_meals_gradle.constants.ValidationMsg;
import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpsertRegionsTaxReq {
	
	/* @NotBlank: 限制屬性值不能是 1. 空字串 2. 全空白字串 3. null */
	
	@NotBlank(message = ValidationMsg.COUNTRY_ERROR)
	private String country;
	
	@NotBlank(message = ValidationMsg.CURRENCY_CODE_ERROR)
	@JsonAlias("currency_code")
    private String currencyCode;
	
	@NotBlank(message = ValidationMsg.COUNTRY_CODE_ERROR)
	@JsonAlias("country_code")
    private String countryCode;
    
	@NotNull(message = ValidationMsg.TAX_RATE_ERROR) // BigDecimal 建議用 NotNull
	@Min(value = 0, message = ValidationMsg.TAX_RATE_ERROR) // 至少 >=0
	@JsonAlias("tax_rate")
	private BigDecimal taxRate;
    
    @NotBlank(message = ValidationMsg.TAX_TYPE_ERROR)
    @JsonAlias("tax_type")
	private String taxType;

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

	public BigDecimal getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}

	public String getTaxType() {
		return taxType;
	}

	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}

	
}
