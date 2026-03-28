package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.global_meals_gradle.entity.Products;

@Repository
public interface ProductsDao extends JpaRepository<Products, Integer> {

	@Query(value = "select name from products where id = ?1", nativeQuery = true)
	public String getProductsNameById(int id);
}
