package com.example.global_meals_gradle.service;

import com.example.global_meals_gradle.dao.*;
import com.example.global_meals_gradle.entity.*;
import com.example.global_meals_gradle.req.*;
import com.example.global_meals_gradle.res.*;

import com.example.global_meals_gradle.constants.OperationType;
import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.TaxType;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

	@Autowired
	private OrderCartDao orderCartDao;

	@Autowired
	private OrderCartDetailsDao orderCartDetailsDao;

	@Autowired
	private ProductsDao productsDao;
	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;
//	@Autowired
//	private MembersDao membersDao;

//	核心 API 1:同步購物車，包括刪除單品
//	 前端呼叫時機：使用者改了數量且「停手 1 秒後」（Debounce 防抖邏輯）
	@Transactional
	public CartViewRes syncItem(CartSyncReq req) {

		int currentCartId;

//		 步驟 1：判斷這是第一件商品（要建新車），還是後面加的商品（沿用舊車）
		if (req.getCartId() == null) {

//			 劇本分支 A：建新車
//        	A-1.準備一個裝新購物車的空殼子
			OrderCart newCart = new OrderCart();
//        	A-2:塞東西進去，讓newCart各個屬性完整
			newCart.setGlobalAreaId(req.getGlobalAreaId());
//			   字串轉列舉
			newCart.setOperationType(OperationType.valueOf(req.getOperationType()));

//			操作者：員工點餐存 staffId，顧客點餐存 memberId
			if ("STAFF".equals(req.getOperationType())) {
				newCart.setOperation(req.getStaffId()); // 員工點餐存 staffId
			} else {
				newCart.setOperation(req.getMemberId());
			}

//			 這裡必須用 Spring Data JPA 內建的 save(),「新建主表」時我們強烈建議用 save()！
//			 因為 save() 執行完，資料庫自動生成的 ID 會立刻被裝回 newCart.getId() 裡面。
//			 我們馬上就能拿到剛建好的「7號」，提供給後面的商品明細使用。原生 SQL 很難做到這點！
//            A-3:用DAO將塞滿req資料的OrderCart存到資料庫去
			orderCartDao.save(newCart);
//			 把剛建好的newCart的Id值獲取到，至此新購物車的id就已經知道了
			currentCartId = newCart.getId();

		} else {
//			 劇本分支 B：已經有車了,id沿用
			currentCartId = req.getCartId();
		}

//		 步驟 2：處理這筆商品 的增刪改
//		 這時候我們有了 currentCartId (購物車號碼)，以及 req 傳來的 productId 和 quantity

		if (req.getQuantity() == 0) {
//			 ==== 情境 2-1：刪除商品 ====
			orderCartDetailsDao.deleteByCartIdAndProductId(currentCartId, req.getProductId());

		} else {
//			 ==== 情境 2-2：數量大於 0，加入新商品 或 修改數量 ====
//			 先找找看，這台車裡面是不是已經有這個商品了
			OrderCartDetails existingDetail = orderCartDetailsDao.findByCartIdAndProductId(currentCartId,
					req.getProductId());

			if (existingDetail != null) {
//				 情況 A：購物車裡已經有了，單純把數量「覆蓋」成前端傳來的新數量
				existingDetail.setQuantity(req.getQuantity());

//				 再次呼叫 save()！在此它是 UPDATE 的意思 (因為 existingDetail 已經有流水號 id 了)
				orderCartDetailsDao.save(existingDetail);

			} else {
//				 情況 B：購物車裡還沒有，這是一筆全新的購物車明細，我們要建一筆新的 OrderCartDetails！
				OrderCartDetails newDetail = new OrderCartDetails();
				newDetail.setOrderCartId(currentCartId);
				newDetail.setProductId(req.getProductId());
//				 🚨 【大腦呼叫倉庫】去 Products 表查一下這個商品到底多少錢？
//				 思云有協助修改 -- 有問題問
				Products product = productsDao.findById(req.getProductId());
//				  商品不存在或已下架，拋例外讓前端知道
				if (product == null || !product.isActive()) {
					throw new RuntimeException("商品 ID " + req.getProductId() + " 不存在或已下架");
				}

				newDetail.setPrice(product.getBasePrice());
				newDetail.setQuantity(req.getQuantity());
				newDetail.setGift(false); // 客人選的，絕對不是贈品

//				 存進資料庫做 INSERT
				orderCartDetailsDao.save(newDetail);
			}
		}

//		 步驟 3：重新計算金額、滿額贈、包成 Res 回傳給畫面
		return getCartView(currentCartId, req.getMemberId());
	}

	/**
	 * 核心 API 2:刪除購物車內的特定商品
	 */
	@Transactional
	public CartViewRes removeItem(CartRemoveReq req) {
//		 1. 直接呼叫剛剛寫好的刪除 SQL
		orderCartDetailsDao.deleteByCartIdAndProductId(req.getCartId(), req.getProductId());

//		 2. 刪完之後，重新結算整台車的金額並回傳！ (預設沒有會員資訊就傳 null)
		return getCartView(req.getCartId(), req.getMemberId());
	}
//	 API 3：還未完成
//     API 3：使用者選擇贈品（Methods A：先存後下單）
//     selectedGiftProductId = null 或 0 → 選「不要贈品」
//     selectedGiftProductId > 0            → 選了某個贈品

	@Transactional
	public CartViewRes selectGift(CartSelectGiftReq req) {
//	         步驟 1：先把舊贈品全部清掉（避免一個人有兩份贈品）
		orderCartDetailsDao.deleteAllGiftsByCartId(req.getCartId());
		Integer selectedId = req.getSelectedGiftProductId();
		if (selectedId != null && selectedId > 0) {
//	             使用者選了一個實際的贈品，進行一連串驗證
//	             2-1. 重算小計，確認消費金額真的達標
			List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(req.getCartId());
			BigDecimal subtotal = BigDecimal.ZERO;
			for (OrderCartDetails d : allDetails) {
				if (!d.isGift()) { // 贈品 0 元，只加一般商品
					subtotal = subtotal.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
				}
			}
//	             2-2. 找這條贈品規則（從 promotions_gifts 查）
			PromotionsGifts giftRule = promotionsGiftsDao.findActiveRuleByGiftProductId(selectedId);
			if (giftRule == null) {
//	                 此贈品規則不存在或已下架
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_NOT_AVAILABLE.getCode());
				err.setMessage(ReplyMessage.GIFT_NOT_AVAILABLE.getMessage());
				return err;
			}
//	             2-3. 確認消費額有達到門檻（subtotal >= fullAmount）
			if (subtotal.compareTo(giftRule.getFullAmount()) < 0) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.NOT_REACH_FULLAMOUNT.getCode());
				err.setMessage(ReplyMessage.NOT_REACH_FULLAMOUNT.getMessage());
				return err;
			}
//	             2-4. 確認贈品庫存（-1=無限量，0=已送完）
			if (giftRule.getQuantity() == 0) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_SEND_LIGHT.getCode());
				err.setMessage(ReplyMessage.GIFT_SEND_LIGHT.getMessage());
				return err;
			}
			/*
			 * 2-5. 確認贈品商品本身上架中,因為贈品頁是商品，可能不選贈品，所以會是Null, 用內建的方法重寫了findById方法
			 * orElse(null)它的意思就是「如果盒子裡有商品就拿出來， 如果沒查到任何東西（或你傳了 Null 給它）就回傳 null
			 */

			Products giftProduct = productsDao.findById(selectedId).orElse(null);
			if (giftProduct == null || !giftProduct.isActive()) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_NOT_AVAILABLE.getCode());
				err.setMessage(ReplyMessage.GIFT_NOT_AVAILABLE.getMessage());
				return err;
			}
//	             2-6. 全部通過！把贈品寫進購物車明細
			OrderCartDetails giftDetail = new OrderCartDetails();
			giftDetail.setOrderCartId(req.getCartId()); // 關聯購物車
			giftDetail.setProductId(selectedId); // 贈品的商品 ID
			giftDetail.setPrice(BigDecimal.ZERO); // 贈品 0 元
			giftDetail.setQuantity(1); // 固定送 1 份
			giftDetail.setGift(true); // 標記為贈品
			giftDetail.setDiscountNote("滿額贈"); // 說明來源
			orderCartDetailsDao.save(giftDetail); // INSERT 進資料庫
		}
//	         selectedId == null 或 == 0：使用者選「不要贈品」
//	         步驟 1 已刪掉舊贈品，這裡直接重新結算即可
		return getCartView(req.getCartId(), req.getMemberId());
	}

//	     API 4：清空購物車（一般商品 + 贈品全刪）

	@Transactional
	public CartViewRes clearCart(CartClearReq req) {
//	         刪除這台購物車的所有明細（一般商品 + 贈品全部清空）
		orderCartDetailsDao.deleteAllByCartId(req.getCartId());
//	         回傳空的購物車（items=空清單、subtotal=0、total=0）
		return getCartView(req.getCartId(), req.getMemberId());
	}

	/**
	 * api5：查看購物車內容 (例如前端一進到購物車頁面要拉取資料)
	 */
	public CartViewRes viewCart(int cartId, int memberId) {
//		 什麼都不用改，直接結算現有的東西！
		return getCartView(cartId, memberId);
	}

//	 --------------【共用方法】還需要修改

	@Transactional
	public CartViewRes getCartView(int cartId, int memberId) {
		CartViewRes res = new CartViewRes();
		res.setCartId(cartId);
		List<String> warningMessages = new ArrayList<>(); // 警告訊息（調價/下架）
		List<CartItemVO> voList = new ArrayList<>(); // 商品明細清單
		BigDecimal subtotal = BigDecimal.ZERO; // 稅前小計，從 0 開始
//	         ── 步驟 1：確認購物車存在 ──
		OrderCart cart = orderCartDao.findById(cartId);
		if (cart == null) {
			res.setCode(ReplyMessage.CART_NOT_FOUND.getCode());
			res.setMessage(ReplyMessage.CART_NOT_FOUND.getMessage());
			return res; // 直接返回，後面不用繼續
		}
//	         ── 步驟 2：逐一驗算每筆「非贈品」明細 ──
		List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(cartId);
		for (OrderCartDetails detail : allDetails) {
			if (detail.isGift())
				continue; // 贈品留到步驟 3 處理
			Products product = productsDao.findById(detail.getProductId());
			CartItemVO vo = new CartItemVO();
			vo.setDetailId(detail.getId());
			vo.setProductId(detail.getProductId());
			vo.setQuantity(detail.getQuantity());
			vo.setGift(false);
			vo.setDiscountNote(detail.getDiscountNote());
			if (product == null || !product.isActive()) {
//	                 商品已下架或不存在：警告，lineTotal 設 0，不計入小計
				String name = (product != null) ? product.getName() : "商品 #" + detail.getProductId();
				vo.setProductName(name + "（已下架）");
				vo.setPrice(detail.getPrice()); // 顯示最後快照的價格
				vo.setLineTotal(BigDecimal.ZERO); // 不納入小計
				warningMessages.add("「" + name + "」已下架或不存在，請將其移除");
			} else {
				vo.setProductName(product.getName());
//	                 偵測到調價：警告 + 更新資料庫快照
				if (detail.getPrice().compareTo(product.getBasePrice()) != 0) {
					warningMessages.add("「" + product.getName() + "」的價格已從 $" + detail.getPrice() + " 調整為 $"
							+ product.getBasePrice());
					detail.setPrice(product.getBasePrice()); // 更新快照
					orderCartDetailsDao.save(detail); // 寫回 DB
				}
				BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())); // 單價 × 數量
				vo.setPrice(detail.getPrice());
				vo.setLineTotal(lineTotal);
				subtotal = subtotal.add(lineTotal); // 累加小計
			}
			voList.add(vo);
		}
//	         ── 步驟 3：重新驗證使用者已選的贈品是否仍然有效 ──
//	      先抓出這台購物車裡已經選擇的贈品
		List<OrderCartDetails> existingGifts = orderCartDetailsDao.findSelectGiftsByCartId(cartId);
		if (!existingGifts.isEmpty()) {
			OrderCartDetails existingGift = existingGifts.get(0); // 購物車只會有一個贈品
			Products giftProduct = productsDao.findById(existingGift.getProductId());
			PromotionsGifts giftRule = promotionsGiftsDao.findActiveRuleByGiftProductId(existingGift.getProductId());
//	             判斷贈品是否「依然有效」（4 個條件全部滿足）：
			boolean giftStillValid = giftRule != null // 規則存在且上架
					&& giftProduct != null && giftProduct.isActive() // 贈品商品上架
					&& (giftRule.getQuantity() == -1 || giftRule.getQuantity() > 0) // 還有庫存
					&& subtotal.compareTo(giftRule.getFullAmount()) >= 0; // 金額達標
			if (giftStillValid) {
//	                 有效：把贈品 VO 加入清單顯示在購物車界面
				CartItemVO giftVO = new CartItemVO();
				giftVO.setDetailId(existingGift.getId());
				giftVO.setProductId(existingGift.getProductId());
				giftVO.setProductName(giftProduct.getName()); // 展示贈品名稱（如「大盤雞」）
				giftVO.setQuantity(1);
				giftVO.setPrice(BigDecimal.ZERO);
				giftVO.setLineTotal(BigDecimal.ZERO);
				giftVO.setGift(true);
				giftVO.setDiscountNote("滿額贈");
				voList.add(giftVO);
			} else {
//	                 失效：自動刪除，並告知前端
				orderCartDetailsDao.deleteAllGiftsByCartId(cartId);
				warningMessages.add("贈品資格已失效（消費金額不足或贈品已下架），請重新選擇");
			}
		}
//	         ── 步驟 4：建立「可選贈品下拉清單」 ──
		List<AvailableGiftVO> availableGifts = new ArrayList<>();
		List<PromotionsGifts> activeGiftRules = promotionsGiftsDao.findAllActiveGifts();
		for (PromotionsGifts rule : activeGiftRules) {
			if (subtotal.compareTo(rule.getFullAmount()) >= 0) {
//	                 消費金額達到此規則的門檻，才加入可選清單
				AvailableGiftVO option = new AvailableGiftVO();
				option.setGiftRuleId(rule.getId());
				option.setGiftProductId(rule.getGiftProductId());
				option.setFullAmount(rule.getFullAmount());
				Products giftProd = productsDao.findById(rule.getGiftProductId());
				if (giftProd == null || !giftProd.isActive()) {
//	                     贈品商品已下架，前端顯示為灰色不可點
					String gName = (giftProd != null) ? giftProd.getName() : "贈品 #" + rule.getGiftProductId();
					option.setGiftProductName(gName);
					option.setAvailable(false);
					option.setUnavailableReason("「" + gName + "」已下架");
				} else if (rule.getQuantity() == 0) {
//	                     贈品已送完，前端顯示為灰色不可點
					option.setGiftProductName(giftProd.getName());
					option.setAvailable(false);
					option.setUnavailableReason("「" + giftProd.getName() + "」贈品已送完");
				} else {
//	                     可以選
					option.setGiftProductName(giftProd.getName());
					option.setAvailable(true);
					option.setUnavailableReason(null);
				}
				availableGifts.add(option);
			}
		}
//	         ── 步驟 5：查稅務設定並計算稅額 ──
		TaxInfoVO taxInfo = new TaxInfoVO();
		BigDecimal totalAmount = subtotal; // 預設（無稅設定時）：總計 = 小計
		GlobalArea area = globalAreaDao.findById(cart.getGlobalAreaId());
		if (area != null) {
			Regions region = regionsDao.findByCountry(area.getCountry());
			if (region != null) {
				taxInfo.setTaxRate(region.getTaxRate()); // 儲存稅率
				taxInfo.setTaxType(region.getTaxType().name()); // 儲存稅型別字串
				BigDecimal taxAmount;
				if (region.getTaxType() == TaxType.EXCLUSIVE) {
//	                     外加稅：稅額 = 小計 × 稅率，總計 = 小計 + 稅額
					taxAmount = subtotal.multiply(region.getTaxRate()).setScale(2, RoundingMode.HALF_UP);
					totalAmount = subtotal.add(taxAmount);
				} else {
//	                     內含稅：稅額從含稅金額反推 = 小計 × 稅率 ÷ (1 + 稅率)
					taxAmount = subtotal.multiply(region.getTaxRate()).divide(BigDecimal.ONE.add(region.getTaxRate()),
							2, RoundingMode.HALF_UP);
					totalAmount = subtotal; // 內含稅：總計就是小計，不另加稅
				}
				taxInfo.setTaxAmount(taxAmount);
			}
		}
//	         ── 步驟 6：打包所有結果回傳 ──
		res.setItems(voList);
		res.setSubtotal(subtotal);
		res.setAvailableGifts(availableGifts); // 可選贈品下拉清單
		res.setTaxInfo(taxInfo); // 稅務資訊
		res.setTotalAmount(totalAmount); // 最終總計
		res.setWarningMessages(warningMessages); // 警告訊息（空清單代表一切正常）
		res.setCode(ReplyMessage.SUCCESS.getCode());
		res.setMessage(ReplyMessage.SUCCESS.getMessage());
		return res;
	}
}
