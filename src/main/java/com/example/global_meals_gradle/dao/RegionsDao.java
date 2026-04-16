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
	
	// 新增
	/* 在使用 nativeQuery = true 時，JPA 有時無法自動將 TaxType (Enum) 轉換為資料庫認識的字串。
	 * 所以這邊需要改成 String taxType */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO regions (country, currency_code, tax_rate, tax_type, created_at, updated_at) "
			+ " values (?1, ?2, ?3, ?4, curdate(), curdate())", nativeQuery = true)
	public void insert(String country, String currencyCode, BigDecimal taxRate, String taxType);
	
	// 修改
	// 修改時通常需要手動觸發 updated_at = CURDATE()，否則該欄位會維持在舊的新增日期。
	@Modifying
	@Transactional
	@Query(value = "UPDATE regions SET tax_rate = ?2, tax_type = ?3, updated_at = curdate() WHERE id = ?1", nativeQuery = true)
	public void update(int id, BigDecimal taxRate, String taxType);
	
	// 查詢各國稅率
	@Query(value = "SELECT * FROM regions", nativeQuery = true)
	public List<Regions> getAll();

	// 依國家名稱查出對應的折扣上限（promotions 折扣計算用）
	// 查不到表示前端傳入的 country 不存在於 regions 表，屬於異常
	@Query(value = "SELECT usage_cap FROM regions WHERE country = :country", nativeQuery = true)
	Integer findUsageCapByCountry(@Param("country") String country);



	/* 查找該分店所在國家的稅制與稅率(成立訂單使用) */ 
	@Query("SELECT r FROM Regions r JOIN GlobalArea g ON r.country = g.country WHERE g.id = ?1")
	public Regions findTaxByAreaId(int areaId);
}
