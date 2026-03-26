package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.OrderCartDetails;

@Repository
public interface OrderCartDetailsDao extends JpaRepository<OrderCartDetails, Integer> {

	/* 新增購物車裡的商品細項 */
	@Modifying
	@Transactional
	@Query(value = "insert into order_cart_details (id, order_cart_id, product_id, quantity, price, is_gift, discount_note) values (?1, ?2, ?3, ?4, ?5, ?6, ?7)", nativeQuery = true)
	public void insert(int id, int orderCartId, int productId, int quantity,// 
			BigDecimal price, boolean gift, String discountNote);
	
	/* 刪除購物車 id=? 的所有商品(之後在新增新的商品細項) */
	@Modifying
	@Transactional
	@Query(value = "delete from order_cart_details where id = ?1", nativeQuery = true)
	public void delete(int quizId);
}
