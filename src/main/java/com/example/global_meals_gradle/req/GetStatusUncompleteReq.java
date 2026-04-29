package com.example.global_meals_gradle.req;

import java.util.List;

import com.example.global_meals_gradle.vo.OrdersIdVo;

import jakarta.validation.Valid;

public class GetStatusUncompleteReq {

	@Valid
	private List<OrdersIdVo> ordersIdList;

	public List<OrdersIdVo> getOrdersIdList() {
		return ordersIdList;
	}

	public void setOrdersIdList(List<OrdersIdVo> ordersIdList) {
		this.ordersIdList = ordersIdList;
	}
	
}
