package com.example.global_meals_gradle.constants;

public enum OperationType {
	
	STAFF("STAFF"), //
	CUSTOMER("CUSTOMER");
	
	private String operationType;

	private OperationType(String operationType) {
		this.operationType = operationType;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	

}
