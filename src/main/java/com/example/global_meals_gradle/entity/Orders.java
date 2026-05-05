package com.example.global_meals_gradle.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.global_meals_gradle.constants.OrdersStatus;
import com.example.global_meals_gradle.constants.PayStatus;

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

	@Column(name = "phone")
	private String phone;

	@Column(name = "subtotal_before_tax", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal subtotalBeforeTax;

	@Column(name = "tax_amount", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal taxAmount;

	@Column(name = "total_amount", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal totalAmount;

	@Column(name = "total_cost", precision = 12, scale = 2) // DECIMAL(12,2)
	private BigDecimal totalCost;

	@Column(name = "payment_method")
	private String paymentMethod;

	@Column(name = "transaction_id")
	private String transactionId;

	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
	@Column(name = "orders_status")
	private OrdersStatus ordersStatus;

	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
	@Column(name = "pay_status")
	private PayStatus payStatus;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "is_use_discount")
	private boolean useDiscount;

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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
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

	public OrdersStatus getOrdersStatus() {
		return ordersStatus;
	}

	public void setOrdersStatus(OrdersStatus ordersStatus) {
		this.ordersStatus = ordersStatus;
	}

	public PayStatus getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(PayStatus payStatus) {
		this.payStatus = payStatus;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public boolean isUseDiscount() {
		return useDiscount;
	}

	public void setUseDiscount(boolean useDiscount) {
		this.useDiscount = useDiscount;
	}
}
