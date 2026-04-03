package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.global_meals_gradle.entity.Regions;

@Repository
public interface RegionsDao extends JpaRepository<Regions, Integer> {

	/* 查找該分店所在國家的稅制與稅率(成立訂單使用) */ 
	@Query("SELECT r FROM Regions r JOIN GlobalArea g ON r.country = g.country WHERE g.id = ?1")
	public Regions findTaxByAreaId(int areaId);
}
