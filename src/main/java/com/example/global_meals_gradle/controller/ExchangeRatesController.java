package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.ExchangeRatesReq;
import com.example.global_meals_gradle.res.ExchangeRatesRes;
import com.example.global_meals_gradle.service.ExchangeRatesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/exchange-rates") 
@Tag(name = "匯率管理模組", description = "提供匯率歷史紀錄查詢與指定日期匯率查詢")
public class ExchangeRatesController {
	
	@Autowired
	private ExchangeRatesService exchangeRatesService;
	
	// 取得全部匯率歷史紀錄清單
	@GetMapping("get_all_rates")
	@Operation(summary = "取得全部匯率", description = "獲取系統中所有匯率的歷史紀錄清單")
	public ExchangeRatesRes getAllRates() {
		return exchangeRatesService.getAllRates();
	}
	
	// 取得匯率清單(依據日期)
	@PostMapping("get_rates_by_date")
	@Operation(summary = "依日期查詢匯率", description = "根據傳入的日期參數查詢當日匯率清單")
	public ExchangeRatesRes getAllByDate(@Valid @RequestBody ExchangeRatesReq req) {
		return exchangeRatesService.getAllByDate(req);
	}

}
