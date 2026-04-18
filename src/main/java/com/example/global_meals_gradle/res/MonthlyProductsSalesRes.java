package com.example.global_meals_gradle.res;

import java.util.List;

// 回傳給前端的最外層包裝，固定格式：code + message + 資料
	public class MonthlyProductsSalesRes  extends BasicRes {
	    private List<MonthlyProductsSalesVo> salesList; // 裡面裝了這個月所有商品的銷售清單
	    // 查詢失敗時用（只傳 code 跟 message）
	    public MonthlyProductsSalesRes (int code, String message) {
	        super(code, message);
	    }
	    // 查詢成功時用（同時傳資料）
	    public MonthlyProductsSalesRes (int code, String message, List<MonthlyProductsSalesVo> salesList) {
	        super(code, message);
	        this.salesList = salesList;
	    }
	    public List<MonthlyProductsSalesVo> getSalesList() {
	        return salesList;
	    }
	    public void setSalesList(List<MonthlyProductsSalesVo> salesList) {
	        this.salesList = salesList;
	    }
}
