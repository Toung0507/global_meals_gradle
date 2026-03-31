package com.example.global_meals_gradle.dao;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.global_meals_gradle.entity.PromotionsGifts;

@Repository
public interface PromotionsGiftsDao extends JpaRepository<PromotionsGifts, Integer> {

    /* 新增贈品細項 */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO promotions_gifts(promotions_id, full_amount, gift_product_id, is_active) " +
                   "VALUES (?1, ?2, ?3, ?4)", nativeQuery = true)
    void insert(int promotionsId, BigDecimal fullAmount, int giftProductId, boolean active);
    
    /* 贈品狀態改變: 上下架 */
    @Modifying
    @Transactional
    @Query(value = "UPDATE promotions_gifts SET is_active = NOT is_active WHERE id = ?1", nativeQuery = true)
    void updateStatus(int id);

    /* 核心邏輯：取得所有目前上架中的贈品門檻 */
    @Query(value = "SELECT * FROM promotions_gifts WHERE is_active = 1", nativeQuery = true)
    List<PromotionsGifts> findAllActiveGifts();
}