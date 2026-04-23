package com.example.global_meals_gradle.req;

/* 查詢會員訂單歷史紀錄: 用會員 Id */
public class HistoricalOrdersReq {

	private Integer memberId;

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}
}
