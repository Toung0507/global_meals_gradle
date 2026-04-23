package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.global_meals_gradle.entity.Promotions;
import java.util.List;

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

    /**
     * 客戶端專用：取得「全球活動（global_area_id IS NULL）」+「指定分店活動」
     * 用於在客戶端只顯示該分店相關的活動，不顯示其他分店的活動
     */
    @Query(value = "SELECT * FROM promotions " +
                   "WHERE (global_area_id = ?1 OR global_area_id IS NULL) " +
                   "AND is_active = 1 " +
                   "AND start_time <= CURRENT_DATE " +
                   "AND end_time >= CURRENT_DATE " +
                   "ORDER BY CASE WHEN global_area_id IS NULL THEN 1 ELSE 0 END, id", nativeQuery = true)
    List<Promotions> findActiveByAreaOrGlobal(int globalAreaId);

    /**
     * 管理端：取得所有促銷活動（不篩選分店，供 getList() 使用）
     * 已由 JpaRepository.findAll() 提供，此處額外提供依分店篩選的版本
     */
    @Query(value = "SELECT * FROM promotions " +
                   "WHERE (global_area_id = ?1 OR global_area_id IS NULL) " +
                   "ORDER BY id", nativeQuery = true)
    List<Promotions> findAllByAreaOrGlobal(int globalAreaId);

}
