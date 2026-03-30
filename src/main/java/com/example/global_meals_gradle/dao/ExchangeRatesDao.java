package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.global_meals_gradle.entity.ExchangeRates;

@Repository
public interface ExchangeRatesDao extends JpaRepository<ExchangeRates, Integer>{
	
	// 新增/更新匯率
	@Modifying
	@Transactional
	@Query(value = "inser into exchange_rates (currency_code, rate_to_twd) "
			+ " values (?1, ?2) "
			+ "on duplicate key update rate_to_twd = ?2", nativeQuery = true)
	public void upserRate(String code, BigDecimal rate);

}
