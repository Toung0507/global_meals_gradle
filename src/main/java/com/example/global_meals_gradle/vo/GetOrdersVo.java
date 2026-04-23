package com.example.global_meals_gradle.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/* 查詢訂單中的外層 */
public class GetOrdersVo {

	private String id;

	private String orderDateId;

	private int globalAreaId;

	private BigDecimal totalAmount;

	private String status;

	private LocalDateTime completedAt;

	private List<GetOrdersDetailVo> GetOrdersDetailVoList;

	public GetOrdersVo() {
		super();
	}

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

	public int getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
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
