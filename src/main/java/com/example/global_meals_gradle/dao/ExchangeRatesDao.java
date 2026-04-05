package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
	@Query(value = "INSERT INTO exchange_rates (currency_code, rate_to_twd, updated_at) "
			+ " values (?1, ?2, now()) "
			+ "ON DUPLICATE KEY UPDATE rate_to_twd = ?2, updated_at = now()", nativeQuery = true)
	public void upsertRate(String code, BigDecimal rate);
	
	// 查詢匯率
	@Query(value = "SELECT * FROM exchange_rates", nativeQuery = true)
	public List<ExchangeRates> getAll();
	
	// 查詢匯率(依據日期)
	// 使用 DATE(updated_at) 取得日期部分
	@Query(value = "SELECT * FROM exchange_rates WHERE DATE(updated_at) = ?1 ORDER BY id", nativeQuery = true)
	public List<ExchangeRates> getRatesByDate(LocalDate date);

}
