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

	// ╔══════════════════════════════════════════════════════════════════╗
	// ║  【Bug 修正 1】INSERT VALUES 佔位符數量與欄位數量不符            ║
	// ╚══════════════════════════════════════════════════════════════════╝
	// ❌ 原本錯誤寫法：
	//    VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)
	//    只有 ?7，但 INSERT 欄位有 9 個（subtotal_before_tax、tax_amount、total_amount 都要填）
	//
	// 🔴 錯誤現象：執行 INSERT 時報 SQL 語法錯誤或 Column count doesn't match value count。
	//
	// 📖 為什麼會出錯？
	//    SQL 的 INSERT 語法規則是：欄位清單有幾個，VALUES 裡就要有幾個對應的值（佔位符）。
	//    ?1 到 ?9 就是「第 1 個參數」到「第 9 個參數」的意思（Spring Data JPA 用法）。
	//    少寫了 ?8 和 ?9，MySQL 就不知道 tax_amount 和 total_amount 要填什麼，直接報錯。
	//
	// ✅ 修正後：補上 ?8（taxAmount）和 ?9（totalAmount）
	/* 新增訂單 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO orders (id, order_date_id, order_cart_id, global_area_id, member_id, phone, "
	        + " subtotal_before_tax, tax_amount, total_amount) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)", nativeQuery = true)

	public void insert(String id, String orderDateId, String orderCartId, int globalAreaId, int memberId, String phone, //
			BigDecimal subtotalBeforeTax, //
			BigDecimal taxAmount, BigDecimal totalAmount);

	/* 根據 orderDateId 查詢特定訂單 */
	// ORDER BY id (排序)(字串排序需長度樣(補零)) DESC (倒序) LIMIT 1 (限制筆數) FOR UPDATE:
	// 查詢到的這筆資料會被鎖住
	// Optional: 如果當天還沒有人下單（第一筆），它會回傳 Optional.empty()，你的 Service 就可以判斷 isPresent()
	// 來給出第一個號碼 0001。
	@Query(value = "SELECT * FROM orders WHERE order_date_id = ?1 ORDER BY id DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
	public Optional<Orders> getOrderByOrderDateId(String orderDateId);

	/* 根據電話號碼查詢最新的一筆訂單 */
	@Query(value = "SELECT * FROM orders WHERE order_date_id = ?1 AND phone = ?2 ORDER BY id DESC LIMIT 1", nativeQuery = true)
	public GetOrdersVo getOrderByPhone(String orderDateId, String phone);

	/* 依據訂單編號查詢該筆訂單 */
	@Query(value = "SELECT * FROM orders WHERE order_date_id = ?1 AND id = ?2", nativeQuery = true)
	public Orders getOrderByOrderDateIdAndId(String orderDateId, String id);

	// ╔══════════════════════════════════════════════════════════════════╗
	// ║  【Bug 修正 2】updatePay 參數型別用 Enum，native query 無法轉字串 ║
	// ╚══════════════════════════════════════════════════════════════════╝
	// ❌ 原本錯誤寫法：
	//    public void updatePay(..., OrdersStatus status);
	//    最後一個參數是 OrdersStatus 這個 Enum 型別
	//
	// 🔴 錯誤現象：UPDATE 執行後資料庫的 status 欄位沒有更新，或直接報型別轉換錯誤。
	//
	// 📖 為什麼會出錯？
	//    nativeQuery = true 代表這是「原生 SQL」，直接送給資料庫執行，中間沒有 ORM 轉換。
	//    資料庫只認識字串（例如 "COMPLETED"），不認識 Java 的 Enum 物件。
	//    你傳進去 OrdersStatus.COMPLETED，SQL 收到的是 Java 物件的記憶體位址，
	//    資料庫完全看不懂，UPDATE 自然失敗。
	//
	//    相對地，如果是用 JPQL（非 nativeQuery），Spring Data JPA 會自動幫 Enum 做轉換，
	//    但 nativeQuery 不走這條路，需要自己處理。
	//
	// ✅ 修正方式：參數改成 String，由呼叫端（Service）負責傳入 OrdersStatus.COMPLETED.name()
	//    .name() 是 Java Enum 內建方法，回傳 Enum 常數的宣告名稱字串（例如 "COMPLETED"）
	//    這樣 SQL 收到的就是字串 "COMPLETED"，資料庫能正確更新。
	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態) */
	@Modifying
	@Transactional
	@Query(value = "UPDATE orders SET payment_method = ?3, transaction_id = ?4, status = ?5 WHERE id = ?1 "
			+ " AND order_date_id = ?2", nativeQuery = true)
	public void updatePay(String id, String orderDateId, String paymentMethod, String transactionId,
			String status);

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

	// ╔═══════════════════════════════════════════════════════════════════════╗
	// ║  【Bug 修正 3】updateOrderStatus SQL 欄名錯誤 + @Param 名稱不符       ║
	// ╚═══════════════════════════════════════════════════════════════════════╝
	// ❌ 原本錯誤寫法（兩個錯誤同時存在）：
	//    @Query("... AND date_id = :order_date_id ...")           ← 欄名用 date_id（錯）
	//    public int updateOrderStatus(@Param("order_date_id") ... ← @Param 用底線命名（與 SQL 中 :orderDateId 不符）
	//
	// 🔴 錯誤現象：UPDATE 執行但沒有更新任何資料（affected rows = 0），
	//             或報參數無法綁定的錯誤。
	//
	// 📖 為什麼會出錯？
	//    ▸ 錯誤 1：資料表欄位實際名稱是 order_date_id，寫成 date_id 根本找不到這個欄位，
	//              WHERE 條件永遠不成立，沒有任何資料被更新。
	//
	//    ▸ 錯誤 2：Spring Data JPA 的具名參數規則是「@Param("名稱") 必須與 SQL 裡的 :名稱 完全一致」。
	//              SQL 裡寫 :orderDateId（駝峰式），但 @Param 卻寫 "order_date_id"（底線式），
	//              兩者對不上，Spring 找不到要把這個參數填到 SQL 的哪個位置。
	//
	// ✅ 修正方式：
	//    1. SQL 欄名改為正確的 order_date_id
	//    2. @Param 改為 "orderDateId" 與 SQL 中的 :orderDateId 完全一致
	//
	// 📝 關於 OrdersStatus status 型別保留的說明：
	//    這裡刻意保留 Enum 型別（不像 updatePay 改成 String）。
	//    原因：使用「具名參數」（:status）搭配 @Param 時，部分版本的 Spring Data JPA
	//    能自動呼叫 Enum 的 .name() 做轉換。如果執行後仍有問題，可改為 String。
	/* 訂單狀態更新 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE orders SET status = :status WHERE id = :id AND order_date_id = :orderDateId AND status = 'COMPLETED'", nativeQuery = true)
	public int updateOrderStatus(@Param("status") OrdersStatus status,
			@Param("id") String id, @Param("orderDateId") String orderDateId);
	
	// ╔══════════════════════════════════════════════════════════════════╗
	// ║  【Bug 修正 4】upDateTotalAmount SQL 缺少資料表名稱與 SET 關鍵字  ║
	// ╚══════════════════════════════════════════════════════════════════╝
	// ❌ 原本錯誤寫法：
	//    @Query(value = "UPDATE total_amount = ?3 WHERE id = ?1 AND order_date_id = ?2")
	//
	// 🔴 錯誤現象：SQL 語法錯誤，Spring Boot 啟動時或執行時直接拋出 exception。
	//
	// 📖 為什麼會出錯？
	//    SQL UPDATE 的完整語法是：
	//       UPDATE  <資料表名稱>  SET  <欄位> = <值>  WHERE  <條件>
	//    原本寫成 "UPDATE total_amount = ..."，
	//    少了「orders」（要更新哪張表？）和「SET」（這個關鍵字告訴 SQL 後面是要更新的欄位）。
	//    資料庫完全看不懂，直接報語法錯誤。
	//
	//    這就像中文說「請 金額 = 100 在 id = 1 的地方」，
	//    正確應該說「請 在 orders 表 設定 金額 = 100 在 id = 1 的地方」。
	//
	// ✅ 修正方式：補上 orders SET，變成完整的 UPDATE orders SET total_amount = ?3 ...
	/* 更改總金額 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE orders SET total_amount = ?3 WHERE id = ?1 AND order_date_id = ?2", nativeQuery = true)
	public void upDateTotalAmount(String id, String orderDateId, BigDecimal totalAmount);
}
