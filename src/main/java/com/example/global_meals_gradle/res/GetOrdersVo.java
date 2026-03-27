package com.example.global_meals_gradle.res;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.global_meals_gradle.constants.OrdersStatus;

/* 查詢訂單中的外層 */
public class GetOrdersVo {

	private String id;    
	
	private String orderDateId;
	
	private int orderCartId;
	
	private int globalAreaId;
	
	private BigDecimal subtotalBeforeTax;
	
	private BigDecimal taxAmount;
	
	private BigDecimal totalAmount;
	
	private String paymentMethod;
	
	private String transactionId;
	
	private OrdersStatus status;
	
	private LocalDateTime completedAt;
	
	private List<GetOrdersDetailVo> GetOrdersDetailVoList;

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

	public List<GetOrdersDetailVo> getGetOrdersDetailVoList() {
		return GetOrdersDetailVoList;
	}

	public void setGetOrdersDetailVoList(List<GetOrdersDetailVo> getOrdersDetailVoList) {
		GetOrdersDetailVoList = getOrdersDetailVoList;
	}
	
}
