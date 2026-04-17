package com.example.global_meals_gradle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Staff;

@Repository
public interface StaffDao extends JpaRepository<Staff, Integer>{
	
	/* ADMIN, REGION_MANAGER, STAFF; */
	
	// 新增分店長 / 員工(is_status 預設 true)
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO staff (name, account, password, role, global_area_id, is_status, hire_at) "
			+ " values (:inputName, :inputAccount, :inputPwd, :inputRole, :inputAreaId, true, CURDATE())", nativeQuery = true)
	public void insert(//
			@Param("inputName") String name, //
			@Param("inputAccount") String account, //
			@Param("inputPwd") String password, //
			@Param("inputRole") String role, //
			@Param("inputAreaId") int globalAreaId);
	
	// ADMIN 查詢所有分店長(REGION_MANAGER -> RM)
	@Query(value = "SELECT * FROM staff WHERE role = 'REGION_MANAGER' ORDER BY global_area_id", nativeQuery = true)
	public List<Staff> getAllRM();
	
	// ADMIN 查詢全部員工(STAFF -> ST)-->目前先不用老闆茶分店長就好
	@Query(value = "SELECT * FROM staff WHERE role = 'STAFF' ORDER BY global_area_id", nativeQuery = true)
	public List<Staff> getAllST();
	
	// REGION_MANAGER 查詢所屬分店員工(STAFF -> ST)
	@Query(value = "SELECT * FROM staff WHERE role = 'STAFF' AND global_area_id = ?1 ORDER BY global_area_id", nativeQuery = true)
	public List<Staff> getSTListById(int globalAreaId);
	
	// 停權/復權
	@Modifying
	@Transactional
	@Query(value = "UPDATE staff SET is_status = ?2 WHERE id = ?1", nativeQuery = true)
	public void updateStatus(int id, boolean status);

	// 檢查帳號用
	@Query(value = "SELECT * FROM staff WHERE account = ?1", nativeQuery = true)
	public Staff findByAccount(String account);
	
	// 註冊用的格式化編碼:固定前綴 + 自動遞增四位數
	@Query(value = "SELECT account FROM staff WHERE role = ?1 ORDER BY account DESC LIMIT 1", nativeQuery = true)
	String findLastAccountByRole(String role);//用角色找最大的數字因為權限=角色
}