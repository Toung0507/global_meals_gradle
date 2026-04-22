package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Regions;

@Repository
public interface RegionsDao extends JpaRepository<Regions, Integer>{
	
	// 新增/修改稅率
	/* 在使用 nativeQuery = true 時，JPA 有時無法自動將 TaxType (Enum) 轉換為資料庫認識的字串。
	 * 所以這邊需要改成 String taxType */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO regions (country, currency_code, country_code, tax_rate, tax_type, usage_cap, created_at, updated_at) "
			+ " values (:inputCountry, :inputCurrencyCode, :inputCountryCode, :inputTaxRate, :inputTaxType, 0, curdate(), curdate()) "
			+ " ON DUPLICATE KEY UPDATE "
			+ " tax_rate = IFNULL(:inputTaxRate, tax_rate), "
			+ " tax_type = IFNULL(:inputTaxType, tax_type), "
			+ " updated_at = curdate() ", nativeQuery = true)
	public void upsertTax(//
			@Param("inputCountry") String country, //
			@Param("inputCurrencyCode") String currencyCode, //
			@Param("inputCountryCode") String countryCode, //
			@Param("inputTaxRate") BigDecimal taxRate, //
			@Param("inputTaxType") String taxType);
	
//	// 修改
//	// 修改時通常需要手動觸發 updated_at = CURDATE()，否則該欄位會維持在舊的新增日期。
//	@Modifying
//	@Transactional
//	@Query(value = "UPDATE regions SET tax_rate = ?2, tax_type = ?3, updated_at = curdate() WHERE id = ?1", nativeQuery = true)
//	public void update(int id, BigDecimal taxRate, String taxType);
	
	// 查詢各國稅率
	@Query(value = "SELECT * FROM regions", nativeQuery = true)
	public List<Regions> getAll();
	
	// 根據 id 查詢
	@Query(value = "SELECT * FROM regions WHERE id = ?1", nativeQuery = true)
	public Regions getById(int id);
	
	// 修改折扣上限
	@Modifying
	@Transactional
	@Query(value = "UPDATE regions SET usage_cap = ?2, updated_at = CURDATE() WHERE id = ?1", nativeQuery = true)
	public void updateUsageCap(int id, int usageCap);

	// 依國家名稱查出對應的折扣上限（promotions 折扣計算用）
	// 查不到表示前端傳入的 country 不存在於 regions 表，屬於異常
	@Query(value = "SELECT usage_cap FROM regions WHERE country = :country", nativeQuery = true)
	Integer findUsageCapByCountry(@Param("country") String country);



	/* 查找該分店所在國家的稅制與稅率(成立訂單使用) */ 
	@Query(value = "SELECT r.* FROM regions r " + //
            "JOIN global_area g ON r.id = g.regions_id " + //
            "WHERE g.id = ?1", 
    nativeQuery = true)
	public Regions findTaxByAreaId(int areaId);

	
	/*
	 * 根據 global_area 的 id，直接查出對應的 Regions（一條 SQL 走完兩張表）
	 * 原理：global_area.regions_id 是指向 regions.id 的外鍵，直接用子查詢
	 */
	@Query(value = "SELECT r.* FROM regions r "
	        + "JOIN global_area g ON r.id = g.regions_id "
	        + "WHERE g.id = :globalAreaId", 
	        nativeQuery = true)
	Regions findByGlobalAreaId(@Param("globalAreaId") int globalAreaId);
	
}