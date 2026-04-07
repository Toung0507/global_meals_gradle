package com.example.global_meals_gradle.constants;

import org.springframework.util.StringUtils;

public enum TaxType {

	INCLUSIVE("INCLUSIVE"), //
	EXCLUSIVE("EXCLUSIVE");

	private String taxType;

	private TaxType(String taxType) {
		this.taxType = taxType;
	}

	public String getTaxType() {
		return taxType;
	}

	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}
	
	// 參數檢查
	public static boolean check(String input) {
		
		if(!StringUtils.hasText(input)) {
			return false;
		}
		for(TaxType type : values()) {
			if(input.equalsIgnoreCase(type.getTaxType())) {
				return true;
			}
		}
		return false;
	}
	

}
