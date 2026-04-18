package com.example.global_meals_gradle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.global_meals_gradle.entity.MonthlyFinancialReports;

@Repository
public interface MonthlyFinancialReportsDao extends JpaRepository<MonthlyFinancialReports, Integer> {

	// 取的特定分店的月營業額(for 店長)
	@Query(value = "SELECT reports.report_date AS reportDate, g.name AS branchName, //"
			+ "r.name AS regionsName, reports.total_amount AS totalAmount " +
            "FROM monthly_financial_reports reports " +
            "JOIN global_area g ON reports.global_area_id = g.id " +
            "JOIN regions r ON g.regions_id = r.id " +
            "WHERE reports.report_date IN (?1) " +
            "reports.branch_id = ?2 ", nativeQuery = true)
	public List<Object[]> getReportByDateIdAndBranchId(List<String> reportDate, int branchId);
	
	// 取的所有分店的月營業額(for 老闆)
	@Query(value = "SELECT reports.report_date AS reportDate, g.name AS branchName, //"
			+ "r.name AS regionsName, reports.total_amount AS totalAmount " +
            "FROM monthly_financial_reports reports " +
            "JOIN global_area g ON reports.global_area_id = g.id " +
            "JOIN regions r ON g.regions_id = r.id " +
            "WHERE reports.report_date IN (?1) " , nativeQuery = true)
	public List<Object[]> getReportByDateId(List<String> reportDate);
}
