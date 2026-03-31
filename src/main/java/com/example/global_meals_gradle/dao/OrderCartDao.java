package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.OperationType;
import com.example.global_meals_gradle.entity.OrderCart;

@Repository
public interface OrderCartDao extends JpaRepository<OrderCart, Integer> {

	/* 建立購物車 */
	//	@Modifying
	//	@Transactional
	//	@Query(value = "insert into order_cart(global_area_id, operation, operation_type)"
	//			+ "values (?1, ?2, ?3)", nativeQuery = true)
	//	public void insert(int globalAreaId, int operation, OperationType operationType);
	//	

}
