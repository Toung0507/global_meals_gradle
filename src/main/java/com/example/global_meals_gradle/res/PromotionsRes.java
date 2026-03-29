package com.example.global_meals_gradle.res;

import java.math.BigDecimal;
import java.util.List;

public class PromotionsRes {
	private int cartId;                        // 購物車 ID
    private List<Integer> appliedPromotionIds; // 所有參與到的贈品活動 ID
    private String appliedDiscountName;        // 若用券則為 "會員 8 折優惠"，否則為空
    private BigDecimal originalAmount;         // 原始總價
    private int finalAmount;                   // 最終金額 (折扣後且無條件進位)
    private List<GiftItem> receivedGifts;      // 贈品清單
}
