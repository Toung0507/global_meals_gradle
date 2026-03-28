package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.PromotionsGifts;

@Repository
public interface PromotionsGiftsDao extends JpaRepository<PromotionsGifts, Integer> {

	/* 新增贈品細項 */
	@Modifying
	@Transactional
	@Query(value = "insert into promotions_gifts(promotions_id, full_amount, gift_product_id, is_active)"
			+ "values (?1, ?2, ?3, ?4)", nativeQuery = true)
	public void insert(int promotionsId, BigDecimal fullAmount, int giftProductId, boolean active);
	
	/* 贈品狀態改變: 上下架 */
	@Modifying
	@Transactional
	@Query(value = "update promotions_gifts set is_active = NOT is_active where id = ?1 ", nativeQuery = true)
	public void update(int id);
	
	/* 取的所有贈品 */
	@Query(value = "select * from promotions_gifts", nativeQuery = true)
	public int getAll();
}
