package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.entity.OrdersId;

public interface OrdersDao extends JpaRepository<Orders, OrdersId> {

	/* 新增訂單 */
	@Modifying
	@Transactional
	@Query(value = "insert into orders (order_cart_id, global_area_id, member_id, subtotal_befor_tax, tax_amount, total_amount, payment_method, transaction_id) values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)", nativeQuery = true)
	public void insert(String orderCartId, int globalAreaId, int memberId, BigDecimal subtotalBeforeTax,// 
			BigDecimal taxAmount, BigDecimal totalAmount, String paymentMethod, String transactionId);
	
	/* 查詢該會員的訂單紀錄 */
	@Query(value = "select * from orders where member_id = ?1", nativeQuery = true)
	public void getOrderByMemberId(int memberId);
}
