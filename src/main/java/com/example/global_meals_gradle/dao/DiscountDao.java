package com.example.global_meals_gradle.dao; // 宣告此介面所屬的套件路徑

import java.util.List; // 回傳多筆資料時使用

import org.springframework.data.jpa.repository.JpaRepository; // 繼承 JPA 基本 CRUD 方法（save、findById、existsById 等）
import org.springframework.data.jpa.repository.Modifying; // 標記為修改型查詢（UPDATE / DELETE），搭配 @Query 使用
import org.springframework.data.jpa.repository.Query; // 自訂原生 SQL 查詢語句
import org.springframework.data.repository.query.Param; // 將方法參數綁定到 SQL 的具名參數（:paramName）

import com.example.global_meals_gradle.entity.Discount; // discount 表對應的 JPA Entity

// JpaRepository<Discount, Integer>：
//   第一個泛型 = 操作的 Entity 類別
//   第二個泛型 = 主鍵（id）的資料型別
public interface DiscountDao extends JpaRepository<Discount, Integer> {

	/* 查詢全部 discount 記錄 */
	@Query(value = "SELECT * FROM discount", nativeQuery = true) // 查出 discount 表所有資料
	List<Discount> findAllDiscounts(); // 回傳全部折抵記錄清單

	/* 根據主鍵 id 查詢單筆 discount */
	@Query(value = "SELECT * FROM discount WHERE id = :id", nativeQuery = true) // 以主鍵精準定位
	Discount findDiscountById(@Param("id") int id); // :id 綁定主鍵參數；查不到時回傳 null

	/* 根據 regions_id 查詢對應的 discount 記錄 */
	@Query(value = "SELECT * FROM discount WHERE regions_id = :regionsId", nativeQuery = true) // 以 regions_id 查詢
	Discount findByRegionsId(@Param("regionsId") int regionsId); // :regionsId 綁定國家區域 ID；查不到時回傳 null

	/* 修改指定 discount 的折抵上限（usage_cap） */
	@Modifying // 標記為修改型查詢（UPDATE）
	@jakarta.transaction.Transactional // 此方法自帶交易，確保 UPDATE 原子性
	@Query(value = "UPDATE discount SET usage_cap = :usageCap WHERE id = :id", nativeQuery = true) // 只更新 usage_cap 欄位
	void updateUsageCap(@Param("id") int id, @Param("usageCap") int usageCap); // :id 綁定主鍵；:usageCap 綁定新的折抵上限值

	/* 修改指定 discount 的累積次數（count） */
	@Modifying // 標記為修改型查詢（UPDATE）
	@jakarta.transaction.Transactional // 此方法自帶交易，確保 UPDATE 原子性
	@Query(value = "UPDATE discount SET count = :count WHERE id = :id", nativeQuery = true) // 只更新 count 欄位
	void updateCount(@Param("id") int id, @Param("count") int count); // :id 綁定主鍵；:count 綁定新的累積次數

	/* 刪除指定 discount 記錄 */
	@Modifying // 標記為修改型查詢（DELETE）
	@jakarta.transaction.Transactional // 此方法自帶交易，確保 DELETE 原子性
	@Query(value = "DELETE FROM discount WHERE id = :id", nativeQuery = true) // 真刪除指定主鍵的記錄
	void deleteDiscountById(@Param("id") int id); // :id 綁定主鍵參數

}
