package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.OrdersStatus;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.entity.OrdersId;
import com.example.global_meals_gradle.res.GetOrdersVo;


@Repository
public interface OrdersDao extends JpaRepository<Orders, OrdersId> {

	/* 新增訂單 */
	@Modifying
	@Transactional
	@Query(value = "insert into orders (id, order_date_id, order_cart_id, global_area_id, member_id, phone, subtotal_before_tax, tax_amount, total_amount) values (?1, ?2, ?3, ?4, ?5, ?6, ?7)", nativeQuery = true)
	public void insert(String id, String orderDateId, String orderCartId, int globalAreaId, int memberId, String phone,//
			BigDecimal subtotalBeforeTax, //
			BigDecimal taxAmount, BigDecimal totalAmount);

	/* 根據 orderDateId 查詢特定訂單 */
	// ORDER BY id (排序)(字串排序需長度樣(補零)) DESC (倒序) LIMIT 1 (限制筆數) FOR UPDATE:
	// 查詢到的這筆資料會被鎖住
	// Optional: 如果當天還沒有人下單（第一筆），它會回傳 Optional.empty()，你的 Service 就可以判斷 isPresent()
	// 來給出第一個號碼 0001。
	@Query(value = "select * from orders where order_date_id = ?1 order by id desc limit 1 for update", nativeQuery = true)
	public Optional<Orders> getOrderByOrderDateId(String orderDateId);
	
	/* 根據電話號碼查詢最新的一筆訂單 */
	@Query(value = "select * from orders where order_date_id = ?1 and phone = ?2 order by id desc limit 1", nativeQuery = true)
	public GetOrdersVo getOrderByPhone(String orderDateId, String phone);
	
	/* 依據訂單編號查詢該筆訂單 */
	@Query(value = "select * from orders where order_date_id = ?1 and id = ?2", nativeQuery = true)
	public Orders getOrderByOrderDateIdAndId(String orderDateId, String id);

	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態) */
	@Modifying
	@Transactional
	@Query(value = "update orders set payment_method = ?3, transaction_id = ?4, status = ?5 where id = ?1 and order_date_id = ?2", nativeQuery = true)
	public void updatePay(String id, String orderDateId, String paymentMethod, String transactionId, OrdersStatus status);

	/* 查詢該會員的訂單紀錄 */
	@Query(value = "select * from orders where member_id = ?1", nativeQuery = true)
	public List<GetOrdersVo> getOrderByMemberId(int memberId);

	/* 查詢該會員的訂單紀錄 */
	@Query(value = "SELECT o.id, o.order_date_id, o.global_area_id, o.total_amount, o.status, o.completed_at,"
			+ "d.quantity, d.price, d.is_gift, d.discount_note, " + "p.name as product_name " + "FROM orders o "
			+ "LEFT JOIN order_cart_details d ON o.order_cart_id = d.order_cart_id "
			+ "LEFT JOIN products p ON d.product_id = p.id " + "WHERE o.member_id = ?1 "
			+ "ORDER BY o.order_date_id DESC, o.id DESC", nativeQuery = true)
	public List<Object[]> getFullOrderHistory(int memberId);

	/* 訂單狀態更新 */
	@Modifying
	@Transactional
	@Query(value = "update orders SET status = :status where id = :id and date_id = :orderDateId", nativeQuery = true)
	public int updateOrderStatus(@Param("status") OrdersStatus status, // AI 是說要字串型態，我有說資料庫是設ENUM
			@Param("id") String id, @Param("order_date_id") String orderDateId);
}
