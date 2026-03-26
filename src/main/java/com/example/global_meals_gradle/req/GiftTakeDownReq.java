package com.example.global_meals_gradle.req;

import java.util.List;

/* 贈品下架 */
public class GiftTakeDownReq {
	
	private List<Integer> id;  // 可能一次下架多個贈品

	public List<Integer> getId() {
		return id;
	}

	public void setId(List<Integer> id) {
		this.id = id;
	}
	
	
}
