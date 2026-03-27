package com.example.global_meals_gradle.dao;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Promotions;


public interface PromotionsDao extends JpaRepository<Promotions, Integer> {

	/* 新增活動方案 */
	@Modifying
	@Transactional
	@Query(value = "insert into promotions(name, start_time, end_time, max_exchange)"
			+ "values (?1, ?2, ?3, ?4)", nativeQuery = true)
	public void insert(String name, LocalDate startTime, LocalDate endTime, int maxExchange);
	
	/* 取得所有活動 */
	@Query(value = "select * from promotions", nativeQuery = true)
	public int getAll();
}
