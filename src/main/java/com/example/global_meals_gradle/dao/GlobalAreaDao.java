package com.example.global_meals_gradle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.GlobalArea;

@Repository
public interface GlobalAreaDao extends JpaRepository<GlobalArea, Integer>{
	
	// 新增分店
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO global_area (country, branch, address, phone) "
			+ " values (?1, ?2, ?3, ?4)", nativeQuery = true)
	public void insert(String country, String branch, String address, String phone);
	
	// 更新分店
	@Modifying
	@Transactional
	@Query(value = "UPDATE global_area SET branch = ?2, address = ?3, phone = ?4 WHERE id = ?1", nativeQuery = true)
	public void update(int id, String branch, String address, String phone);
	
	// 取得分店清單
	@Query(value = "SELECT * FROM global_area", nativeQuery = true)
	public List<GlobalArea> getAll();
	
	// 刪除多家分店
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM global_area WHERE id in (?1)", nativeQuery = true)
	public void delete(List<Integer> idList);

    // 根據分店 ID 查詢分店資訊,用途：從 order_cart.global_area_id → 找到分店所在國家 → 查稅務
	//建新車，驗證GlobalArea的時候不用這個方法用內建的，因為用舊車的時候，這個GlobalAreaId傳進來就是Null
	@Query(value = "SELECT * FROM global_area WHERE id = ?1", nativeQuery = true)
    GlobalArea findById(int id); 
	
}
