package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.global_meals_gradle.entity.OrderCart;

@Repository
public interface OrderCartDao extends JpaRepository<OrderCart, Integer> {
	// 根據購物車 ID 查詢購物車主表
	// 用途：在 getCartView() 中拿到 globalAreaId，後續用來查稅率
	@Query(value = "SELECT * FROM order_cart WHERE id = ?1", nativeQuery = true)
	OrderCart findById(int id);
}
