package com.example.global_meals_gradle.dao;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.example.global_meals_gradle.entity.Promotions;

public interface PromotionsDao extends JpaRepository<Promotions, Integer> {

    /* 新增活動方案 */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO promotions(name, start_time, end_time, max_exchange, exchange_count) " +
                   "VALUES (?1, ?2, ?3, ?4, 0)", nativeQuery = true)
    void insert(String name, LocalDate startTime, LocalDate endTime, int maxExchange);
    
    /* 取得目前活動的最大 id */
    @Query(value = "SELECT MAX(id) FROM promotions", nativeQuery = true)
    int getMaxId();

    /* 根據 ID 查詢活動名稱 (用於 Res 回傳) */
    @Query(value = "SELECT name FROM promotions WHERE id = ?1", nativeQuery = true)
    String findNameById(int id);
}