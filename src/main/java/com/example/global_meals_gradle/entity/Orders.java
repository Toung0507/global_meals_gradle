package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.global_meals_gradle.constants.OrdersStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
@IdClass(value = OrdersId.class)
public class Orders {
	
	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "order_date_id")
	private String orderDateId;
	
	@Column(name = "order_cart_id")
	private int orderCartId;
	
	@Column(name = "global_area_id")
	private int globalAreaId;
	
	@Column(name = "member_id")
	private int memberId;
	
	@Column(name = "subtotal_before_tax", precision = 12, scale = 2) // DECIMAL(12,2)
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
	
	@Column(name = "phone")
	private String phone ;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderDateId() {
		return orderDateId;
	}

	public void setOrderDateId(String orderDateId) {
		this.orderDateId = orderDateId;
	}

	public int getOrderCartId() {
		return orderCartId;
	}

	public void setOrderCartId(int orderCartId) {
		this.orderCartId = orderCartId;
	}

	public int getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public BigDecimal getSubtotalBeforeTax() {
		return subtotalBeforeTax;
	}

	public void setSubtotalBeforeTax(BigDecimal subtotalBeforeTax) {
		this.subtotalBeforeTax = subtotalBeforeTax;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public OrdersStatus getStatus() {
		return status;
	}

	public void setStatus(OrdersStatus status) {
		this.status = status;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

}
