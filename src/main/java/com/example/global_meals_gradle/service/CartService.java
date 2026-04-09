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
	@Autowired
	private GlobalAreaDao globalAreaDao;
	@Autowired
	private RegionsDao regionsDao;
	@Autowired
	private PromotionsDao promotionsDao; // 用來撈上架中且在有效期間內的活動清單

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

//     API 3：使用者選擇贈品
//     前端傳來 giftRuleId（promotions_gifts 主鍵），後端精準定位贈品規則
//     若使用者直接點「確認下單」而未選贈品，前端不會呼叫此 API
	@Transactional
	public CartViewRes selectGift(CartSelectGiftReq req) {

//		步驟1：先把購物車裡舊的贈品全部清掉（確保購物車只有一個贈品）
		orderCartDetailsDao.deleteAllGiftsByCartId(req.getCartId());

//		步驟2-0：取得前端傳來的贈品規則 ID
		int giftRuleId = req.getGiftRuleId();

//		giftRuleId > 0 才代表使用者選了贈品（int 型別缺少時預設是 0）
		if (giftRuleId > 0) {

//			步驟2-1：用 giftRuleId 主鍵精準查詢這條贈品規則
//			findActiveRuleByGiftRuleId 會同時 JOIN promotions 表
//			確保：規則上架 + 對應活動上架且在有效期間內，否則回傳 null
			PromotionsGifts giftRule = promotionsGiftsDao.findActiveRuleByGiftRuleId(giftRuleId);

//			步驟2-2：確認這條規則存在且有效
			if (giftRule == null) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_NOT_AVAILABLE.getCode());
				err.setMessage(ReplyMessage.GIFT_NOT_AVAILABLE.getMessage());
				return err;
			}

//			步驟2-3：從規則物件直接取得贈品的商品 ID（不依賴前端傳值，從資料庫取更安全）
			int giftProductId = giftRule.getGiftProductId();

//			步驟2-4：重新計算購物車小計，確認消費額仍然達標
			List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(req.getCartId());
			BigDecimal subtotal = BigDecimal.ZERO;
			for (OrderCartDetails d : allDetails) {
				if (!d.isGift()) {
					subtotal = subtotal.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
				}
			}

//			步驟2-5：確認消費額達到這條規則的門檻
			if (subtotal.compareTo(giftRule.getFullAmount()) < 0) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.NOT_REACH_FULLAMOUNT.getCode());
				err.setMessage(ReplyMessage.NOT_REACH_FULLAMOUNT.getMessage());
				return err;
			}

//			步驟2-6：確認贈品行銷名額還夠（-1=無限量，0=已送完，>0=還有名額）
			if (giftRule.getQuantity() == 0) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_SEND_LIGHT.getCode());
				err.setMessage(ReplyMessage.GIFT_SEND_LIGHT.getMessage());
				return err;
			}

//			步驟2-7：確認這個贈品商品在商品表裡還是上架的
//			用 giftProductId（從規則物件取的），不是從前端取的
			Products giftProduct = productsDao.findById(giftProductId);
			if (giftProduct == null || !giftProduct.isActive()) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_NOT_AVAILABLE.getCode());
				err.setMessage(ReplyMessage.GIFT_NOT_AVAILABLE.getMessage());
				return err;
			}

//			步驟2-8：全部驗證通過！把贈品寫進購物車明細
			OrderCartDetails giftDetail = new OrderCartDetails();
			giftDetail.setOrderCartId(req.getCartId()); // 關聯購物車
			giftDetail.setProductId(giftProductId);      // 贈品商品 ID（從規則取）
			giftDetail.setPrice(BigDecimal.ZERO);         // 贈品 0 元
			giftDetail.setQuantity(1);                    // 固定送 1 份
			giftDetail.setGift(true);                     // 標記為贈品
			giftDetail.setDiscountNote("滿額贈");          // 說明來源
			orderCartDetailsDao.save(giftDetail);         // INSERT 進資料庫
		}

//		步驟3：重新結算整台購物車並回傳
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

	/*
	 * 這個方法其實就類比結算機器，其他幾個api就是各自功能的服務生，最後一步都是到結算機器的這些功能 結算機器：檢查價格、重新驗證贈品、加稅金、打包成漂亮的
	 * CartViewRes）。 1.客人在首頁，點擊右上角的「🛒 購物車圖示」，滑出購物車側邊欄的那一瞬間。
	 * 2.或者客人點擊「前往結帳」，進入確認訂單頁面的那一瞬間。
	 * 做的6件事：1.先確認這個客人有沒有座位（購物車存在嗎）2.把桌上每道菜的現在價格都仔細核對一遍
	 * 3.看看他的免費贈品資格還有效嗎4.列出他還可以選哪些免費贈品5.加上這個地區的稅6.把完整的帳單列印出來給客人看
	 */
	@Transactional
	public CartViewRes getCartView(int cartId, int memberId) {
		CartViewRes res = new CartViewRes();
		res.setCartId(cartId);
		List<String> warningMessages = new ArrayList<>(); // 警告訊息（調價/下架）
		List<CartItemVO> voList = new ArrayList<>(); // 商品明細清單
		BigDecimal subtotal = BigDecimal.ZERO; // 稅前小計，從 0 開始

//	         ── 步驟 1：檢查1：確認傳進來的cartId對應的購物車存在嗎？──
		OrderCart cart = orderCartDao.findById(cartId);
		if (cart == null) {
			res.setCode(ReplyMessage.CART_NOT_FOUND.getCode());
			res.setMessage(ReplyMessage.CART_NOT_FOUND.getMessage());
			return res; // 直接返回，後面不用繼續
		}
//	         ── 步驟 2：檢查2：逐一驗算每筆「非贈品」的狀態與金額並組裝成商品VO、最後累加小計 ─
//		 ①：先撈出這台車的每一個購物車明細進行組裝成商品VO（購物計車明細是商品+贈品的）
//		這是最大的 for 迴圈，要做 3 件事：（A)檢查狀態（B）檢查金額：比對定價(C) 組裝 成商品VO、(D) 累加小計
		List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(cartId);
		for (OrderCartDetails detail : allDetails) {
			if (detail.isGift())
				continue; // 贈品留到步驟 3 處理
			/*
			 * ②先去查這個商品目前的狀態.分成-1.存在沒有下架（裡面又分成價格有變->需要更新orderCartDetails資料庫快照
			 * 、和價格和商品表的價格一樣）-2.下架或者不存在，為了 若是情況-2。商品VO倆面的
			 * setProductName會顯示XX已下架、setLineTotal會顯示0、商品單價就是加入購物車時的快照價、
			 * 然後加入警告warningMessages提示商品已下架或不存在
			 */
			Products product = productsDao.findById(detail.getProductId());
			CartItemVO vo = new CartItemVO();
//			（A）檢查狀態：
//			-2.如果商品已下架或不存在：
			if (product == null || !product.isActive()) {
//	           如果商品物件還在只是被下架了，就抓它的原名。
//             如果商品被刪除了，就抓 detail（訂單明細快照）裡的 productId 來當替代名稱。
				String name = (product != null) ? product.getName() : "商品 #" + detail.getProductId();
				vo.setProductName(name + "（已下架）");
				vo.setPrice(detail.getPrice()); // 顯示最後快照的價格
				vo.setLineTotal(BigDecimal.ZERO); // 不納入小計
				warningMessages.add("「" + name + "」已下架或不存在，請將其移除");
			} else {
//				-1：
				vo.setProductName(product.getName());
//	              (B)檢查金額： 比對定價： 偵測到調價：警告 + 更新orderCartDetails資料庫快照
				if (detail.getPrice().compareTo(product.getBasePrice()) != 0) {
					warningMessages.add("「" + product.getName() + "」的價格已從 $" + detail.getPrice() + " 調整為 $"
							+ product.getBasePrice());
					detail.setPrice(product.getBasePrice()); // 更新快照
					orderCartDetailsDao.save(detail); // 更新orderCartDetails資料庫快照
				}
//				 ③(C) ：組裝一個 商品VO（給前端看的商品卡片）
				vo.setDetailId(detail.getId());
				vo.setProductId(detail.getProductId());
				vo.setQuantity(detail.getQuantity());
				vo.setGift(false);
				vo.setDiscountNote(detail.getDiscountNote());
				BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())); // 單價 × 數量
				vo.setPrice(detail.getPrice());
				vo.setLineTotal(lineTotal);
//				④（D）累加小計
				subtotal = subtotal.add(lineTotal);
			}
			voList.add(vo);
		}

		/*
		 * 步驟 3：重新驗證使用者“已選的贈品”是否仍然有效 :4 個條件全部滿足，贈品才有效： ① 贈品規則存在、② 贈品商品上架、③ 行銷庫存充足、④
		 * 金額仍然達標
		 */
//	      先抓出這台購物車裡是否已經有已經選好的贈品了：如果有選好的看是否還有效，還沒有選好贈品的情況就創建贈品列表
		List<OrderCartDetails> existingGifts = orderCartDetailsDao.findSelectGiftsByCartId(cartId);
		if (!existingGifts.isEmpty()) {
			OrderCartDetails existingGift = existingGifts.get(0); // 購物車只會有一個贈品
			Products giftProduct = productsDao.findById(existingGift.getProductId());
//			根據購物車詳情裡的選中的贈品gift，獲取gift的ProductId，通過ProductId尋找商品表的這個贈品的狀態還有贈品表裡這個贈品對應的上架的活動
			PromotionsGifts giftRule = promotionsGiftsDao.findActiveRuleByGiftProductId(existingGift.getProductId());
//	             判斷贈品是否「依然有效」（4 個條件全部滿足）：
			boolean giftStillValid = giftRule != null // 規則存在（上架上面findActiveRuleByGiftProductId已經判斷）
					&& giftProduct != null && giftProduct.isActive() // 贈品作為商品狀態：存在並上架才醒
					&& (giftRule.getQuantity() == -1 || giftRule.getQuantity() > 0) // 還有庫存
					&& subtotal.compareTo(giftRule.getFullAmount()) >= 0; // 金額達標
			if (giftStillValid) {
//	                 有效：把贈品 VO 加入CartItemVO的清單voList中顯示在購物車界面
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
//	                 失效：自動刪除全部贈品，並告知前端
				orderCartDetailsDao.deleteAllGiftsByCartId(cartId);
				warningMessages.add("贈品資格已失效（消費金額不足或贈品已下架），請重新選擇");
			}
		}
	//  ── 步驟 4：建立「以活動為單位」的兩層可選贈品清單 ──

		// 外層清單：裝「使用者有資格參加的活動」
		// 最後這個清單會被設定進 res.setAvailablePromotions()
		// 空清單代表消費未達任何活動門檻，前端不顯示「選擇活動」按鈕
		List<AvailablePromotionVO> availablePromotions = new ArrayList<>();

		// 去資料庫撈出所有目前上架且在有效時間範圍內的活動（完整物件，含 id 和 name）
		List<Promotions> activePromotions = promotionsDao.findActivePromotions();

		// 逐一審查每個活動，看使用者的消費有沒有資格進入
		for (Promotions promotion : activePromotions) {

		    // 撈出「這個活動」底下所有上架的贈品規則
		    // promotions_id = promotion.getId() → 是後端自己迴圈取的，跟前端無關
		    List<PromotionsGifts> giftsInThisPromotion =
		            promotionsGiftsDao.findGiftsByPromotionId(promotion.getId());

		    // 如果這個活動底下沒有任何贈品規則（活動建了但沒設贈品），直接跳過
		    // 前端：這個活動完全不出現在下拉選單裡
		    if (giftsInThisPromotion.isEmpty()) {
		        continue; // 跳過，進入 for 迴圈下一輪
		    }

		    // 找出這個活動底下「門檻最低的那條規則的金額」
		    // 目的：使用者消費只要達到最低門檻，就算「有資格進入這個活動」
		    // 進入後，活動底下的每個贈品會再各自判斷使用者能不能選

		    // 先設為 null，代表還沒找到任何值
		    BigDecimal minFullAmount = null;

		    // 逐一比較每條贈品規則的門檻金額，找最小值
		    for (PromotionsGifts rule : giftsInThisPromotion) {
		        BigDecimal thisAmount = rule.getFullAmount(); // 這條規則的門檻金額
		        if (minFullAmount == null) {
		            // 第一次進來，還沒有比較對象，先把第一條的金額當暫時最小值
		            minFullAmount = thisAmount;
		        } else if (thisAmount.compareTo(minFullAmount) < 0) {
		            // compareTo 回傳負數 → thisAmount 比 minFullAmount 還要小
		            // → 發現更小的門檻，更新 minFullAmount
		            minFullAmount = thisAmount;
		        }
		    }

		    // 保險機制：理論上走到這裡 minFullAmount 不會是 null
		    // （因為上面 isEmpty() 已排除空清單）
		    // 但萬一發生，給 0 作為預設，防止下面 compareTo(null) 噴 NullPointerException
		    if (minFullAmount == null) {
		        minFullAmount = BigDecimal.ZERO;
		    }

		    // 判斷使用者消費小計有沒有達到這個活動的最低門檻
		    if (subtotal.compareTo(minFullAmount) < 0) {
		        // subtotal < minFullAmount → 消費不達標 → 跳過整個活動
		        // 前端：這個活動不出現在下拉選單（沒有「選擇活動」按鈕）
		        continue;
		    }

		    // ── 使用者達到這個活動的最低門檻！開始組裝這個活動底下的贈品清單 ──

		    // 內層清單：裝「這個活動底下每個贈品選項的狀況」
		    List<AvailableGiftVO> giftsVoList = new ArrayList<>();

		    // 逐一審查這個活動底下的每條贈品規則
		    for (PromotionsGifts rule : giftsInThisPromotion) {

		        // 確認使用者消費有沒有達到「這條贈品規則」自己的門檻
		        // 同一個活動可能有不同門檻：滿300送可樂、滿500才能選大盤雞
		        // 使用者消費400 → 能選可樂，但不能選大盤雞
		        if (subtotal.compareTo(rule.getFullAmount()) < 0) {
		            continue; // 這條規則的門檻消費者達不到，跳過這個贈品
		        }

		        // 消費達到這條規則！開始組裝這個贈品的 VO
		        AvailableGiftVO option = new AvailableGiftVO();
		        option.setGiftRuleId(rule.getId());                // promotions_gifts 主鍵
		        option.setGiftProductId(rule.getGiftProductId());  // 贈品的商品 ID
		        option.setFullAmount(rule.getFullAmount());         // 這條規則的消費門檻

		        // 去商品表查這個贈品商品的狀態（是否存在、是否上架）
		        // rule.getGiftProductId() 是 promotions_gifts.gift_product_id 的值
		        // 後端用它去 products 表拿完整的商品資訊（名稱、上架狀態等）
		        Products giftAsProd = productsDao.findById(rule.getGiftProductId());

		        if (giftAsProd == null || !giftAsProd.isActive()) {
		            // 情況1：贈品商品已下架或被刪除
		            // 前端：這個贈品顯示為灰色不可點，旁邊顯示原因
		            String gName = (giftAsProd != null)
		                    ? giftAsProd.getName()
		                    : "贈品 #" + rule.getGiftProductId(); // 被刪除了就用ID代替名字
		            option.setGiftProductName(gName);
		            option.setAvailable(false);                   // false → 前端灰色不可選
		            option.setUnavailableReason("「" + gName + "」已下架");

		        } else if (rule.getQuantity() == 0) {
		            // 情況2：這條規則的贈品行銷名額已送完
		            // quantity = 0 → 送完了（-1 = 無限量，> 0 = 還有名額）
		            // 前端：這個贈品顯示為灰色不可點，旁邊顯示已送完
		            option.setGiftProductName(giftAsProd.getName());
		            option.setAvailable(false);
		            option.setUnavailableReason("「" + giftAsProd.getName() + "」贈品已送完");

		        } else {
		            // 情況3：一切正常，這個贈品可以選
		            // 前端：正常顯示，使用者可以點選
		            option.setGiftProductName(giftAsProd.getName());
		            option.setAvailable(true);
		            option.setUnavailableReason(null); // null → 沒有問題
		        }

		        // 把這個贈品選項加入這個活動的贈品清單
		        giftsVoList.add(option);
		    }

		    // 組裝這個活動的外層 VO
		    AvailablePromotionVO promotionVO = new AvailablePromotionVO();
		    promotionVO.setPromotionId(promotion.getId());        // 活動 ID
		    promotionVO.setPromotionName(promotion.getName());    // 活動名稱（前端下拉顯示）
		    promotionVO.setFullAmount(minFullAmount);             // 這個活動的最低消費門檻
		    promotionVO.setGifts(giftsVoList);                   // 這個活動底下的所有贈品選項

		    // 把這個活動加入外層清單
		    availablePromotions.add(promotionVO);
		}

//	         ── 步驟 5：查稅務設定並計算稅額 ──
		TaxInfoVO taxInfo = new TaxInfoVO();
		BigDecimal totalAmount = subtotal; // 預設（無稅設定時/內含稅）：總計 = 小計
		GlobalArea area = globalAreaDao.findById(cart.getGlobalAreaId());
		if (area != null) {
			Regions region = regionsDao.findByCountry(area.getCountry());
			if (region != null) {
				taxInfo.setTaxRate(region.getTaxRate());
//				如果它原本是 TaxType.EXCLUSIVE 的enum物件。加上 .name() 後，它身上的一層皮就被剝下來，變成純粹的英文字串 "EXCLUSIVE"。
				taxInfo.setTaxType(region.getTaxType().name());
				BigDecimal taxAmount;
				if (region.getTaxType() == TaxType.EXCLUSIVE) {
//	                     外加稅：稅額 = 小計 × 稅率，總計 = 小計 + 稅額
					taxAmount = subtotal.multiply(region.getTaxRate()).setScale(2, RoundingMode.HALF_UP);
					totalAmount = subtotal.add(taxAmount);
				} else {
//	                     內含稅：“稅額”=從含稅金額反推 = 小計 × 稅率 ÷ (1 + 稅率)
					taxAmount = subtotal.multiply(region.getTaxRate()).divide(BigDecimal.ONE.add(region.getTaxRate()),
							2, RoundingMode.HALF_UP);
					totalAmount = subtotal; // 內含稅：總計就是小計，不另加稅
				}
				taxInfo.setTaxAmount(taxAmount);
			}
		}
//	         ── 步驟 6：打包所有結果回傳 ──
		/*
		 * voList就是cartItem（包括贈品）的list
		 * availablePromotions是 以活動為單位的兩層可選贈品清單
		 * subtotal是小計，taxInfo是稅務資訊，totalAmount是最終總計
		 * warningMessages警告訊息（空清單代表一切正常）
		 */

		res.setItems(voList);
		res.setSubtotal(subtotal);
		res.setAvailablePromotions(availablePromotions);
		res.setTaxInfo(taxInfo);
		res.setTotalAmount(totalAmount);
		res.setWarningMessages(warningMessages);
		res.setCode(ReplyMessage.SUCCESS.getCode());
		res.setMessage(ReplyMessage.SUCCESS.getMessage());
		return res;
	}
}
