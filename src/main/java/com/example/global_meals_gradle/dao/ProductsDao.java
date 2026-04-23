package com.example.global_meals_gradle.dao;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Products;

@Repository
public interface ProductsDao extends JpaRepository<Products, Integer> {

	List<Products> findByActiveIsTrueAndDeletedAtIsNull();

	Optional<Products> findByIdAndDeletedAtIsNull(int id);

	// 1. 【隱形功能】繼承來的 .save(entity)
	// 用途：處理「新增」與「修改」的所有情況。
	// 優點：自動處理 byte[] 圖片，不管是新商品還是改舊商品，通通丟進去就對了。

	// 2. 修改商品基本資訊 - 透過 .save 讓 JPA 自己去判斷，但先留著備註
	// @Modifying
	// @Transactional
	// @Query(value = "UPDATE products SET name = ?1, category = ?2, description = ?3, "//
	//	 	+ "food_img = ?4 WHERE id = ?5", nativeQuery = true)
	// public int updateProductInfo(String name, String category, String description, byte[] img, int id);

	// 3. 軟刪除商品
	@Modifying
	@Transactional
	@Query(value = "UPDATE products SET deleted_at = NOW(), is_active = 0 WHERE id = ?1", //
			nativeQuery = true)
	public int softDeleteProduct(int id);

	// 4. 查詢商品名稱 (贈品顯示用)
	@Query(value = "SELECT name FROM products WHERE id = :id", nativeQuery = true)
	public String findNameById(@Param("id") int id);

	// 5. 取得特定 ID 商品 ==> 只可以看到商品名稱 & 圖片 (艷羽有使用)
	@Query(value = "SELECT * FROM products WHERE id = ?1", nativeQuery = true)
	public Products findById(int id);

	// 6. 檢查名稱是否存在 (自動產生 SELECT COUNT(*) ...)
	public boolean existsByName(String name);

	// 7. 更新時檢查名稱是否存在
	@Query(value = "SELECT COUNT(*) > 0 FROM products WHERE name = :name AND id <> :id", //
			nativeQuery = true)
	public boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") int id);
	
	// 8. 查詢所有「未刪除」的商品 (管理者清單頁)
    @Query(value = "SELECT * FROM products WHERE deleted_at IS NULL", nativeQuery = true)
    public List<Products> findByDeletedAtIsNull();

    // 9. 查詢所有「已刪除」的商品 (垃圾桶頁面)
    @Query(value = "SELECT * FROM products WHERE deleted_at IS NOT NULL", nativeQuery = true)
    public List<Products> findByDeletedAtIsNotNull();
}