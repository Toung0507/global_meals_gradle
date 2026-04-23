package com.example.global_meals_gradle.dao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.GlobalArea;

@Repository
public interface GlobalAreaDao extends JpaRepository<GlobalArea, Integer> {

	// 新增分店
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO global_area (regions_id, branch, address, phone, country, country_code) "
	        + " values (?1, ?2, ?3, ?4, ?5, ?6)", nativeQuery = true)
	public void insert(int regionsId, String branch, String address, String phone, String country, String countryCode);

	// 更新分店
	@Modifying
	@Transactional
	@Query(value = "UPDATE global_area SET branch = ?2, address = ?3, phone = ?4, country = ?5, country_code = ?6, regions_id = ?7 WHERE id = ?1", nativeQuery = true)
	public void update(int id, String branch, String address, String phone, String country, String countryCode, int regionsId);
	// 取得分店清單
	@Query(value = "SELECT * FROM global_area", nativeQuery = true)
	public List<GlobalArea> getAll();

	// 刪除多家分店
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM global_area WHERE id in (?1)", nativeQuery = true)
	public void delete(List<Integer> idList);

	// 根據分店 ID 查詢分店資訊,用途：從 order_cart.global_area_id → 找到分店所在國家 → 查稅務
	// 建新車，驗證GlobalArea的時候不用這個方法用內建的，因為用舊車的時候，這個GlobalAreaId傳進來就是Null
	@Query(value = "SELECT * FROM global_area WHERE id = ?1", nativeQuery = true)
	GlobalArea findById(int id);

	// 取得最後新增的一筆 ID - 思云新增，確保新增分店時，也會同步幫分店新增全部的商品庫存初始為0
	@Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
	public int findLastId();

	/**
	 * 思云新增 取得分店 ID 與名稱的對照表 (Map)。 <br>
	 * 設計目的： <br>
	 * 將資料轉換為 Map，讓後續的 service 可以透過 ID 以 O(1) 的時間複雜度， <br>
	 * 快速查詢到分店名稱，避免在迴圈中重複呼叫資料庫 (N+1 問題)。 <br>
	 */
	default Map<Integer, String> getBranchNameMap() {
		// 1. 從資料庫撈出所有分店實體，並轉為 Stream 串流開始進行處理
		return this.findAll().stream().collect(Collectors.toMap(
				// 2. Key Mapper：將分店 ID 作為 Map 的 Key
				GlobalArea::getId,

				// 3. Value Mapper：將分店名稱作為 Map 的 Value
				GlobalArea::getBranch,

				// 4. Merge Function (衝突處理)：
				// 如果出現重複的 Key (ID)，保留原本的 (existing)，忽略新的 (replacement)
				// 這能確保程式在資料庫 ID 重複時也不會崩潰
				(existing, replacement) -> existing));
	}

}
