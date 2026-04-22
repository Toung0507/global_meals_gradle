package com.example.global_meals_gradle.constants;

public enum AiType {

	PRODUCT_DESC("PRODUCT_DESC"), //
	PROMO_COPY("PROMO_COPY");

	private String AiType;

	private AiType(String aiType) {
		AiType = aiType;
	}

	public String getAiType() {
		return AiType;
	}

	public void setAiType(String aiType) {
		AiType = aiType;
	}

}
