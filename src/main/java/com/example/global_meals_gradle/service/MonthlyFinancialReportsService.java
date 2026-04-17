package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.GlobalAreaDao;
import com.example.global_meals_gradle.dao.MonthlyFinancialReportsDao;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.entity.GlobalArea;
import com.example.global_meals_gradle.entity.MonthlyFinancialReports;
import com.example.global_meals_gradle.req.RevenueQueryReq;
import com.example.global_meals_gradle.res.RevenueData;
import com.example.global_meals_gradle.res.RevenueQueryRes;

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
		} else {
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

	// 取的特定期間的營業額
	public RevenueQueryRes getRevenueReport(RevenueQueryReq req) {

		// 1. 處理日期邊界 (避免漏掉最後一天的訂單)
		// LocalDate.parse: 把字串變為 LocalDate
		// atStartOfDay(): 該日的 00:00:00.000 / atTime(LocalTime.MAX): 該日的 23:59:59.999
		LocalDateTime beginTime = LocalDate.parse(req.getStartDate()).atStartOfDay();
		LocalDateTime endTime = LocalDate.parse(req.getEndDate()).atTime(LocalTime.MAX);
		log.info("【營業額查詢】時間區間: {} 到 {}", beginTime, endTime);

		// 宣告一個清單來裝結果
		List<Object[]> rawData;
		// 2. 根據 ID 是否為 null (或 0) 來決定查詢維度
		if (req.getBranchId() != null && req.getBranchId() != 0) {
			// A. 查單店：回傳該店名稱與金額
			rawData = ordersDao.findSingleBranchRevenue(req.getBranchId(), beginTime, endTime);
		}else if (req.getRegionsId() != null && req.getRegionsId() != 0) {
			// B. 查國家：回傳該國家下所有分店的列表
			rawData = ordersDao.findRevenueByRegionGroupedByBranch(req.getRegionsId(), beginTime, endTime);
		}else {
			// B. 查全球：回傳所有分店的列表
			rawData = ordersDao.findRevenue(beginTime, endTime);
		}
		// 判斷是否為空的 List
		if(rawData == null || rawData.isEmpty()) {
			throw new RuntimeException("查無資料");
		}
		// rawData.stream(): 把這個清單(rawData) 變成一個「流（Stream）」，準備一個一個加工
		// .map(result -> { ... }): 舊的東西轉成新的東西
		// resule: 這是一個 Object[] 陣列，裡面裝著 [店名, 國家名, 金額]
		List<RevenueData> dataList = rawData.stream().map(result -> {
			RevenueData data = new RevenueData();
			data.setBranchName((String) result[0]);
			data.setRegionsName((String) result[1]);
			data.setTotalAmount((BigDecimal) result[2]);
			return data;
		}).collect(Collectors.toList());
		return new RevenueQueryRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), dataList);

	}
}
