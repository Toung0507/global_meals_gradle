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
	@Modifying
	@Transactional
	@Query(value = "insert into order_cart(global_area_id, operation, operation_type)"
			+ "values (?1, ?2, ?3)", nativeQuery = true)
	public void insert(int globalAreaId, int operation, OperationType operationType);
	
	/* 更新購物車 */
	@Modifying
	@Transactional
	@Query(value = "update order_cart set global_area_id = ?2, operation = ?3, operation_type = ?4", nativeQuery = true)
	public void update(int id, int globalAreaId, int operation, OperationType operationType);
	
	/* 取得該會員或是員工最新的購物車id(依據 operation)*/
	@Query(value = "select max(id) from order_cart where operation = ?1", nativeQuery = true)
	public int getMaxId(int operation);
}
