package com.example.global_meals_gradle.req;

import java.time.LocalDate;

import com.example.global_meals_gradle.constants.ValidationMsg;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

public class ExchangeRatesReq {
	
	@NotNull(message = ValidationMsg.DATE_TIME_ERROR)
	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonAlias("today")
	private LocalDate date;

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
}
