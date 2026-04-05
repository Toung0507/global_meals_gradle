package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.global_meals_gradle.entity.Promotions;
import java.util.List;

public interface PromotionsDao extends JpaRepository<Promotions, Integer> {

    /**
     * 找出目前所有在時間範圍內且啟用的活動 ID
     *
     * 條件說明：
     *   is_active = 1                    → 活動是啟用的（未手動結束）
     *   start_time <= CURRENT_DATE       → 活動已經開始
     *   end_time >= CURRENT_DATE         → 活動還沒結束
     *
     * 目前這支方法保留備用，尚未在 PromotionsService 中使用
     * 贈品活動的查詢已在 PromotionsGiftsDao.findTopQualifiedGift 裡直接 JOIN promotions 做掉
     */
    @Query(value = "SELECT id FROM promotions " +
                   "WHERE is_active = 1 " +
                   "AND start_time <= CURRENT_DATE " +
                   "AND end_time >= CURRENT_DATE", nativeQuery = true)
    List<Integer> findActivePromotionIds();

}
