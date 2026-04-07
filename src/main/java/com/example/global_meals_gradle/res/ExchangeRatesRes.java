package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.entity.ExchangeRates;

public class ExchangeRatesRes extends BasicRes {

	private List<ExchangeRates> exchangeRatesList;

	public ExchangeRatesRes() {
		super();
	}

	public ExchangeRatesRes(int code, String message) {
		super(code, message);
	}

	public ExchangeRatesRes(int code, String message, List<ExchangeRates> exchangeRatesList) {
		super(code, message);
		this.exchangeRatesList = exchangeRatesList;
	}

	public List<ExchangeRates> getExchangeRatesList() {
		return exchangeRatesList;
	}

	public void setExchangeRatesList(List<ExchangeRates> exchangeRatesList) {
		this.exchangeRatesList = exchangeRatesList;
	}

}
