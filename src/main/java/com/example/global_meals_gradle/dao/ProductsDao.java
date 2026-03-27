package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.global_meals_gradle.entity.OrderCartDetails;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.res.GetOrdersDetailVo;

public interface ProductsDao extends JpaRepository<Products, Integer> {

	@Query(value = "select name from products where id = ?1", nativeQuery = true)
	public String getProductsNameById(int id);
}
