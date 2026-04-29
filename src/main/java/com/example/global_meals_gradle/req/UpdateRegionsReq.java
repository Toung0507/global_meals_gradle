package com.example.global_meals_gradle.req;

import java.math.BigDecimal;

import com.example.global_meals_gradle.constants.ValidationMsg;
import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateRegionsReq {
	
	@Min(value = 1, message = ValidationMsg.REGIONS_ID_ERROR)
	private int id;
	
	@JsonAlias("tax_rate")
	private BigDecimal taxRate;
    
    @JsonAlias("tax_type")
	private String taxType;
	
    @JsonAlias("usage_cap")
    private Integer usageCap; // 改用 Integer 以便判斷 null

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Integer getUsageCap() {
		return usageCap;
	}

	public void setUsageCap(Integer usageCap) {
		this.usageCap = usageCap;
	}

}
