package com.example.global_meals_gradle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Products;

@Repository
public interface ProductsDao extends JpaRepository<Products, Integer> {

	// 1. 【隱形功能】繼承來的 .save(entity)
	// 用途：處理「新增」與「修改」的所有情況。
	// 優點：自動處理 byte[] 圖片，不管是新商品還是改舊商品，通通丟進去就對了。

	// 2. 【隱形功能】繼承來的 .findById(id)
	// 用途：修改商品前，先撈出舊資料。

	// 3. 依區域搜尋商品的方法 (自動命名)
	public List<Products> findByRegionCountry(String regionCountry);

	// 4. 給前台看的菜單方法 (手寫 SQL)
	@Query(value = "SELECT * FROM products WHERE region_country = ?1 AND "//
			+ "is_active = 1 AND deleted_at IS NULL", nativeQuery = true)
	public List<Products> getMenu(String regionCountry);

	// 5. 軟刪除方法 (手寫 SQL)
	@Modifying
	@Transactional
	@Query(value = "UPDATE products SET deleted_at = NOW() , is_active = 0 WHERE id = ?1 ", nativeQuery = true)
	public int softDeleteProduct(int productsId);

	6. 扣庫存的方法 (手動實作樂觀鎖) -- > 到時候合併邵穎的寫法
	@Modifying
	@Transactional
	@Query(value = "UPDATE products SET stock_quantity = ?2, version = version + 1 "
			+ "WHERE id = ?1 AND version = ?3", nativeQuery = true)
	public int updateStockWithOptimisticLock(int productsId, int newStock, int currentVersion);

  	//邵穎用在雙層迴圈，改成用 JOIN 先註解不使用
	@Query(value = "select name from products where id = ?1", nativeQuery = true)
	public String getProductsNameById(int id);
	
	/*  用於訂單成立 */
	@Query(value = "select * from products where id = ?1", nativeQuery = true)
	public Products findById(int id);

	/* 庫存減少 */
	// stock_quantity >= ?2: 防止「沒鎖好」的意外
	@Modifying
	@Transactional
	@Query(value = "update products set stock_quantity = stock_quantity - ?2 where id = ?1 and stock_quantity >= ?2", nativeQuery = true)
	public int upDateStock(int id, int stockQuantity);
	
	// 7. 前台實時確認庫存 (只抓數字，不抓整張表，效能最快)
	@Query(value = "SELECT stock_quantity FROM products WHERE id = ?1 AND deleted_at IS NULL", nativeQuery = true)
	public Integer getStockById(int productsId);

}
