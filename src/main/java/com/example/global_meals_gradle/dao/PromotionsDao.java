package com.example.global_meals_gradle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.global_meals_gradle.entity.Promotions;

public interface PromotionsDao extends JpaRepository<Promotions, Integer> {

    /**
     * 撈出所有目前有效（上架中且在時間範圍內）的活動，回傳完整 Promotions 物件
     *
     * 條件說明：
     *   is_active = 1                    → 活動是啟用的（未手動結束）
     *   start_time <= CURRENT_DATE       → 活動已經開始
     *   end_time >= CURRENT_DATE         → 活動還沒結束
     *
     * 與 PromotionsGiftsDao 裡各方法的差別：
     *   這裡只查活動本身（promotions 表），不 JOIN promotions_gifts
     *   使用時機：CartService 需要活動名稱或完整活動物件時使用
     */
    @Query(value = "SELECT * FROM promotions " +
                   "WHERE is_active = 1 " +
                   "AND start_time <= CURRENT_DATE " +
                   "AND end_time >= CURRENT_DATE", nativeQuery = true)
    List<Promotions> findActivePromotions();

}
