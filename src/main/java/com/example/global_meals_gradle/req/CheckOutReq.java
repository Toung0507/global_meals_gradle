package com.example.global_meals_gradle.req;

import java.math.BigDecimal;
import java.util.List;

import com.example.global_meals_gradle.entity.OrderCartDetails;

/* 結帳 */
public class CheckOutReq {
	
	private String orderCartId;
	
	private int globalAreaId;
	
	private int memberId;
	
	private BigDecimal subtotalBeforeTax;
	
	private BigDecimal taxAmount;
	
	private BigDecimal totalAmount;
	
	private String paymentMethod;
	
	private String transactionId;
	
	private List<OrderCartDetails> OrderCartDetailsList;

	public String getOrderCartId() {
		return orderCartId;
	}

	public void setOrderCartId(String orderCartId) {
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

	public List<OrderCartDetails> getOrderCartDetailsList() {
		return OrderCartDetailsList;
	}

	public void setOrderCartDetailsList(List<OrderCartDetails> orderCartDetailsList) {
		OrderCartDetailsList = orderCartDetailsList;
	}
	
}
