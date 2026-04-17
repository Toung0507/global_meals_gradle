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

	// 建立日誌物件
	private static final org.slf4j.Logger log = org.slf4j //
			.LoggerFactory.getLogger(MonthlyFinancialReportsService.class);

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private GlobalAreaDao globalAreaDao;

	@Autowired
	private MonthlyFinancialReportsDao monthlyFinancialReportsDao;

	@Transactional(rollbackFor = Exception.class)
	public void monthlyRevenue() {
		// 1. 自動計算上個月的年份與月份 (格式: 2026-03)
		// YearMonth.now(): 當前的年份與月份 .minusMonths(1): 自動把月份往回推 1 個月
		YearMonth lastMonth = YearMonth.now().minusMonths(1);
		// 定義日期格式並轉為字串
		String reportDate = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

		log.info("開始產出 {} 份各店月報表...", reportDate);

		// 2. 取得所有分店資訊
		List<GlobalArea> allAreas = globalAreaDao.findAll();
		log.debug("【財報排程】共取得 {} 個分店資訊進行統計", allAreas.size());

		List<MonthlyFinancialReports> reportList = new ArrayList<>();

		for (GlobalArea area : allAreas) {
			try {
				// 3. 從訂單表加總該分店、該月份、狀態為「已完成」的訂單
				MonthlyFinancialReports monthReports = //
						generateIndividualBranchReport(area.getId(), //
								area.getRegionsId(), reportDate, lastMonth);
				reportList.add(monthReports);
			} catch (Exception e) {
				log.error("分店 {} 報表產出失敗: {}", area.getBranch(), e.getMessage());
			}
		}
		if (!reportList.isEmpty()) {
			monthlyFinancialReportsDao.saveAll(reportList);
			// [INFO] 記錄最終產出結果
            log.info("【財報完成】{} 月份報表已產出，成功儲存 {} 筆數據", reportDate, reportList.size());
		}else {
			// [WARN] 如果一筆都沒存，這很不正常，需要警告
            log.warn("【財報警告】{} 月份未產出任何報表數據，請確認訂單是否有完成紀錄", reportDate);
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
