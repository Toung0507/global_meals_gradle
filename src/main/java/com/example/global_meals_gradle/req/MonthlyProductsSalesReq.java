package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MonthlyProductsSalesReq {

    // 年份：必填，具體的年份範圍留給 Service 去做動態驗證
    @NotNull(message = ValidationMsg.YEAR_REQUIRED)
    private Integer year;

    // 月份：1 ~ 12 月是永遠不會變的，所以可以直接用 @Min @Max 沒問題
    @NotNull(message = ValidationMsg.MONTH_REQUIRED)
    @Min(value = 1, message = ValidationMsg.MONTH_RANGE)
    @Max(value = 12, message = ValidationMsg.MONTH_RANGE)
    private Integer month;

    // 國家（Region ID）：只有老闆查詢功能B才需要傳入
    // 分店長查詢功能A不需要傳這個欄位（直接從 Session 取 globalAreaId）
    // 不加 @NotNull，因為分店長用不到這個欄位
    private Integer regionId;

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
    public Integer getRegionId() {
        return regionId;
    }
    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }
}
