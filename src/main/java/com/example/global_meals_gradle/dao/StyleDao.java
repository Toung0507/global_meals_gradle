package com.example.global_meals_gradle.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.global_meals_gradle.entity.Style;

public interface StyleDao extends JpaRepository<Style, Integer> {
    Optional<Style> findByName(String name);
}
