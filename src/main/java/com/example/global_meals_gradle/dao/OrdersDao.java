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

import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.entity.OrdersId;
import com.example.global_meals_gradle.res.GetOrdersVo;

@Repository
public interface OrdersDao extends JpaRepository<Orders, OrdersId> {

	/* 新增訂單 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO orders (id, order_date_id, order_cart_id, global_area_id, member_id, phone, "
			+ " subtotal_before_tax, tax_amount, total_amount, status, is_use_discount) "
			+ "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)", nativeQuery = true)
	public void insert(String id, String orderDateId, String orderCartId, int globalAreaId, int memberId, String phone, //
			BigDecimal subtotalBeforeTax, //
			BigDecimal taxAmount, BigDecimal totalAmount, String status, boolean useDiscount);

	/* 根據 orderDateId 查詢特定訂單 */
	// ORDER BY id (排序)(字串排序需長度樣(補零)) DESC (倒序) LIMIT 1 (限制筆數) FOR UPDATE:
	// 查詢到的這筆資料會被鎖住
	// Optional: 如果當天還沒有人下單（第一筆），它會回傳 Optional.empty()，你的 Service 就可以判斷 isPresent()
	// 來給出第一個號碼 0001。
	@Query(value = "SELECT * FROM orders WHERE order_date_id = ?1 ORDER BY id DESC LIMIT 1 FOR UPDATE",
			nativeQuery = true)
	public Optional<Orders> getOrderByOrderDateId(String orderDateId);

	/* 根據電話號碼查詢最新的一筆訂單 */
	@Query(value = "SELECT * FROM orders WHERE order_date_id = ?1 AND phone = ?2 ORDER BY id DESC LIMIT 1", 
			nativeQuery = true)
	public GetOrdersVo getOrderByPhone(String orderDateId, String phone);

	/* 依據訂單編號查詢該筆訂單 */
	@Query(value = "SELECT * FROM orders WHERE order_date_id = ?1 AND id = ?2", nativeQuery = true)
	public Orders getOrderByOrderDateIdAndId(String orderDateId, String id);

	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態) */
	@Modifying
	@Transactional
	@Query(value = "UPDATE orders SET payment_method = ?3, transaction_id = ?4, status = ?5 WHERE id = ?1 "
			+ " AND order_date_id = ?2 AND status = 'UNPAID'", nativeQuery = true)
	public int updatePay(String id, String orderDateId, String paymentMethod, String transactionId,
			String status);
	
	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態，如果有使用優惠劵，則總金額需更改) */
	@Modifying
	@Transactional
	@Query(value = "UPDATE orders SET payment_method = ?3, transaction_id = ?4, status = ?5 WHERE id = ?1,"
			+ "total_amount = ?6 AND order_date_id = ?2", nativeQuery = true)
	public void updatePayUseDiscount(String id, String orderDateId, String paymentMethod, String transactionId,
			String status, BigDecimal totalAmount);

	/* 查詢該會員的訂單紀錄 */
	@Query(value = "SELECT * FROM orders WHERE member_id = ?1", nativeQuery = true)
	public List<GetOrdersVo> getOrderByMemberId(int memberId);

	/* 查詢該會員的訂單紀錄 */
	@Query(value = "SELECT o.id, o.order_date_id, o.global_area_id, o.total_amount, o.status, o.completed_at,"
			+ "d.quantity, d.price, d.is_gift, d.discount_note, " + "p.name as product_name " + "FROM orders o "
			+ "LEFT JOIN order_cart_details d ON o.order_cart_id = d.order_cart_id "
			+ "LEFT JOIN products p ON d.product_id = p.id " + "WHERE o.member_id = ?1 "
			+ "ORDER BY o.order_date_id DESC, o.id DESC", nativeQuery = true)
	public List<Object[]> getFullOrderHistory(int memberId);

	/* 訂單狀態更新(用於退款或取消訂單) */
	@Modifying
	@Transactional
	@Query(value = "UPDATE orders SET status = :status WHERE id = :id AND order_date_id = :orderDateId AND status = 'COMPLETED'", nativeQuery = true)
	public int updateOrderStatus(@Param("status") String status, // AI 是說要字串型態，我有說資料庫是設ENUM
			@Param("id") String id, @Param("orderDateId") String orderDateId);
	
	/* 更改總金額 */
	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態) */
	@Modifying
	@Transactional
	@Query(value = "UPDATE total_amount = ?3 WHERE id = ?1 AND order_date_id = ?2", nativeQuery = true)
	public void upDateTotalAmount(String id, String orderDateId, BigDecimal totalAmount);
	/**
	 * 檢查這個購物車 ID 是否已經被結帳（存在於訂單表中）
	 * 
	 * 條件說明：
	 *   order_cart_id = :orderCartId → 尋找這台購物車
	 * SELECT EXISTS 會回傳 boolean（1 或 0），效能最好
	 */
	@Query(value = "SELECT EXISTS(SELECT 1 FROM orders WHERE order_cart_id = :orderCartId)", 
		   nativeQuery = true)
	boolean existsByOrderCartId(@Param("orderCartId") int orderCartId);
	// =====================================================================
	// 功能A：分店長用 - 查某年某月「指定分店」所有商品銷售量
	// 說明：
	//   AND o.global_area_id = :globalAreaId → 只算該分店的訂單
	// =====================================================================
	@Query(value = "SELECT p.name AS productName, SUM(d.quantity) AS totalQuantity "
	        + "FROM orders o "
	        + "LEFT JOIN order_cart_details d ON o.order_cart_id = d.order_cart_id "
	        + "LEFT JOIN products p ON d.product_id = p.id "
	        + "WHERE o.order_date_id LIKE :yearMonth "
	        + "AND o.global_area_id = :globalAreaId "
	        + "AND o.status = 'COMPLETED' "
	        + "AND d.is_gift = 0 "
	        + "GROUP BY d.product_id, p.name "
	        + "ORDER BY totalQuantity DESC",
	        nativeQuery = true)
	List<Object[]> getMonthlySalesByBranch(
	        @Param("yearMonth") String yearMonth,
	        @Param("globalAreaId") int globalAreaId);

	// =====================================================================
	// 功能B：老闆用 - 查某年某月「指定國家」所有分店銷售前5名商品
	// =====================================================================
	@Query(value = "SELECT p.name AS productName, SUM(d.quantity) AS totalQuantity "
	        + "FROM orders o "
	        + "LEFT JOIN order_cart_details d ON o.order_cart_id = d.order_cart_id "
	        + "LEFT JOIN products p ON d.product_id = p.id "
	        + "LEFT JOIN global_area ga ON o.global_area_id = ga.id "
	        + "LEFT JOIN regions r ON ga.regions_id = r.id "
	        + "WHERE o.order_date_id LIKE :yearMonth "
	        + "AND r.id = :regionId "
	        + "AND o.status = 'COMPLETED' "
	        + "AND d.is_gift = 0 "
	        + "GROUP BY d.product_id, p.name "
	        + "ORDER BY totalQuantity DESC "
	        + "LIMIT 5",
	        nativeQuery = true)
	List<Object[]> getTop5MonthlySalesByRegion(
	        @Param("yearMonth") String yearMonth,
	        @Param("regionId") int regionId);


}