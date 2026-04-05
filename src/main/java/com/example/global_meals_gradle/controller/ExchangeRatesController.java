package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.ExchangeRatesReq;
import com.example.global_meals_gradle.res.ExchangeRatesRes;
import com.example.global_meals_gradle.service.ExchangeRatesService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true") // 允許 Angular 跨域呼叫
public class ExchangeRatesController {
	
	@Autowired
	private ExchangeRatesService exchangeRatesService;
	
	// 取得全部匯率歷史紀錄清單
	@GetMapping("exchange_rates/get_all_rates")
	public ExchangeRatesRes getAllRates() {
		return exchangeRatesService.getAllRates();
	}
	
	// 取得匯率清單(依據日期)
	@PostMapping("exchange_rates/get_rates_by_date")
	public ExchangeRatesRes getAllByDate(@Valid @RequestBody ExchangeRatesReq req) {
		return exchangeRatesService.getAllByDate(req);
	}

}
