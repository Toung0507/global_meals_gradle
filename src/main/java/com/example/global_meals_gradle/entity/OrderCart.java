package com.example.global_meals_gradle.entity;

import com.example.global_meals_gradle.constants.OperationType;

import jakarta.persistence.*;

@Entity
@Table(name = "order_cart")
public class OrderCart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id; // 必須用 Integer（可 null），Spring Data isNew() 才能正確判斷新實體

	@Column(name = "global_area_id")
	private int globalAreaId;

	@Column(name = "operation")
	private int operation;

	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
	@Column(name = "operation_type")
	private OperationType operationType;

	public OrderCart() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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
}
