package com.example.global_meals_gradle.dao;

import com.example.global_meals_gradle.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsDao extends JpaRepository<Products, Integer> {
	/* 根據商品 ID 找商品 */
//	@Query(value = "SELECT * FROM products WHERE id = ?1", nativeQuery = true)
//	public Products findProductById(int productId);
}
