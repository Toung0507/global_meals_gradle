package com.example.global_meals_gradle.entity;

import java.time.LocalDateTime;

import com.example.global_meals_gradle.constants.OperationType;

import jakarta.persistence.*;

@Entity
@Table(name = "order_cart")
@IdClass(value = OrderCartId.class)
public class OrderCart {
	
	@Id
	@Column(name = "date_id")
	private String dateId;
	
	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "global_area_id")
	private int globalAreaId;
	
	@Column(name = "operation")
	private int operation;
	
	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
    @Column(name = "operation_type")
	private OperationType operationType;
	
	@Column(name = "order_time")
	private LocalDateTime orderTime;

	public String getDateId() {
		return dateId;
	}

	public void setDateId(String dateId) {
		this.dateId = dateId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
