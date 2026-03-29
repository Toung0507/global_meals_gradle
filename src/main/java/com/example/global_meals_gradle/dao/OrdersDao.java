package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;
import java.util.List;

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
	@Query(value = "insert into orders (id,order_date_id,order_cart_id, global_area_id, member_id, subtotal_before_tax, tax_amount, total_amount) values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)", nativeQuery = true)
	public void insert(String id, String orderDateId,int orderCartId, int globalAreaId, int memberId, BigDecimal subtotalBeforeTax,// 
			BigDecimal taxAmount, BigDecimal totalAmount);
	
	/* 查詢該會員的訂單紀錄 */
	@Query(value = "select * from orders where member_id = ?1", nativeQuery = true)
	public List<Orders>  getOrderByMemberId(int memberId);
}
