package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 匯入你要求的路徑
import com.example.global_meals_gradle.req.PromotionsReq;
import com.example.global_meals_gradle.res.PromotionsRes;
import com.example.global_meals_gradle.res.GiftItem; // 假設在 res 資料夾下
import com.example.global_meals_gradle.dao.*;
import com.example.global_meals_gradle.entity.*;

@Service
public class PromotionsService {

	@Autowired
	private MemberTempDao memberTempDao;

	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;

	@Autowired
	private ProductsTempDao productTempDao;

	/**
	 * 促銷活動試算邏輯
	 */
	public PromotionsRes calculate(PromotionsReq req, BigDecimal originalAmount) {

		PromotionsRes res = new PromotionsRes();
		res.setCartId(req.getCartId());
		res.setOriginalAmount(originalAmount);

		List<Integer> appliedPromotionIds = new ArrayList<>();
		List<GiftItem> receivedGifts = new ArrayList<>();
		double currentTotal = originalAmount.doubleValue();
		res.setAppliedDiscountName("");

		// 1. 處理會員折扣 (ID 1 為訪客，不處理)
		if (req.getMemberId() > 1) {
			// 先執行原生 SQL 檢查 30 天過期
			memberTempDao.resetExpiredCoupon(req.getMemberId());

			Members member = memberTempDao.findByMemberId(req.getMemberId());
			if (member != null) {
				// 如果有券且前端選擇使用
				if (member.isDiscount() && req.isUseCoupon()) {
					currentTotal = currentTotal * 0.8;
					res.setAppliedDiscountName("會員 8 折優惠");
				}
			}
		}

		// 2. 處理贈品邏輯 (傳統 For 迴圈)
		List<PromotionsGifts> activeGifts = promotionsGiftsDao.findAllActiveGifts();
		for (int i = 0; i < activeGifts.size(); i++) {
			PromotionsGifts gift = activeGifts.get(i);

			// 原始總額是否達到門檻
			if (originalAmount.compareTo(gift.getFullAmount()) >= 0) {
				appliedPromotionIds.add(gift.getPromotionsId());

				// 透過 ProductTempDao 抓取商品名稱
				String productName = productTempDao.findNameById(gift.getGiftProductId());

				GiftItem item = new GiftItem();
				item.setProductId(gift.getGiftProductId());
				item.setProductName(productName != null ? productName : "活動贈品");
				item.setQuantity(1);
				receivedGifts.add(item);
			}
		}

		// 3. 無條件進位並封裝
		res.setFinalAmount((int) Math.ceil(currentTotal));
		res.setAppliedPromotionIds(appliedPromotionIds);
		res.setReceivedGifts(receivedGifts);

		return res;
	}
}