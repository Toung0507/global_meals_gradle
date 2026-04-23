package com.example.global_meals_gradle.req;

import java.math.BigDecimal;
import java.util.List;

import com.example.global_meals_gradle.constants.ValidationMsg;
import com.example.global_meals_gradle.entity.OrderCartDetails;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/* 成立訂單(還沒付款) */
public class CreateOrdersReq {

	@NotBlank(message = ValidationMsg.ORDER_CART_ID_ERROR)
	private String orderCartId;

	private int globalAreaId;

	private int memberId;

	@NotBlank(message = ValidationMsg.PHONE_ERROR)
	private String phone;

	private BigDecimal subtotalBeforeTax;

	private BigDecimal taxAmount;

	private BigDecimal totalAmount;
	
	@NotEmpty(message = ValidationMsg.ORDER_CART_DETAILS_NOT_EMPTY)
	@Valid // 這樣才會去檢查 OrderCartDetails 類別裡面的標註
	private List<OrderCartDetails> OrderCartDetailsList;
	
	private boolean useDiscount;   // 判斷有無使用優惠劵

	private int promotionsId;  // 判斷參加的優惠活動id

	// 客戶端現金下單時傳 "CASH"，後端會建立 PENDING_CASH 訂單（不立即付款）
	private String paymentMethod;

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

	public List<OrderCartDetails> getOrderCartDetailsList() {
		return OrderCartDetailsList;
	}

	public void setOrderCartDetailsList(List<OrderCartDetails> orderCartDetailsList) {
		OrderCartDetailsList = orderCartDetailsList;
	}

	public boolean isUseDiscount() {
		return useDiscount;
	}

	public void setUseDiscount(boolean useDiscount) {
		this.useDiscount = useDiscount;
	}

	public int getPromotionsId() {
		return promotionsId;
	}

	public void setPromotionsId(int promotionsId) {
		this.promotionsId = promotionsId;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}
