package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.global_meals_gradle.entity.Members;

public interface MemberTempDao extends JpaRepository<Members, Integer> {

	/* 根據 ID 取得會員資料 */
	@Query(value = "SELECT * FROM members WHERE id = ?1", nativeQuery = true)
	public Members findByMemberId(int id);

}