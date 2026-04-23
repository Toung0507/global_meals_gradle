package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.global_meals_gradle.entity.AiGenerated;

public interface AiGeneratedDao extends JpaRepository<AiGenerated, Integer> {
	// 用 save 就好
}
