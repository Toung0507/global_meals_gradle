package com.example.global_meals_gradle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.global_meals_gradle.service.ExchangeRatesService;

@EnableScheduling
@Component
public class ExchangeRatesScheduler {

	@Autowired
	private ExchangeRatesService exchangeRatesService;
	
	/* 星號由左至右為-->秒 分 小時 日 月 週(星期幾)，並且星號要有空格 */
	@Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4點刷新
	public void refreshDailyRates() {
		exchangeRatesService.saveRates();
	}
	
}
