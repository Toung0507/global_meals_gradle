package com.example.global_meals_gradle.res;

import java.util.List;


public class GetAllOrdersRes extends BasicRes {

	private List<GetOrdersVo> getOrderVoList;
	
	public GetAllOrdersRes() {
		super();
	}


	public GetAllOrdersRes(int code, String message, List<GetOrdersVo> getOrderVoList) {
		super(code, message);
		this.getOrderVoList = getOrderVoList;
	}

	public List<GetOrdersVo> getGetOrderVoList() {
		return getOrderVoList;
	}

	public void setGetOrderVoList(List<GetOrdersVo> getOrderVoList) {
		this.getOrderVoList = getOrderVoList;
	}
	
}
