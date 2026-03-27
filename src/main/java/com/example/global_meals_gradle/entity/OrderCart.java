package com.example.global_meals_gradle.entity;

import java.time.LocalDateTime;

import com.example.global_meals_gradle.constants.OperationType;

import jakarta.persistence.*;

@Entity
@Table(name = "order_cart")
public class OrderCart {
	
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "global_area_id")
	private int globalAreaId;
	
	@Column(name = "operation")
	private int operation;
	
	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
    @Column(name = "operation_type")
	private OperationType operationType;
	
	@Column(name = "order_time")
	private LocalDateTime orderTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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

	public LocalDateTime getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(LocalDateTime orderTime) {
		this.orderTime = orderTime;
	}

}
