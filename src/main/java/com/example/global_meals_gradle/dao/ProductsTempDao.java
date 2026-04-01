package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.global_meals_gradle.entity.Products;

public interface ProductsTempDao extends JpaRepository<Products, Integer> {

	/* 僅抓取商品名稱 理由：避免抓取 MEDIUMBLOB (圖片) 造成效能問題，且即使其他欄位修改也不受影響 */
	@Query(value = "SELECT name FROM products WHERE id = :id", nativeQuery = true)
	String findNameById(@Param("id") int id);
}