package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.dao.GlobalAreaDao;
import com.example.global_meals_gradle.dao.MonthlyFinancialReportsDao;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.entity.GlobalArea;
import com.example.global_meals_gradle.entity.MonthlyFinancialReports;

@Service
public class MonthlyFinancialReportsService {

	@Autowired
    private OrdersDao ordersDao;
	
	@Autowired
	private GlobalAreaDao globalAreaDao;
	
	@Autowired
	private MonthlyFinancialReportsDao monthlyFinancialReportsDao;
	
	@Transactional(rollbackFor = Exception.class)
	public void monthlyRevenue() {
		// 1. 自動計算上個月的年份與月份 (格式: 2026-03)
		// YearMonth.now(): 當前的年份與月份   .minusMonths(1): 自動把月份往回推 1 個月
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        // 定義日期格式並轉為字串
        String reportDate = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // log.info("開始產出 {} 份各店月報表...", reportDate);

        // 2. 取得所有分店資訊
        List<GlobalArea> allAreas = globalAreaDao.findAll();
        
        List<MonthlyFinancialReports> reportList = new ArrayList<>();

        for (GlobalArea area : allAreas) {
            try {
                // 3. 從訂單表加總該分店、該月份、狀態為「已完成」的訂單
                // 這裡建議在 OrderRepository 寫一個專門的查詢方法
            	MonthlyFinancialReports monthReports = //
            			generateIndividualBranchReport(area.getId(),//
            					area.getRegionsId(), reportDate, lastMonth);
            	reportList.add(monthReports);
            } catch (Exception e) {
                // log.error("分店 {} 報表產出失敗: {}", branchId, e.getMessage());
            }
        }
        if (!reportList.isEmpty()) {
            monthlyFinancialReportsDao.saveAll(reportList);
        }
    }
	
	// 從訂單表加總該分店、該月份、狀態為「已完成」的訂單
	private MonthlyFinancialReports generateIndividualBranchReport(//
			int branchId, int regionsId, String reportDate, YearMonth lastMonth) {
		// 設定該月的啟始與結束時間 (例如 2026-03-01 00:00:00 到 2026-03-31 23:59:59)
        LocalDateTime start = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime end = lastMonth.atEndOfMonth().atTime(23, 59, 59);
        // 取的該店該月的總營業額
		BigDecimal monthlyTotalAmount = ordersDao.findTotalAmountByGlobalAreaId(branchId, start, end);
		MonthlyFinancialReports monthReports = new MonthlyFinancialReports();
		monthReports.setReportDate(reportDate);
		monthReports.setBranchId(branchId);
		monthReports.setRegionsId(regionsId);
		monthReports.setTotalAmount(monthlyTotalAmount != null ? monthlyTotalAmount : BigDecimal.ZERO);
		return monthReports;
	}
}
