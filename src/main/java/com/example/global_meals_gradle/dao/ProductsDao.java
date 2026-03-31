package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Products;

@Repository
public interface ProductsDao extends JpaRepository<Products, Integer> {

  // 用在雙層迴圈，改成用 JOIN 先註解不使用
	// @Query(value = "select name from products where id = ?1", nativeQuery = true)
	// public String getProductsNameById(int id);
	
	/*  用於訂單成立 */
	@Query(value = "select * from products where id = ?1 for update", nativeQuery = true)
	public Products findByIdForUpdate(int id);
	
	/* 庫存減少 */
	// stock_quantity >= ?2: 防止「沒鎖好」的意外
	@Modifying
	@Transactional
	@Query(value = "update products set stock_quantity = stock_quantity - ?2 where id = ?1 and stock_quantity >= ?2", nativeQuery = true)
	public int upDateStock(int id, int stockQuantity);
}
