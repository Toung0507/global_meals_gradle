package com.example.global_meals_gradle.constants;

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

}
