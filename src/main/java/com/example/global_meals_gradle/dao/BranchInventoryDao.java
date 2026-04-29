package com.example.global_meals_gradle.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.BranchInventory;

@Repository
public interface BranchInventoryDao extends JpaRepository<BranchInventory, Integer> {

	// 1-1. 修改庫存 => 分店長直接修改 (覆蓋庫存) => 改成使用 saveAll
	// @Modifying
	// @Transactional
	// @Query(value = "UPDATE branch_inventory " //
	// + " SET stock_quantity = ?3 " //
	// + " WHERE product_id = ?1 AND global_area_id = ?2", nativeQuery = true)
	// int updateStockDirectly(int productId, int globalAreaId, int newStock);

	// 1-2. 修改庫存 => 訂單下單扣庫存
	@Modifying
	@Transactional
	@Query(value = "UPDATE branch_inventory " //
			+ " SET stock_quantity = stock_quantity - :quantityToBuy, " //
			+ " version = version + 1 " //
			+ " WHERE product_id = :productId " //
			+ " AND global_area_id = :globalAreaId " //
			+ " AND stock_quantity >= :quantityToBuy " //
			+ " AND version = :oldVersion", nativeQuery = true)
	public int updateBranchStock( //
			@Param("productId") int productId, //
			@Param("globalAreaId") int globalAreaId, //
			@Param("quantityToBuy") int quantityToBuy, //
			@Param("oldVersion") int oldVersion);

	// 2. 修改售價 => 改成使用 saveAll
	// @Modifying
	// @Transactional
	// @Query(value = "UPDATE branch_inventory SET base_price = ?3 " //
	// + " WHERE product_id = ?1 AND global_area_id = ?2", nativeQuery = true)
	// public int updatePrice(int productId, int globalAreaId, BigDecimal
	// basePrice);

	// 3. 查詢某分店某商品的庫存量 - 給組員 1 使用
	@Query(value = "SELECT * FROM branch_inventory " //
			+ " WHERE product_id = ?1 AND global_area_id = ?2", nativeQuery = true)
	public BranchInventory findByProductIdAndAreaId(int productId, int globalAreaId);

	// 4. 查詢某分店某商品的庫存量 - 給組員 2 使用
	// 但其實 3 跟 4 是一樣的結果只是取用方式不同
	public Optional<BranchInventory> findByProductIdAndGlobalAreaId(int productId, int globalAreaId);

	// 5. 查詢某分店某商品的庫存量 - 查詢分店庫存的使用，以防有誤呼叫的狀況底下
	@Query(value = "SELECT * FROM branch_inventory " //
			+ " WHERE product_id = ?1 AND global_area_id = ?2", nativeQuery = true)
	List<BranchInventory> findByProductIdAndGlobalAreaIdForStaff(int productId, int globalAreaId);

	// 6. 分店菜單列表 (JOIN 兩表)
	// 以庫存表為主，撈出該分店所有「上架」商品
	@Query(value = "SELECT p.*, bi.base_price, bi.stock_quantity, bi.active " //
			+ "  FROM branch_inventory AS bi " //
			+ "  JOIN products AS p " //
			+ "    ON bi.product_id = p.id " //
			+ " WHERE bi.global_area_id = ?1 " //
			+ "   AND p.is_active = 1 AND p.deleted_at IS NULL", nativeQuery = true)
	public List<Object[]> getMenuByArea(int globalAreaId);

	// 7. 透過分店 ID 找出全部的商品庫存
	public List<BranchInventory> findByGlobalAreaId(int globalAreaId);

	// 8. 透過商品 ID 找出全部相對應的分店庫存
	public List<BranchInventory> findByProductId(int productId);
}
