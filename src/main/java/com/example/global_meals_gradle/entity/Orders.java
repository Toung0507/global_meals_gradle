package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.global_meals_gradle.constants.OrdersStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Orders {
	
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name = "order_cart_date_id")
	private String orderCartDateId;
	
	@Column(name = "order_cart_id")
	private String orderCartId;
	
	@Column(name = "global_area_id")
	private int globalAreaId;
	
	@Column(name = "member_id")
	private int memberId;
	
	@Column(name = "subtotal_befor_tax", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal subtotalBeforeTax;
	
	@Column(name = "tax_amount", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal taxAmount;
	
	@Column(name = "total_amount", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal totalAmount;
	
	@Column(name = "payment_method")
	private String paymentMethod;
	
	@Column(name = "transaction_id")
	private String transactionId;
	
	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
    @Column(name = "status")
	private OrdersStatus status;
	
	@Column(name = "completed_at")
	private LocalDateTime completedAt;

}
