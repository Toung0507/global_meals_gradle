package com.example.global_meals_gradle.res;

public class AiRes extends BasicRes {
	private String generatedDescription;

	public AiRes(int code, String message) {
		super(code, message);
	}

	public AiRes(int code, String message, String content) {
		super(code, message);
		this.generatedDescription = content;
	}

	public String getGeneratedDescription() {
		return generatedDescription;
	}

	public void setGeneratedDescription(String content) {
		this.generatedDescription = content;
	}
}
