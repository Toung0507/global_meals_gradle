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
	@Query(value = "insert into order_cart_details (order_cart_id, product_id, quantity, price, is_gift, discount_note) values (?1, ?2, ?3, ?4, ?5, ?6)", nativeQuery = true)
	public void insert(int orderCartId, int productId, int quantity,// 
			BigDecimal price, boolean gift, String discountNote);
	
//	查詢 ？號車裡面有沒有 ？ 號商品，有就加數量，沒有就新建購物車
//	public void findByCartIdAndProductId
//	把 ？號車裡『所有的商品』都拿出來給我算小計！不包含贈品
//	public void findAllByCartId
//	把 7 號車裡標記為『贈品(is_gift=true)』的東西全刪了,因為加點的情況下，滿 500 送紅茶，滿 1000 送酸辣湯，不把舊的贈品刪掉重新算滿額贈，就會兩個贈品都送
	//deleteAllGiftsByCartId
	
	/* 刪除購物車 id=? 的所有商品(之後在新增新的商品細項) */
	@Modifying
	@Transactional
	@Query(value = "delete from order_cart_details where id = ?1", nativeQuery = true)
	public void delete(int quizId);
}
