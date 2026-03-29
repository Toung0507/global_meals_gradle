package com.example.global_meals_gradle.entity;



import com.example.global_meals_gradle.constants.OperationType;

import jakarta.persistence.*;

@Entity
@Table(name = "order_cart")
public class OrderCart {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //當資料庫的id欄位設為AI時，Entity 也必須告訴 JPA：「這個欄位的值由資料庫自己產生」。如果不加，JPA 就會認為你要手動塞一個 ID 給它，導致新增資料時出錯。
	@Column(name = "id")
	private int id;
	
	@Column(name = "global_area_id")
	private int globalAreaId;
	
	@Column(name = "operation")
	private int operation;
	
	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
    @Column(name = "operation_type")
	private OperationType operationType;
	
//	@Column(name = "order_time")
//	private LocalDateTime orderTime;

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

	

}
