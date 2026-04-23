package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.MonthRangeReportsReq;
import com.example.global_meals_gradle.req.MonthlyReportReq;
import com.example.global_meals_gradle.req.RevenueQueryReq;
import com.example.global_meals_gradle.res.MonthRangeReportsRes;
import com.example.global_meals_gradle.res.MonthlyReportRes;
import com.example.global_meals_gradle.res.RevenueQueryRes;
import com.example.global_meals_gradle.service.MonthlyFinancialReportsService;

import jakarta.servlet.http.HttpSession;

@RestController
public class ReportsController {

	@Autowired
	private MonthlyFinancialReportsService monthlyFinancialReportsService;
	
	// 預設查詢一個月份時，會傳該月與上個月的營業額
	@PostMapping("find_monthly_reports")
	public MonthlyReportRes findMonthlyReports(@RequestBody MonthlyReportReq req, HttpSession session) {
		return monthlyFinancialReportsService.getMonthlyReport(req, session);
	}
	
	// 查詢特定區間的營業額(以月份為單位)
	@PostMapping("find_monthly_reports_by_date_range")
	public MonthRangeReportsRes findMonthlyReportByDateRange(@RequestBody MonthRangeReportsReq req, //
			HttpSession session) {
		return monthlyFinancialReportsService.getMonthlyReportsByDateRange(req, session);
	}
	
	// 取的特定期間的營業額(以日為單位)
	@PostMapping("get_revenue_reports")
	public RevenueQueryRes getRevenueReports(@RequestBody RevenueQueryReq req) {
		return monthlyFinancialReportsService.getRevenueReports(req);
	}
}
