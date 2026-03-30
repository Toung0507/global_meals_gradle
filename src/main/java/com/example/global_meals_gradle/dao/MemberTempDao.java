package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.global_meals_gradle.entity.Members;

import jakarta.transaction.Transactional;

public interface MemberTempDao extends JpaRepository<Members, Integer> {

    /* 根據 ID 取得會員資料 */
    @Query(value = "SELECT * FROM members WHERE id = ?1", nativeQuery = true)
    public Members findByMemberId(int id);

    /* 檢查並重置過期券 (30天邏輯：現在時間 > 最後結帳日 + 30天) */
    @Modifying
    @Transactional
    @Query(value = "UPDATE members SET is_discount = 0, order_count = 0 " +
                   "WHERE id = ?1 AND is_discount = 1 " +
                   "AND NOW() > DATE_ADD(coupon_expired_at, INTERVAL 30 DAY)", nativeQuery = true)
    public void resetExpiredCoupon(int id);
}