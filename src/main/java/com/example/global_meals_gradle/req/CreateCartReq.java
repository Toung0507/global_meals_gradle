package com.example.global_meals_gradle.req;

import java.util.List;

import com.example.global_meals_gradle.constants.OperationType;
import com.example.global_meals_gradle.entity.OrderCartDetails;

public class CreateCartReq {
	
	private int globalAreaId;
	
	private int operation;
	
	private OperationType operationType;
	
	private List<OrderCartDetails> orderCartDetailsList;

	public int getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public List<OrderCartDetails> getOrderCartDetailsList() {
		return orderCartDetailsList;
	}

	public void setOrderCartDetailsList(List<OrderCartDetails> orderCartDetailsList) {
		this.orderCartDetailsList = orderCartDetailsList;
	}

}
