package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.global_meals_gradle.entity.MonthlyFinancialReports;

@Repository
public interface MonthlyFinancialReportsDao extends JpaRepository<MonthlyFinancialReports, Integer> {

}
