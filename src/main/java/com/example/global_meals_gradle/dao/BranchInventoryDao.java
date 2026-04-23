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

	public Optional<BranchInventory> findByProductIdAndGlobalAreaId(int productId, int globalAreaId);

	public List<BranchInventory> findByGlobalAreaId(int globalAreaId);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE branch_inventory "
			+ "SET stock_quantity = stock_quantity - :quantityToBuy, "
			+ "version = version + 1 "
			+ "WHERE product_id = :productId "
			+ "AND global_area_id = :globalAreaId "
			+ "AND stock_quantity >= :quantityToBuy "
			+ "AND version = :oldVersion", nativeQuery = true)
	public int updateBranchStock( //
			@Param("productId") int productId, //
			@Param("globalAreaId") int globalAreaId, //
			@Param("quantityToBuy") int quantityToBuy, //
			@Param("oldVersion") int oldVersion
	);
}
