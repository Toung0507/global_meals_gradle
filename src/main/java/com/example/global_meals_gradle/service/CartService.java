package com.example.global_meals_gradle.service;

import com.example.global_meals_gradle.dao.*;
import com.example.global_meals_gradle.entity.*;
import com.example.global_meals_gradle.req.*;
import com.example.global_meals_gradle.res.*;
import com.example.global_meals_gradle.vo.*;
import com.example.global_meals_gradle.constants.OperationType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Optional;

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
	@Autowired
	private MembersDao membersDao;

//	【核心 API 1:同步購物車，包括刪除單品】
	@Transactional
	public CartViewRes syncItem(CartSyncReq req) {

		int currentCartId;

		// 步驟 1：判斷這是第一件商品（要建新車），還是後面加的商品（沿用舊車）
		if (req.getCartId() == null) {

			// 劇本分支 A：建新車
//        	A-1.準備一個裝新購物車的空殼子
			OrderCart newCart = new OrderCart();
//        	A-2:塞東西進去
			newCart.setGlobalAreaId(req.getGlobalAreaId());
			newCart.setOperationType(OperationType.valueOf(req.getOperationType()));

			// 決定「操作者」的欄位要存誰
			if ("STAFF".equals(req.getOperationType())) {
				newCart.setOperation(req.getStaffId()); // 員工點餐存 staffId
			} else {
				newCart.setOperation(req.getMemberId() != null ? req.getMemberId() : 1); // 顧客點餐存 memberId 或 1
			}

			// 這裡必須用 Spring Data JPA 內建的 save(),「新建主表」時我們強烈建議用 save()！
			// 因為 save() 執行完，資料庫自動生成的 ID 會立刻被裝回 newCart.getId() 裡面。
			// 我們馬上就能拿到剛建好的「7號」，提供給後面的商品明細使用。原生 SQL 很難做到這點！
//            A-3:存到資料庫去
			orderCartDao.save(newCart);
			// 把剛建好的newCart的Id值獲取到
			currentCartId = newCart.getId();

		} else {
			// 劇本分支 B：已經有車了
			currentCartId = req.getCartId();
		}

		// 步驟 2：處理這筆商品 (新增、更新數量、或直接刪除)
		// 這時候我們有了 currentCartId (購物車號碼)，以及 req 傳來的 productId 和 quantity

		if (req.getQuantity() == 0) {
			// ==== 情境 2-1：刪除商品 ====
			orderCartDetailsDao.deleteByCartIdAndProductId(currentCartId, req.getProductId());

		} else {
			// ==== 情境 2-2：數量大於 0，加入新商品 或 修改數量 ====
			// 先找找看，這台車裡面是不是已經有這個商品了
			OrderCartDetails existingDetail = orderCartDetailsDao.findByCartIdAndProductId(currentCartId,
					req.getProductId());

			if (existingDetail != null) {
				// 情況 A：購物車裡已經有了，單純把數量「覆蓋」成前端傳來的新數量
				existingDetail.setQuantity(req.getQuantity());

				// 再次呼叫 save()！在此它是 UPDATE 的意思 (因為 existingDetail 已經有流水號 id 了)
				orderCartDetailsDao.save(existingDetail);

			} else {
				// 情況 B：購物車裡還沒有，這是一筆全新的明細，我們要建一筆新的 OrderCartDetails！

				// 🚨 【大腦呼叫倉庫】去 Products 表查一下這個商品到底多少錢？

				// 思云有協助修改 -- 有問題問
				Optional<Products> product = productsDao.findById(req.getProductId());

				// 回傳的可能是 null，所以大腦要自己做安全檢查
				// 檢查包裹是否為空 (Optional 不能直接跟 null 比較，要用 .isPresent() 或 .isEmpty())
				if (product.isEmpty()) {
					throw new RuntimeException("找不到商品 ID: " + req.getProductId());
				}

				OrderCartDetails newDetail = new OrderCartDetails();
				newDetail.setOrderCartId(currentCartId);
				newDetail.setProductId(req.getProductId());
				newDetail.setPrice(product.get().getBasePrice()); // 需要多一個 .get() 才能將資料取出
				newDetail.setGift(false); // 客人選的，絕對不是贈品

				// 存進資料庫做 INSERT
				orderCartDetailsDao.save(newDetail);
			}
		}

		// 💡 步驟 3：重新計算金額、滿額贈、包成 Res 回傳給畫面
		return getCartView(currentCartId, req.getMemberId());
	}

	/**
	 * 【核心 API 2】刪除購物車內的特定商品
	 */
	@Transactional
	public CartViewRes removeItem(CartRemoveReq req) {
		// 1. 直接呼叫剛剛寫好的刪除 SQL
		orderCartDetailsDao.deleteByCartIdAndProductId(req.getCartId(), req.getProductId());

		// 2. 刪完之後，重新結算整台車的金額並回傳！ (預設沒有會員資訊就傳 null)
		return getCartView(req.getCartId(), req.getMemberId());
	}

	/**
	 * 【核心 API 3】切換是否使用折價券 (對應 CartCouponReq)
	 */
	@Transactional
	public CartViewRes applyCoupon(CartCouponReq req) {
		// 我們先取得目前的購物車結算結果
		CartViewRes currentCart = getCartView(req.getCartId(), req.getMemberId());

		// 如果客人打 API 說「我要用折價券 (true)」，且他真的有特權 (hasCoupon == true)
		if (req.isUseCoupon() && currentCart.isHasCoupon()) {
			// 試算打折後的最終金額 (例如：小計 * 0.95):
			BigDecimal discountRate = new BigDecimal("0.95"); // 95折
			BigDecimal discountedTotal = currentCart.getSubtotal().multiply(discountRate);

			currentCart.setDiscountedTotal(discountedTotal); // 把綠色的優惠價展示給前端
		} else {
			currentCart.setDiscountedTotal(null); // 取消使用折價券，恢復原價
		}

		return currentCart;
	}

	/**
	 * [api4】單純查看購物車內容 (例如前端一進到購物車頁面要拉取資料)
	 */
	public CartViewRes viewCart(int cartId, Integer memberId) {
		// 什麼都不用改，直接結算現有的東西！
		return getCartView(cartId, memberId);
	}

	// --------------【共用方法】還需要修改

	/**
	 * 【共用方法】計算一台車的總金額、處理滿額贈、並包裝成 CartViewRes 回傳
	 */
	public CartViewRes getCartView(int cartId, Integer memberId) {
		// 1. 準備一個「裝回應結果」的空盒子res
		CartViewRes res = new CartViewRes();
		res.setCartId(cartId);
		// 2. 準備一個「裝裝明細 VO」的空清單
		List<CartItemVO> voList = new ArrayList<>();
		// 3. 準備一個裝「小計」的變數，從 0 元開始算
		BigDecimal subtotal = BigDecimal.ZERO;
//    	    4. 大腦呼叫倉庫！去 OrderCartDetailsDao 查出這台車所有的明細 
		List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(cartId);

		// 5. 用一個 for 迴圈，把每一筆明細拿出來結算：
		for (OrderCartDetails detail : allDetails) {
//    	    （A）：我們要把它轉換成要回傳給前端的 CartItemVO
//    	    	 -1:裝明細 VO
			CartItemVO vo = new CartItemVO();
			vo.setDetailId(detail.getId());
			vo.setProductId(detail.getProductId());
			vo.setPrice(detail.getPrice());
			vo.setQuantity(detail.getQuantity());
			vo.setGift(detail.isGift());
			vo.setDiscountNote(detail.getDiscountNote());
			// (B) 🚨 大腦再次呼叫倉庫！去 Products 表查出它的名字 (為了放到 vo.setProductName)
			// 思云已改，有問題詢問
			Optional<Products> p = productsDao.findById(detail.getProductId());
			if (p.isPresent()) {
				vo.setProductName(p.get().getName()); // 假設你的實體欄位叫做 name
			}
			// (C) 單品小計 = 單價 × 數量 (必須使用 BigDecimal.valueOf 把 int 數量轉成物件才能相乘)
			BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
			vo.setLineTotal(lineTotal);
			// (D) 把這個 vo 放進我們第二步準備好的清單 voList 中
			voList.add(vo);

			/*
			 * 在 BigDecimal 的世界裡，所有數學符號都要換成英文單字：
			 * 
			 * 加法 (+) 變成 .add() 減法 (-) 變成 .subtract() 乘法 (*) 變成 .multiply() 除法 (/) 變成
			 * .divide()
			 */
			// (E) 結算總金額 (subtotal)：
			// 💡 只有當這個商品「不是贈品 (!detail.isGift())」時，我們才把它加進小計裡面！
			if (!detail.isGift()) {
				subtotal = subtotal.add(lineTotal);
			}
		}

//    	     ————————————————————————重新計算滿額贈：這塊還要修改
		// 重新計算滿額贈：
		// - 1. 先把購物車裡原本的「系統舊贈品」全部殺光
		orderCartDetailsDao.deleteAllGiftsByCartId(cartId);
		// -2. 去滿額贈活動表，把「正在舉辦中」且「金額由大到小排序」的所有贈品規則撈出來
		// 此區的 JPA 寫法 是錯誤請修正
		List<PromotionsGifts> activeGifts = promotionsGiftsDao.findAllActiveGiftsOrdered();

		// -3. 從門檻最高的贈品開始檢查 (因為前面有用 findAllActiveGiftsOrdered() 幫由大到小排序了！)
		for (PromotionsGifts giftRule : activeGifts) {

			// 在 BigDecimal 的世界，比大小要用 compareTo：
			// 回傳 1 表示前面比較大, 回傳 0 表示一樣大, 回傳 -1 表示前面比較小
			// 所以 >= 0，就代表「小計有大於或等於滿額贈的門檻金額」！
			if (subtotal.compareTo(giftRule.getFullAmount()) >= 0) {

				// 恭喜達標！我們把這個新贈品塞進他的購物車裡
				OrderCartDetails giftDetail = new OrderCartDetails();
				giftDetail.setOrderCartId(cartId);
				giftDetail.setProductId(giftRule.getGiftProductId());
				giftDetail.setPrice(BigDecimal.ZERO); // 贈品當然是 0 元
				giftDetail.setQuantity(1); // 預設送一份
				giftDetail.setGift(true); // 標記這是一個贈品！
				giftDetail.setDiscountNote("滿額驚喜贈"); // 告訴前端這玩意哪來的

				// 這裡我們又用到了 save() 內建方法！它會做 INSERT 並且自動生出 ID
				orderCartDetailsDao.save(giftDetail);

//                     但我們在塞進資料庫的同時，也要記得把它包裝成給前端看的 VO
				CartItemVO giftVo = new CartItemVO();
				giftVo.setDetailId(giftDetail.getId()); // 注意：這就是你要用 save() 的原因，有 id！
				giftVo.setProductId(giftDetail.getProductId());
				giftVo.setPrice(giftDetail.getPrice());
				giftVo.setQuantity(giftDetail.getQuantity());
				giftVo.setGift(giftDetail.isGift());
				giftVo.setDiscountNote(giftDetail.getDiscountNote());
				giftVo.setLineTotal(BigDecimal.ZERO); // 因為0元，小計也是0

				// 去查一下贈品的名字，讓前端可以顯示 (假設你實體叫做 getName() )
				Products giftProduct = productsDao.findById(giftDetail.getProductId());
				if (giftProduct != null) {
					giftVo.setProductName(giftProduct.getName());
				}

				// 把贈品正式裝進我們要回傳給前端的箱子
				voList.add(giftVo);

				// 最關鍵的一步：只送能達到最高門檻的那一個贈品，所以送完就可以直接「中斷迴圈」跳出去了
				break;
			}
		}

		// ==========================================
		// 最後一步：判斷客人是否擁有折價券
		// ==========================================

		boolean hasCoupon = false; // 預設大家一開始都沒有券

		// 1. 判斷他到底是不是會員？(你前面已經定好：1號是訪客)
		if (memberId != null && memberId > 1) {

			// 2. 去 Members 表查這個會員的資料
			// 思云修正
			Optional<Members> member = membersDao.findById(memberId);

			// 3. 判斷他身上有沒有我們發的滿 10 次折價券？
			if (member.isPresent()) {
				if (member.get().isDiscount()) {
					hasCoupon = true; // 恭喜！他有券！
				}
			}
		}

		// 4. 把判斷結果和清單小計通通放進回應結果盒子裡
		res.setItems(voList);
		res.setSubtotal(subtotal);
		res.setHasCoupon(hasCoupon);
		res.setDiscountedTotal(null); // 這個金額只有當他按下「我要用折價券」時才會算出數字，目前是 null 不影響

		return res;
	}
}
