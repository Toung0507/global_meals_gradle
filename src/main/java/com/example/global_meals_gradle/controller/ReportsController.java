package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.MonthRangeReportsReq;
import com.example.global_meals_gradle.req.MonthlyReportReq;
import com.example.global_meals_gradle.req.RevenueQueryReq;
import com.example.global_meals_gradle.res.MonthRangeReportsRes;
import com.example.global_meals_gradle.res.MonthlyReportRes;
import com.example.global_meals_gradle.res.RevenueQueryRes;
import com.example.global_meals_gradle.service.MonthlyFinancialReportsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/reports")
@Tag(name = "報表管理模組", description = "處理財務報表、營業額查詢等分析數據")
public class ReportsController {

	@Autowired
	private MonthlyFinancialReportsService monthlyFinancialReportsService;
	
	// 預設查詢一個月份時，會傳該月與上個月的營業額
	@PostMapping("find_monthly_reports")
	@Operation(summary = "查詢月度報表", description = "查詢指定月份的營業額，通常包含該月與上個月的對比數據")
	public MonthlyReportRes findMonthlyReports(@RequestBody MonthlyReportReq req, //
			@Parameter(hidden = true) HttpSession session) {
		return monthlyFinancialReportsService.getMonthlyReport(req, session);
	}
	
	// 查詢特定區間的營業額(以月份為單位)
	@PostMapping("find_monthly_reports_by_date_range")
	@Operation(summary = "查詢月份區間報表", description = "查詢特定月份區間的營業額分析")
	public MonthRangeReportsRes findMonthlyReportByDateRange(@RequestBody MonthRangeReportsReq req, //
			@Parameter(hidden = true) HttpSession session) {
		return monthlyFinancialReportsService.getMonthlyReportsByDateRange(req, session);
	}
	
	// 取的特定期間的營業額(以日為單位)
	@PostMapping("get_revenue_reports")
	@Operation(summary = "查詢每日營業額", description = "取得特定期間內，以「天」為單位的營業額明細")
	public RevenueQueryRes getRevenueReports(@RequestBody RevenueQueryReq req, //
			@Parameter(hidden = true) HttpSession session) {
		return monthlyFinancialReportsService.getRevenueReports(req, session);
	}
}
