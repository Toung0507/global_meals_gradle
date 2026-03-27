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

	// [練習 1] 寫一個依區域搜尋的方法 (自動命名)
	public List<Products> findByRegionCountry(String regionCountry);

	// [練習 2] 寫一個給前台看的菜單方法 (手寫 SQL)
	@Query(value = "SELECT * FROM products WHERE region_country = ?1 AND "//
			+ "is_active = 1 AND deleted_at IS NULL", nativeQuery = true)
	public List<Products> getMenu(String regionCountry);

	// [練習 3] 寫一個軟刪除方法 (手寫 SQL)
	@Modifying
	@Transactional
	@Query(value = "UPDATE products SET deleted_at = NOW() , isWHERE id = ?1 ", nativeQuery = true)
	public int softDeleteProduct(int productsId);

	// [練習 4] 寫一個扣庫存的方法 (手寫 SQL)
	@Modifying
	@Transactional
	@Query(value = "UPDATE products SET stock_quantity = ?2 WHERE id = ?1", nativeQuery = true)
	public int updateStockQuantity(int productsId, int stock);
}
