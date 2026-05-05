package com.example.global_meals_gradle.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.global_meals_gradle.entity.Category;

public interface CategoryDao extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);
}