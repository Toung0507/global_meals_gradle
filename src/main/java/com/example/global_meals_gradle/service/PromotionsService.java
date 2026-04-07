package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.req.PromotionsReq;
import com.example.global_meals_gradle.res.PromotionsRes;
import com.example.global_meals_gradle.res.GiftItem;
import com.example.global_meals_gradle.dao.*;
import com.example.global_meals_gradle.entity.*;

@Service
public class PromotionsService {

	// 查詢 members 表，用來確認會員是否有折扣券資格
	@Autowired
	private MemberTempDao memberTempDao;

	// 查詢 promotions_gifts 表，用來找出達標的贈品活動
	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;

	// 查詢 products 表，用來抓贈品的商品名稱（避免拉到 MEDIUMBLOB 圖片欄位）
	@Autowired
	private ProductsTempDao productTempDao;

	/**
	 * 促銷活動結帳計算
	 *
	 * 流程：
	 *   1. 初始化回傳物件
	 *   2. 先處理贈品（用原始金額判斷門檻）
	 *   3. 再處理折扣（訪客跳過，會員確認是否有券且有勾選）
	 *   4. 無條件進位後封裝回傳
	 *
	 * @param req            前端傳入的請求參數（購物車ID、會員ID、是否使用折扣券）
	 * @param originalAmount 這張購物車的原始總金額（由呼叫方計算好傳進來）
	 */
	public PromotionsRes calculate(PromotionsReq req, BigDecimal originalAmount) {

		// 初始化回傳物件，先把購物車 ID 和原始金額填進去
		PromotionsRes res = new PromotionsRes();
		res.setCartId(req.getCartId());
		res.setOriginalAmount(originalAmount);

		// 準備收集：觸發贈品的活動 ID、贈品清單
		List<Integer> appliedPromotionIds = new ArrayList<>();
		List<GiftItem> receivedGifts = new ArrayList<>();

		// currentTotal 是會隨折扣變動的金額，初始值等於原始金額
		// 贈品判斷不用這個，折扣才用這個
		double currentTotal = originalAmount.doubleValue();

		// 折扣名稱預設為空字串，有打折才會填入
		res.setAppliedDiscountName("");

		// =============================================
		// 步驟一：贈品判斷（用 originalAmount 判斷，與折扣無關）
		// =============================================

		// 從 DB 取出：在時間範圍內、活動啟用、贈品啟用、還有庫存（quantity=-1 或 >0）
		// 且 originalAmount >= full_amount 的所有達標贈品中，取門檻最高的那一筆
		PromotionsGifts topGift = promotionsGiftsDao.findTopQualifiedGift(originalAmount);

		// 如果有找到達標贈品
		if (topGift != null) {

			// 記錄這筆贈品對應的促銷活動 ID（promotions.id）
			appliedPromotionIds.add(topGift.getPromotionsId());

			// 用 gift_product_id 去 products 表查商品名稱
			// 使用 ProductsTempDao 是為了避免把 MEDIUMBLOB（圖片）也一起撈出來
			String productName = productTempDao.findNameById(topGift.getGiftProductId());

			// 組成 GiftItem 放進回傳清單
			GiftItem item = new GiftItem();
			item.setProductId(topGift.getGiftProductId());

			// 若查不到名稱（例如商品已被刪除），給預設值
			item.setProductName(productName != null ? productName : "活動贈品");

			// 數量處理：
			//   quantity = -1 → 無限供應，回傳 -1 讓前端決定如何顯示（例如不顯示數量）
			//   quantity > 0  → 有限供應，回傳實際數字
			item.setQuantity(topGift.getQuantity());

			receivedGifts.add(item);
		}

		// =============================================
		// 步驟二：折扣判斷（用 currentTotal 計算，會影響最終金額）
		// =============================================

		// memberId = 1 是訪客，訪客沒有折扣資格，直接跳過這整段
		if (req.getMemberId() > 1) {

			// 從 members 表查出這位會員的資料
			Members member = memberTempDao.findByMemberId(req.getMemberId());

			// 查不到這位會員，直接拋出例外
			if (member == null) {
				throw new RuntimeException(ReplyMessage.MEMBER_NOT_FOUND.getMessage());
			}

			// 使用者勾選了使用折扣券，但 members.is_discount = 0（沒有券）
			// 屬於不合法的請求，直接攔截
			if (req.isUseCoupon() && !member.isDiscount()) {
				throw new RuntimeException(ReplyMessage.MEMBER_COUPON_NOT_AVAILABLE.getMessage());
			}

			// 判斷兩個條件都成立才打折：
			//   1. members.is_discount = 1（會員有折扣券，代表累積訂單已達 10 次）
			//   2. 前端 useCoupon = true（使用者有勾選使用折扣券）
			if (member.isDiscount() && req.isUseCoupon()) {
				// 打八折
				currentTotal = currentTotal * 0.8;
				// 告訴前端這次有套用折扣，名稱固定為此字串
				res.setAppliedDiscountName("會員 8 折優惠");
			}
		}

		// =============================================
		// 步驟三：無條件進位，封裝所有結果回傳
		// =============================================

		// currentTotal 無條件進位後轉成整數（例如 801.2 → 802）
		res.setFinalAmount((int) Math.ceil(currentTotal));

		// 把收集到的促銷活動 ID 和贈品清單放進回傳物件
		res.setAppliedPromotionIds(appliedPromotionIds);
		res.setReceivedGifts(receivedGifts);

		return res;
	}

}
