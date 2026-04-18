package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MonthlyProductsSalesReq {
	// 年份：必填，具體的年份範圍留給 Service 去做動態驗證
    @NotNull(message = "year 不可為空")
    private Integer year;
    // 月份：1 ~ 12 月是永遠不會變的，所以可以直接用 @Min @Max 沒問題
    @NotNull(message = "month 不可為空")
    @Min(value = 1, message = "月份最小為 1")
    @Max(value = 12, message = "月份最大為 12")
    private Integer month;
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getMonth() {
		return month;
	}
	public void setMonth(Integer month) {
		this.month = month;
	}
    
}