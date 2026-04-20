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
import java.util.Optional;

@Service
public class CartService {

	@Autowired
	private OrderCartDao orderCartDao;
	@Autowired
	private OrdersDao ordersDao;
	@Autowired
	private OrderCartDetailsDao orderCartDetailsDao;

	@Autowired
	private ProductsDao productsDao;
	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;

	@Autowired
	private GlobalAreaDao globalAreaDao;
	@Autowired
	private RegionsDao regionsDao;
	@Autowired
	private PromotionsDao promotionsDao;
	@Autowired
	private BranchInventoryDao branchInventoryDao;

//	核心 API 1:同步購物車，包括刪除單品
//	 前端呼叫時機：使用者改了數量且「停手 1 秒後」（Debounce 防抖邏輯）
	@Transactional
	public CartViewRes syncItem(CartSyncReq req) {

		int currentCartId;

//		 步驟 1：判斷這是第一件商品（要建新車），還是後面加的商品（沿用舊車）
		if (req.getCartId() == null) {

//			 劇本分支 A：建新車

			// 🛡️ 修改六-1：globalAreaId 格式防呆（null 或 <= 0 直接擋回）
			// globalAreaId 是 Integer（物件型），可能為 null，必須先判斷 null 再判斷值
			if (req.getGlobalAreaId() == null || req.getGlobalAreaId() <= 0) {
				return buildError(ReplyMessage.GLOBAL_AREA_ID_ERROR); // code=400，格式錯誤
			}

			// 🛡️ 修改六-2：globalAreaId 實際存在於 DB 的驗證（格式對但分店不存在）
			// globalAreaDao.findById() 是自定義查詢（GlobalAreaDao），找不到回傳 null
			Optional<GlobalArea> targetArea = globalAreaDao.findById(req.getGlobalAreaId());
			// 用內建的dao方法判斷是不是空的
			if (targetArea.isEmpty()) {
				return buildError(ReplyMessage.GLOBAL_AREA_NOT_FOUND); // code=404，分店不存在
			}

			// 🛡️ 修改六-3：operationType 不可為 null 或空白字串
			if (req.getOperationType() == null || req.getOperationType().isBlank()) {
				return buildError(ReplyMessage.INVALID_OPERATION_TYPE); // code=400
			}

			// 🛡️ 修改六-4：operationType 字串安全轉 Enum
			// 原本：OperationType.valueOf(req.getOperationType()) 若傳 "abc" →
			// IllegalArgumentException（500）
			// 改法：try-catch 包住 valueOf，抓到就回傳友善的 400，不讓 500 暴露給前端
			OperationType opType; // 先宣告，等安全轉換完再賦值
			try {
				// toUpperCase()：前端傳 "customer" 也能正確轉成 CUSTOMER，增加大小寫容錯
				opType = OperationType.valueOf(req.getOperationType().toUpperCase());
			} catch (IllegalArgumentException e) {
				// valueOf 找不到對應 Enum 值，代表前端傳了不合法的字串
				return buildError(ReplyMessage.INVALID_OPERATION_TYPE); // code=400
			}

//			A-1.準備一個裝新購物車的空殼子
			OrderCart newCart = new OrderCart();
//			A-2:塞東西進去，讓newCart各個屬性完整
			newCart.setGlobalAreaId(req.getGlobalAreaId());
			// 改用安全轉換後的 opType，不再直接用 valueOf（已在上面通過 try-catch 安全轉換）
			newCart.setOperationType(opType);
//			操作者：員工點餐存 staffId，顧客點餐存 memberId
//			讀法：「如果是STAFF操作？ → 存staffId ：否則 → 存memberId」
			newCart.setOperation(
					"STAFF".equals(req.getOperationType().toUpperCase()) ? req.getStaffId() : req.getMemberId());

//			A-3:用DAO將塞滿req資料的OrderCart存到資料庫，save()完 DB 自動產生的 ID 會回填到 newCart.getId()
			orderCartDao.save(newCart);
			currentCartId = newCart.getId(); // 至此新購物車的 id 就已經知道了

		} else {
//			 劇本分支 B：已經有車了，id 沿用
			currentCartId = req.getCartId();

			// 🛡️ 防禦：確認這台車尚未被結帳
			// 如果前端帶來的 cartId 已在訂單表裡，不允許再修改
			if (ordersDao.existsByOrderCartId(currentCartId)) {
				// 修改十三：從 throw 改成 return buildError，所有公開方法統一用 buildError 風格
				return buildError(ReplyMessage.CART_ALREADY_CHECKED_OUT); // code=400
			}
		}

//		 步驟 2：處理這筆商品 的增刪改
//		 這時候我們有了 currentCartId (購物車號碼)，以及 req 傳來的 productId 和 quantity

//			 第87行：數量為0 → 刪除
		if (req.getQuantity() == 0) {
//			 一行搞定刪除
			orderCartDetailsDao.deleteByCartIdAndProductId(currentCartId, req.getProductId());

		} else {
//			 去查購物車裡有沒有這個商品
			OrderCartDetails existingDetail = orderCartDetailsDao.findByCartIdAndProductId(currentCartId,
					req.getProductId());

			if (existingDetail != null) {
//				 【修改數量】：委託給私有方法處理，這裡只剩 1 行
				updateItemQuantity(existingDetail, req.getQuantity(), currentCartId, req.getProductId());
			} else {
//				 【新增商品】：委託給私有方法處理，這裡也只剩 1 行
				addNewItem(currentCartId, req);
			}
		}

//		 步驟 3：重新計算金額、滿額贈、包成 Res 回傳給畫面
		return getCartView(currentCartId, req.getMemberId());
	}

//	 這個方法只做一件事：驗證後更新數量
//	 不再深埋在 syncItem 的第3、4層裡
	private void updateItemQuantity(OrderCartDetails existing, int newQty, int cartId, int productId) {
//		 Gate：如果是減少數量，完全不用驗證，直接更新
		if (newQty <= existing.getQuantity()) {
			existing.setQuantity(newQty);
			orderCartDetailsDao.save(existing);
			return; // ← Guard Clause：條件滿足就提前結束，不繼續往下
		}

//		 到這裡代表是「增加數量」，才需要驗證
//		 查購物車主表，取得分店ID
		OrderCart cart = orderCartDao.findById(cartId);
		if (cart == null) {
			throw new RuntimeException("購物車不存在（ID: " + cartId + "），資料異常");
		}

//		 查庫存表
		BranchInventory inv = branchInventoryDao.findByProductIdAndGlobalAreaId(productId, cart.getGlobalAreaId())
				.orElse(null);

//		 Guard 1：庫存設定不存在 → 立即結束（拋錯）
		if (inv == null) {
			throw new RuntimeException("商品 ID " + productId + " 在此分店未設定庫存，無法調整數量");
		}
//		 Guard 2：庫存不足 → 立即結束
		if (newQty > inv.getStockQuantity()) {
			throw new RuntimeException("庫存不足，目前剩餘 " + inv.getStockQuantity() + " 份，無法調整為 " + newQty + " 份");
		}
//		 Guard 3：超過單次上限 → 立即結束
		if (newQty > inv.getMaxOrderQuantity()) {
			throw new RuntimeException("單次最多只能購買 " + inv.getMaxOrderQuantity() + " 份");
		}

//		 全部通過才更新
		existing.setQuantity(newQty);
		orderCartDetailsDao.save(existing);
	}

//	 這個方法只做一件事：驗證後新增商品到購物車
	private void addNewItem(int cartId, CartSyncReq req) {

//		 Guard 1：商品存在且上架？不行就立即結束
		Products product = productsDao.findById(req.getProductId());
		if (product == null || !product.isActive()) {
			throw new RuntimeException("商品 ID " + req.getProductId() + " 不存在或已下架");
		}

//		 查購物車主表取分店ID
		OrderCart cart = orderCartDao.findById(cartId);
		if (cart == null) {
			throw new RuntimeException("購物車不存在（ID: " + cartId + "），資料異常");
		}
//		 查庫存表
		BranchInventory inv = branchInventoryDao
				.findByProductIdAndGlobalAreaId(req.getProductId(), cart.getGlobalAreaId()).orElse(null);

//		 Guard 2：這個分店有庫存設定嗎？沒有就立即結束
		if (inv == null) {
			throw new RuntimeException("商品 ID " + req.getProductId() + " 在此分店未設定庫存");
		}
//		 Guard 3：庫存夠嗎？不夠就立即結束
		if (inv.getStockQuantity() < req.getQuantity()) {
			throw new RuntimeException("商品「" + product.getName() + "」庫存不足，目前剩餘 " + inv.getStockQuantity() + " 份");
		}
//		 Guard 4：有沒有超過單次購買上限？超過就立即結束
		if (req.getQuantity() > inv.getMaxOrderQuantity()) {
			throw new RuntimeException("商品「" + product.getName() + "」單次最多只能購買 " + inv.getMaxOrderQuantity() + " 份");
		}

//		 全部通過！組裝新的購物車明細物件
		OrderCartDetails newDetail = new OrderCartDetails();
		newDetail.setOrderCartId(cartId); // 屬於哪台購物車
		newDetail.setProductId(req.getProductId()); // 哪個商品
		newDetail.setPrice(inv.getBasePrice()); // 定價快照（從庫存表取）
		newDetail.setQuantity(req.getQuantity()); // 數量
		newDetail.setGift(false); // 不是贈品
		orderCartDetailsDao.save(newDetail); // 存進資料庫
	}

	/**
	 * 核心 API 2:刪除購物車內的特定商品
	 */
	@Transactional
	public CartViewRes removeItem(CartRemoveReq req) {
		// 🛡️ 防禦：確認購物車存在
		OrderCart cart = orderCartDao.findById(req.getCartId());
		if (cart == null) {
			// 修改十三：從 throw 改成 return buildError，所有公開方法統一用 buildError 風格
			return buildError(ReplyMessage.CART_NOT_FOUND); // code=404
		}

		// 🛡️ 修改七：確認這台購物車尚未被結帳
		// 已結帳的購物車不允許再刪商品，防止購物車資料與訂單資料不一致
		if (ordersDao.existsByOrderCartId(req.getCartId())) {
			return buildError(ReplyMessage.CART_ALREADY_CHECKED_OUT); // code=400
		}

		// 🛡️ 防禦：只有 CUSTOMER 模式才驗擁有者
		// STAFF 模式：員工代替客人點餐，員工天然有操作權限，不需要 memberId 比對
		if (cart.getOperationType() == OperationType.CUSTOMER && cart.getOperation() != req.getMemberId()) {
			// 修改十三：從 throw 改成 return buildError
			return buildError(ReplyMessage.OPERATE_ERROR); // code=403
		}

		orderCartDetailsDao.deleteByCartIdAndProductId(req.getCartId(), req.getProductId());
		return getCartView(req.getCartId(), req.getMemberId());
	}

//     API 3：使用者選擇贈品
//     前端傳來 giftRuleId（promotions_gifts 主鍵），後端精準定位贈品規則
//     若使用者直接點「確認下單」而未選贈品，前端不會呼叫此 API
	@Transactional
	public CartViewRes selectGift(CartSelectGiftReq req) {
//		    🛡️ 前置防禦：確認購物車存在，避免對不存在的 ID 做無謂的刪除操作
		OrderCart cart = orderCartDao.findById(req.getCartId());
		if (cart == null) {
			return buildError(ReplyMessage.CART_NOT_FOUND);
		}

//		步驟1：先把購物車裡舊的贈品全部清掉（確保購物車只有一個贈品）
		orderCartDetailsDao.deleteAllGiftsByCartId(req.getCartId());

//		步驟2-0：取得前端傳來的贈品規則 ID， giftRuleId是前端從carViewRes裡面獲取的
		int giftRuleId = req.getGiftRuleId();

//		giftRuleId > 0 才代表使用者選了贈品（int 型別缺少時預設是 0）
		if (giftRuleId > 0) {

//			步驟2-1：用 giftRuleId 主鍵精準查詢這條贈品規則
//			findActiveRuleByGiftRuleId 會同時 JOIN promotions 表
//			確保1：規則上架 + 對應活動上架且在有效期間內的贈品規則是否存在，否則回傳 null
			PromotionsGifts giftRule = promotionsGiftsDao.findActiveRuleByGiftRuleId(giftRuleId);

//			步驟2-2：確認這條規則存在且有效
			if (giftRule == null) {
				return buildError(ReplyMessage.GIFT_NOT_AVAILABLE);
			}

//			步驟2-3：從規則物件直接取得贈品的商品 ID（不依賴前端傳值，從資料庫取更安全）
			int giftProductId = giftRule.getGiftProductId();

//			步驟2-4：重新計算購物車小計，確認消費額仍然達標
			List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(req.getCartId());
			BigDecimal subtotal = allDetails.stream()
//					 .filter() ← 集合過濾（List Filtering）
//					 功能：把贈品過濾掉，只留非贈品的明細
//					 !d.isGift() 代表「不是贈品才保留」
					.filter(d -> !d.isGift())
//					 .map() ← 轉換（Mapping）
//					 功能：把每一筆 OrderCartDetails（明細物件）「轉換成」它的金額（BigDecimal）
//					 d.getPrice().multiply(...) ← 單價 × 數量 = 這筆的金額
					.map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
//					 .reduce() ← 聚合（Reducing）
//					 功能：把所有金額加總在一起
//					 BigDecimal.ZERO 是起始值（從 0 開始加）
//					 BigDecimal::add 是加法操作（把兩個值相加）
					.reduce(BigDecimal.ZERO, BigDecimal::add);

//			步驟2-5：確保2：確認消費額達到這條規則的門檻
			if (subtotal.compareTo(giftRule.getFullAmount()) < 0) {
				return buildError(ReplyMessage.NOT_REACH_FULLAMOUNT);
			}
//			步驟2-6：確保3：確認贈品行銷名額還夠（-1=無限量，0=已送完，>0=還有名額）
			if (giftRule.getQuantity() == 0) {
				return buildError(ReplyMessage.GIFT_SEND_LIGHT);
			}

//			步驟2-7：確保4：確認這個贈品商品在商品表裡
//			用 giftProductId（從規則物件取的）在步驟2-3，不是從前端取的
			Products giftProduct = productsDao.findById(giftProductId);
			if (giftProduct == null) {
				return buildError(ReplyMessage.GIFT_NOT_AVAILABLE);
			}

//			步驟2-8：全部驗證通過！把贈品寫進購物車明細
			OrderCartDetails giftDetail = new OrderCartDetails();
			giftDetail.setOrderCartId(req.getCartId()); // 關聯購物車
			giftDetail.setProductId(giftProductId); // 贈品商品 ID（從規則取）
			giftDetail.setPrice(BigDecimal.ZERO); // 贈品 0 元
			giftDetail.setQuantity(1); // 固定送 1 份
			giftDetail.setGift(true); // 標記為贈品
			giftDetail.setDiscountNote("滿額贈"); // 說明來源
			orderCartDetailsDao.save(giftDetail); // INSERT 進資料庫
		}

//		步驟3：重新結算整台購物車並回傳
		return getCartView(req.getCartId(), req.getMemberId());
	}

//        API4:切換分店時候新建一個購物車不用舊的購物車
	/**
	 * API4:切換分店時候新建一個購物車不用舊的購物車 前端呼叫時機：使用者在選擇不同分店時 作用：如果目前有購物車且分店不同 → 清空舊購物車 →
	 * 生成一個屬於新分店的空購物車回傳
	 */
	@Transactional
	public CartViewRes switchBranch(int oldCartId, int newGlobalAreaId, int memberId) {
//		防禦：確認新分店真實存在
		GlobalArea newArea = globalAreaDao.findById(newGlobalAreaId);
		if (newArea == null) {
			// 修改十三：從 throw 改成 return buildError，所有公開方法統一用 buildError 風格
			return buildError(ReplyMessage.GLOBAL_AREA_NOT_FOUND); // code=404
		}

//	     第一步：查舊購物車是否存在
		OrderCart oldCart = orderCartDao.findById(oldCartId);

		// 🛡️ 修改九：確認舊購物車的擁有者是這個 memberId
		// 只有 CUSTOMER 模式需要驗擁有者
		// STAFF 模式：員工代客點餐，天然有操作權，不需要驗
		if (oldCart != null && oldCart.getOperationType() == OperationType.CUSTOMER // 是顧客自己的車
				&& oldCart.getOperation() != memberId) { // 但主人不是這個 memberId
			return buildError(ReplyMessage.OPERATE_ERROR); // code=403，無權操作他人的購物車
		}

//	     如果舊購物車存在，且分店ID已經不同了（代表確實換了分店）
		if (oldCart != null && oldCart.getGlobalAreaId() != newGlobalAreaId) {
//	         把舊購物車的所有明細刪掉（清空商品和贈品）
			orderCartDetailsDao.deleteAllByCartId(oldCartId);
//	         注意：這裡也可以選擇把 order_cart 主表也刪掉，看你的設計
//	         orderCartDao.delete(oldCartId);
		}

//	     第二步：建一台新的空購物車，綁定新分店
		OrderCart newCart = new OrderCart();
		newCart.setGlobalAreaId(newGlobalAreaId); // ← 關鍵：綁定新分店
		newCart.setOperationType(OperationType.CUSTOMER); // 預設操作者類型
		newCart.setOperation(memberId); // 記錄誰在操作
		orderCartDao.save(newCart); // 存進資料庫，取得新的 CartId

//	     第三步：回傳一台空的購物車給前端（前端用新的 cartId 取代舊的）
		return getCartView(newCart.getId(), memberId);
	}

//	     API 5：清空購物車（一般商品 + 贈品全刪）

	@Transactional
	public CartViewRes clearCart(CartClearReq req) {
		// 🛡️ 防禦：確認購物車存在才刪，避免對不存在的 ID 做無謂操作
		OrderCart cart = orderCartDao.findById(req.getCartId());
		if (cart == null) {
			return buildError(ReplyMessage.CART_NOT_FOUND);
		}

		// 🛡️ 修改八：確認這台購物車尚未被結帳
		// 已結帳的購物車不允許清空，防止訂單資料失去對應的明細記錄
		if (ordersDao.existsByOrderCartId(req.getCartId())) {
			return buildError(ReplyMessage.CART_ALREADY_CHECKED_OUT); // code=400
		}

//	         刪除這台購物車的所有明細（一般商品 + 贈品全部清空）
		orderCartDetailsDao.deleteAllByCartId(req.getCartId());
//	         回傳空的購物車（items=空清單、subtotal=0、total=0）
		return getCartView(req.getCartId(), req.getMemberId());
	}

	/**
	 * api6：查看購物車內容 (例如前端一進到購物車頁面要拉取資料)
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

//	         ── 步驟 1：確認購物車存在，並驗證擁有者身份 ──
		OrderCart cart = orderCartDao.findById(cartId);
		if (cart == null) {
			// 修改十三：改成 buildError，與其他方法風格一致
			return buildError(ReplyMessage.CART_NOT_FOUND); // code=404
		}

		// 🛡️ 修改十：確認這台購物車屬於這個 memberId
		// STAFF 模式（員工代客點餐）不需要驗，員工天然有操作權
		// CUSTOMER 模式只有本人可以查看自己的購物車
		if (cart.getOperationType() == OperationType.CUSTOMER && cart.getOperation() != memberId) {
			return buildError(ReplyMessage.OPERATE_ERROR); // code=403，無權查看他人的購物車
		}

//	         ── 步驟 2：檢查2：逐一驗算每筆「非贈品」的狀態與金額並組裝成商品VO、最後累加小計 ─
//		 ①：先撈出這台車的每一個購物車明細進行組裝成商品VO（購物計車明細是商品+贈品的）
//		這是最大的 for 迴圈，要做 3 件事：（A)檢查狀態（B）檢查金額：比對定價(C) 組裝 成商品VO、(D) 累加小計
//		 查所有明細
		List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(cartId);

//		 先用 Stream filter 把「一般商品」和「贈品」分開
//		 只保留 isGift() = false 的那些明細
		List<OrderCartDetails> normalItems = allDetails.stream().filter(d -> !d.isGift()) // ← 過濾：只留非贈品
				.collect(java.util.stream.Collectors.toList()); // ← 收集結果成一個新的 List

//		 現在 normalItems 裡只有一般商品，for 迴圈乾淨多了，不需要再寫 continue
		// 修改十一：把「組裝單一商品 VO」的複雜邏輯委託給私有方法 buildNormalItemVO
		// for 迴圈從原來的 5 層巢狀縮排簡化到只剩 3 行，可讀性大幅提升
		for (OrderCartDetails detail : normalItems) {
			CartItemVO vo = buildNormalItemVO(detail, cart, warningMessages); // 商品狀態驗證 + VO 組裝
			voList.add(vo); // 不管是否缺貨，都要顯示在畫面讓用戶知道
			// 所有情況的 lineTotal：error 時是 BigDecimal.ZERO，正常時是 price × qty
			// 累加 ZERO 不影響結果，所以不需要 if 判斷就能直接加
			subtotal = subtotal.add(vo.getLineTotal());
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

//			 先查這個贈品在本分店的實體庫存
//			 cart 物件在步驟1已查好，這裡可以直接使用
//			 本分店的ID（從購物車主表取）， 贈品的商品ID（從已選贈品明細取）
			BranchInventory giftInv = branchInventoryDao
					.findByProductIdAndGlobalAreaId(existingGift.getProductId(), cart.getGlobalAreaId()).orElse(null);

//	             判斷贈品是否「依然有效」：
			boolean giftStillValid = giftRule != null // 條件1：贈品規則有效
					&& giftProduct != null // 條件2：贈品商品存在
					&& (giftRule.getQuantity() == -1 || giftRule.getQuantity() > 0) // 條件3：行銷名額充足
					&& giftInv != null // 條件4（新增）：分店有庫存設定
					&& giftInv.getStockQuantity() > 0 // 條件5（新增）：分店實體庫存 > 0
					&& subtotal.compareTo(giftRule.getFullAmount()) >= 0; // 條件6：消費金額達標
// ✅ 現在：行銷名額 + 實體庫存 兩層都確認才算有效
			if (giftStillValid) {
//	            CartItemVO的清單voList贈品部分：     有效：把贈品 VO 加入CartItemVO的清單voList中顯示在購物車界面
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

//	  ── 步驟 4：建立「以活動為單位」的兩層可選贈品清單 ──
//		 外層清單：裝「使用者有資格參加的活動」
//		 最後這個清單會被設定進 res.setAvailablePromotions()
//		 空清單代表消費未達任何活動門檻，前端不顯示「選擇活動」按鈕
		List<AvailablePromotionVO> availablePromotions = new ArrayList<>();

//		去資料庫撈出所有目前上架且在有效時間範圍內的活動（完整物件，含 id 和 name）
		List<Promotions> activePromotions = promotionsDao.findActivePromotions();

// =========================================================
// 改後版本（groupingBy 方案，只查 1 次DB）：
// =========================================================

// Step 1：一次查詢，拿出【所有有效活動的所有有效贈品規則】
// findAllActiveGifts() 在 PromotionsGiftsDao 第33~40行已寫好但被注解掉了！
// 打開注解即可使用
		List<PromotionsGifts> allActiveGifts = promotionsGiftsDao.findAllActiveGifts();

// Step 2：用 groupingBy 在記憶體裡分組
// Collectors.groupingBy(分組的key是什麼)
// 這裡的 key = 每條規則的 promotionsId（屬於哪個活動）
// 結果：Map<Integer（活動ID）, List<PromotionsGifts>（這個活動的所有規則）>
		java.util.Map<Integer, List<PromotionsGifts>> giftsByPromotionId = allActiveGifts.stream()
//				 .collect() ← 把 Stream 轉成集合
//				 Collectors.groupingBy() ← 按照某個欄位分組（類似 SQL 的 GROUP BY）
//				 PromotionsGifts::getPromotionsId ← 用方法引用取出分組的 key（活動ID）
				.collect(java.util.stream.Collectors.groupingBy(PromotionsGifts::getPromotionsId));

//		 逐一審查每個活動，看使用者的消費有沒有資格進入
		for (Promotions promotion : activePromotions) {

//			 map.getOrDefault(key, 預設值)
//			 如果 map 裡有這個活動ID的規則 → 回傳那份 List
//			 如果沒有（這個活動沒有設贈品規則）→ 回傳空清單（不報錯）
			List<PromotionsGifts> giftsInThisPromotion = giftsByPromotionId.getOrDefault(promotion.getId(),
					java.util.Collections.emptyList());

//			 如果這個活動底下沒有任何贈品規則（活動建了但沒設贈品），直接跳過
//			 前端：這個活動完全不出現在下拉選單裡
			if (giftsInThisPromotion.isEmpty()) {
				continue; // 跳過，進入 for 迴圈下一輪
			}

//			 找出這個活動底下「門檻最低的那條規則的金額」
			BigDecimal minFullAmount = giftsInThisPromotion.stream()
//					 .map() ← 把每條 PromotionsGifts 規則，取出它的門檻金額（getFullAmount()）
//					 PromotionsGifts::getFullAmount 是「方法引用」寫法
//					 等價於：rule -> rule.getFullAmount()
					.map(PromotionsGifts::getFullAmount)
//					 .min() ← 在所有取出的金額裡，找最小的那個
//					 BigDecimal::compareTo 告訴 Stream 怎麼比大小（BigDecimal 不能用 < > 比）
					.min(BigDecimal::compareTo)
//					 .orElse() ← 如果清單是空的（沒有任何值），就用 BigDecimal.ZERO 當預設值
//					 這一行取代了原本的 if (minFullAmount == null) { minFullAmount = ZERO; }
					.orElse(BigDecimal.ZERO);

//			 判斷使用者消費小計有沒有達到這個活動的最低門檻
			if (subtotal.compareTo(minFullAmount) < 0) {
//				 subtotal < minFullAmount → 消費不達標 → 跳過整個活動
//				 前端：這個活動不出現在下拉選單（沒有「選擇活動」按鈕）
				continue;
			}

//			── 使用者達到這個活動的最低門檻！開始組裝這個活動底下的贈品清單 ──
//			內層清單：裝「這個活動底下每個贈品選項的狀況」
			List<AvailableGiftVO> giftsVoList = new ArrayList<>();

//			 逐一審查這個活動底下的每條贈品規則
			for (PromotionsGifts rule : giftsInThisPromotion) {

//				 確認使用者消費有沒有達到「這條贈品規則」自己的門檻
//				 同一個活動可能有不同門檻：滿300送可樂、滿500才能選大盤雞
//				 使用者消費400 → 能選可樂，但不能選大盤雞
				if (subtotal.compareTo(rule.getFullAmount()) < 0) {
					continue;
				}

//				 消費達到這條規則！開始組裝這個贈品的 VO
				AvailableGiftVO option = new AvailableGiftVO();
				option.setGiftRuleId(rule.getId()); // promotions_gifts 主鍵
				option.setGiftProductId(rule.getGiftProductId()); // 贈品的商品 ID
				option.setFullAmount(rule.getFullAmount()); // 這條規則的消費門檻

//				 去商品表查這個贈品商品的狀態（是否存在）
//				 rule.getGiftProductId() 是 promotions_gifts.gift_product_id 的值
//				 後端用它去 products 表拿完整的商品資訊（名稱、上架狀態等）
				Products giftAsProd = productsDao.findById(rule.getGiftProductId());

				if (giftAsProd == null) {
//					 情況1：贈品商品被刪除
//					 前端：這個贈品顯示為灰色不可點，旁邊顯示原因
					String gName = "贈品 #" + rule.getGiftProductId();
					option.setGiftProductName(gName);
					option.setAvailable(false); // false → 前端灰色不可選
					option.setUnavailableReason("「" + gName + "」不存在");

				} else if (rule.getQuantity() == 0) {
//					 情況2：這條規則的贈品行銷名額已送完
//					 quantity = 0 → 送完了（-1 = 無限量，> 0 = 還有名額）
//					 前端：這個贈品顯示為灰色不可點，旁邊顯示已送完
					option.setGiftProductName(giftAsProd.getName());
					option.setAvailable(false);
					option.setUnavailableReason("「" + giftAsProd.getName() + "」贈品已送完");

				} else {
//				 情況3：行銷名額充足，進一步確認分店實體庫存
//				 防禦缺口：promotions_gifts.quantity（行銷名額）充足，
//				 但 branch_inventory（分店實體庫存）可能已空，兩者是不同層面的庫存
					BranchInventory giftInv = branchInventoryDao
							.findByProductIdAndGlobalAreaId(rule.getGiftProductId(), cart.getGlobalAreaId())
							.orElse(null);
					option.setGiftProductName(giftAsProd.getName());
					if (giftInv == null || giftInv.getStockQuantity() <= 0) {
//				 情況3a：行銷名額有，但分店實體庫存已空或未設定
//				 前端：這個贈品顯示為灰色不可點，旁邊顯示暫時缺貨
						option.setAvailable(false);
						option.setUnavailableReason("「" + giftAsProd.getName() + "」暫時缺貨");
					} else {
//				 情況3b：行銷名額 + 實體庫存都正常，這個贈品可以選
//				 前端：正常顯示，使用者可以點選
						option.setAvailable(true);
						option.setUnavailableReason(null); // null → 沒有問題
					}
				}

//				 把這個贈品選項加入這個活動的贈品清單
				giftsVoList.add(option);
			}

//			 組裝這個活動的外層 VO
			AvailablePromotionVO promotionVO = new AvailablePromotionVO();
			promotionVO.setPromotionId(promotion.getId()); // 活動 ID
			promotionVO.setPromotionName(promotion.getName()); // 活動名稱（前端下拉顯示）
			promotionVO.setFullAmount(minFullAmount); // 這個活動的最低消費門檻
			promotionVO.setGifts(giftsVoList); // 這個活動底下的所有贈品選項

//			 把這個活動加入外層清單
			availablePromotions.add(promotionVO);
		}

//	         ── 步驟 5：查稅務設定並計算稅額 ──
		TaxInfoVO taxInfo = new TaxInfoVO();
		BigDecimal totalAmount = subtotal; // 預設（無稅設定時/內含稅）：總計 = 小計
		Regions region = regionsDao.findByGlobalAreaId(cart.getGlobalAreaId());
		if (region == null) {
			System.err.println("[WARN] global_area_id=" + cart.getGlobalAreaId() + " 查無稅務設定");
			warningMessages.add("此分店稅務設定有誤，金額僅供參考，請聯繫管理員");
		} else {
			taxInfo.setTaxRate(region.getTaxRate());
//				如果它原本是 TaxType.EXCLUSIVE 的enum物件。加上 .name() 後，它身上的一層皮就被剝下來，變成純粹的英文字串 "EXCLUSIVE"。
			if (region.getTaxType() != null) {
				taxInfo.setTaxType(region.getTaxType().name());

				BigDecimal taxAmount;
				if (region.getTaxType() == TaxType.EXCLUSIVE) {
//					 外加稅：稅額 = 小計 × 稅率，總計 = 小計 + 稅額
					taxAmount = subtotal.multiply(region.getTaxRate()).setScale(2, RoundingMode.HALF_UP);
					totalAmount = subtotal.add(taxAmount);
				} else {
//					內含稅：反推稅額 = 小計 × 稅率 ÷ (1 + 稅率)
					taxAmount = subtotal.multiply(region.getTaxRate()).divide(BigDecimal.ONE.add(region.getTaxRate()),
							2, RoundingMode.HALF_UP);
					totalAmount = subtotal; // 內含稅：總計就是小計，不另加稅
				}
				taxInfo.setTaxAmount(taxAmount);
			}
			// taxType 為 null → 完全跳過稅務計算，totalAmount 保持等於 subtotal（無稅）

//	         ── 步驟 6：打包所有結果回傳 ──
			/*
			 * voList就是cartItem（包括贈品）的list availablePromotions是 以活動為單位的兩層可選贈品清單
			 * subtotal是小計，taxInfo是稅務資訊，totalAmount是最終總計 warningMessages警告訊息（空清單代表一切正常）
			 */
		}
		res.setItems(voList);// 裝填這台車所有的商品與贈品
		res.setSubtotal(subtotal);
		res.setAvailablePromotions(availablePromotions);// 設置：以活動為單位的兩層巢狀結構
		res.setTaxInfo(taxInfo);
		res.setTotalAmount(totalAmount);
		res.setWarningMessages(warningMessages);
		res.setCode(ReplyMessage.SUCCESS.getCode());
		res.setMessage(ReplyMessage.SUCCESS.getMessage());
		return res;
	}

	/**
	 * 【修改十一】私有工具方法：組裝單一「一般商品」的 CartItemVO 抽出這個方法的目的：讓 getCartView 的 for 迴圈從 5
	 * 層巢狀縮排降至 1 層，大幅提升可讀性 使用 Guard Clause（守門人模式）：遇到問題就提前 return，不用繼續往下執行
	 *
	 * @param detail          購物車明細（單一商品那列）
	 * @param cart            購物車主表（需要裡面的 globalAreaId）
	 * @param warningMessages 警告訊息清單（共用引用，方法內部直接往裡 add）
	 * @return 組裝好的 CartItemVO（lineTotal = ZERO 代表不計入小計；有值代表正常商品）
	 */
	private CartItemVO buildNormalItemVO(OrderCartDetails detail, OrderCart cart, List<String> warningMessages) {
		CartItemVO vo = new CartItemVO();

		// 先查這個商品目前的狀態（是否存在、是否上架）
		Products product = productsDao.findById(detail.getProductId());

		// ── 情況一：商品已下架或被刪除（Guard Clause：提前回傳，lineTotal=ZERO）──
		if (product == null || !product.isActive()) {
			// 商品物件還在只是被下架 → 抓原名；商品被刪除 → 用 productId 當替代名稱
			String name = (product != null) ? product.getName() : "商品 #" + detail.getProductId();
			vo.setProductName(name + "（已下架）"); // 名稱後面加（已下架）讓前端顯示原因
			vo.setPrice(detail.getPrice()); // 顯示最後一次的快照價（供參考）
			vo.setLineTotal(BigDecimal.ZERO); // 不計入小計
			vo.setDetailId(detail.getId());
			vo.setProductId(detail.getProductId());
			vo.setQuantity(detail.getQuantity());
			vo.setGift(false);
			warningMessages.add("「" + name + "」已下架或不存在，請將其移除");
			return vo; // ← Guard Clause：提前回傳，下面不需要執行
		}

		// 走到這裡：商品存在且上架
		vo.setProductName(product.getName());

		// ── 情況二：商品上架，但分店庫存設定不存在 ──
		BranchInventory inv = branchInventoryDao
				.findByProductIdAndGlobalAreaId(detail.getProductId(), cart.getGlobalAreaId()).orElse(null);
		if (inv == null) {
			// 這代表後台資料有問題（商品存在但這個分店沒有庫存設定）
			System.err.println("[WARN] product_id=" + detail.getProductId() + " 在 global_area_id="
					+ cart.getGlobalAreaId() + " 查無 branch_inventory 設定，請檢查資料完整性");
			vo.setDetailId(detail.getId());
			vo.setProductId(detail.getProductId());
			vo.setQuantity(detail.getQuantity());
			vo.setPrice(detail.getPrice()); // 顯示快照價供前端參考
			vo.setLineTotal(BigDecimal.ZERO); // 不計入小計
			vo.setGift(false);
			warningMessages.add("「" + product.getName() + "」在此分店查無庫存設定，請聯絡店員");
			return vo; // ← Guard Clause：提前回傳，lineTotal=ZERO
		}

		// ── 情況三：商品上架且庫存設定存在，依序做各種防禦性檢查 ──

		// (B-1) 調價偵測：看看店長有沒有改過定價
		BigDecimal currentPrice = inv.getBasePrice();
		if (detail.getPrice().compareTo(currentPrice) != 0) {
			warningMessages.add("「" + product.getName() + "」的價格已從 $" + detail.getPrice() + " 調整為 $" + currentPrice);
			detail.setPrice(currentPrice); // 更新 detail 的快照價（記憶體）
			orderCartDetailsDao.save(detail); // 儲存回資料庫
		}

		// (B-2) 庫存歸零防禦：商品賣完了但還沒手動下架（Guard Clause：提前回傳）
		if (inv.getStockQuantity() <= 0) {
			vo.setDetailId(detail.getId());
			vo.setProductId(detail.getProductId());
			vo.setQuantity(detail.getQuantity()); // 顯示原本數量，讓前端提醒客人
			vo.setPrice(detail.getPrice());
			vo.setLineTotal(BigDecimal.ZERO); // 缺貨不計入小計
			vo.setGift(false);
			warningMessages.add("「" + product.getName() + "」目前暫時缺貨，已暫時從計算中移除，請移除該商品或稍後再試");
			return vo; // ← Guard Clause：提前回傳，lineTotal=ZERO
		}

		// (B-3) 超過庫存數量防禦：自動降至庫存上限
		if (detail.getQuantity() > inv.getStockQuantity()) {
			int safeQty = inv.getStockQuantity(); // 安全數量 = 目前最大庫存
			detail.setQuantity(safeQty); // 強制降至庫存上限（記憶體）
			orderCartDetailsDao.save(detail); // 存回資料庫
			warningMessages.add("「" + product.getName() + "」庫存不足，已為您自動調整數量為 " + safeQty + " 份");
		}

		// (B-4) 超過單次限購數量防禦：自動降至限購上限
		if (detail.getQuantity() > inv.getMaxOrderQuantity()) {
			int safeQty = inv.getMaxOrderQuantity(); // 安全數量 = 限購上限
			detail.setQuantity(safeQty);
			orderCartDetailsDao.save(detail);
			warningMessages.add("「" + product.getName() + "」單次限購 " + safeQty + " 份，已為您自動調整");
		}

		// (C) 全部檢查通過！用「最終安全數量」組裝完整 VO
		vo.setDetailId(detail.getId());
		vo.setProductId(detail.getProductId());
		vo.setQuantity(detail.getQuantity()); // 已修正後的安全數量
		vo.setGift(false);
		vo.setDiscountNote(detail.getDiscountNote()); // 折扣說明
		BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
		vo.setPrice(detail.getPrice()); // 顯示單價
		vo.setLineTotal(lineTotal); // 這筆的金額（非ZERO，會被累加進 subtotal）
		return vo;
	}

	/**
	 * 私有工具方法：快速組裝一個錯誤回應 功能：把「建立 CartViewRes、設 code、設 message、return」這三行動作包成一行
	 * 使用情境：selectGift 和其他 API 遇到錯誤時直接 return buildError(ReplyMessage.XXX)
	 *
	 * @param replyMessage ReplyMessage 列舉常數（例如 GIFT_NOT_AVAILABLE、CART_NOT_FOUND）
	 * @return 設好 code 和 message 的錯誤 CartViewRes 物件
	 */
	private CartViewRes buildError(ReplyMessage replyMessage) {
		CartViewRes err = new CartViewRes();
		err.setCode(replyMessage.getCode());
		err.setMessage(replyMessage.getMessage());
		return err;
	}

}