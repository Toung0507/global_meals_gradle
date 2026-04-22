package com.example.global_meals_gradle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.global_meals_gradle.service.MonthlyFinancialReportsService;

@EnableScheduling
@Component
public class MonthlyReportsScheduler {

	@Autowired
	private MonthlyFinancialReportsService monthlyFinancialReportsService;
	
	/* 星號由左至右為-->秒 分 小時 日 月 週(星期幾)，並且星號要有空格 */
	@Scheduled(cron = "49 23 2 1 * ?") // 每月1號凌晨2點23分49秒執行
	public void MonthlyReport() {
		monthlyFinancialReportsService.monthlyRevenue();
	}
}
