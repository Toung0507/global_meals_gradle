package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.OrdersStatus;
import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.BranchInventoryDao;
import com.example.global_meals_gradle.dao.MembersDao;
import com.example.global_meals_gradle.dao.OrderCartDetailsDao;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.dao.PromotionsGiftsDao;
import com.example.global_meals_gradle.dao.RegionsDao;
import com.example.global_meals_gradle.entity.BranchInventory;
import com.example.global_meals_gradle.entity.Members;
import com.example.global_meals_gradle.entity.OrderCartDetails;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.entity.Regions;
import com.example.global_meals_gradle.req.CreateOrdersReq;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.req.RefundedReq;
import com.example.global_meals_gradle.req.KitchenStatusReq;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.CreateOrdersRes;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.res.GetOrdersDetailVo;
import com.example.global_meals_gradle.res.GetOrdersVo;
import com.example.global_meals_gradle.res.GetTodayOrdersRes;
import com.example.global_meals_gradle.res.TodayOrderDetailVo;
import com.example.global_meals_gradle.res.TodayOrderVo;

/*	待做:
 * 	成立訂單那邊的庫存需不需要以分店做區分;已經有未稅金額，但還需要做稅率跟含稅總金額
 */

@Service
public class OrdersService {

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private ProductsDao productsDao;

	@Autowired
	private MembersDao membersDao;

	@Autowired
	private OrderCartDetailsDao orderCartDetailsDao;

	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;

	@Autowired
	private RegionsDao regionsDao;

	@Autowired
	private BranchInventoryDao branchInventoryDao;

	// 這樣在呼叫 self.executeInsert 時，Spring 才會啟動 @Transactional 的代理機制。
	@Autowired
	@Lazy // 加上 @Lazy 避免某些 Spring 版本出現循環依賴的警告
	private OrdersService self;

	// /* 查詢歷史訂單 */
	// @Transactional(readOnly = true) // 只有查詢，寫這段對效能比較好
	// public GetAllOrdersRes getAllOrders(HistoricalOrdersReq req) {
	// /* 判斷會員id是否存在 */
	//
	//
	// try {
	// /* 取得主表(Orders)該會員的歷史訂單 */
	// List<GetOrdersVo> orderList =
	// ordersDao.getOrderByMemberId(req.getMemberId());
	// /* 第一層迴圈跑每一筆訂單拿到各自的明細 */
	// for (GetOrdersVo order : orderList) {
	// // 根據購物車id，拿到該訂單的明細
	// List<OrderCartDetails> detailEntities = orderCartDetailsDao
	// .getProductByOrderCartId(order.getOrderCartId());
	// // 要把上面拿到的資料，放進vo裡
	// List<GetOrdersDetailVo> detailVoList = new ArrayList<>();
	// /* 第二層迴圈，利用產品id 去products表取得產品名稱 */
	// for (OrderCartDetails entity : detailEntities) {
	// GetOrdersDetailVo vo = new GetOrdersDetailVo();
	// // 產品id 去products表取得產品名稱
	// String productName = productsDao.getProductsNameById(entity.getProductId());
	// // 把各個資料塞進去vo
	// vo.setName(productName); // 塞進去上個步驟取的名子
	// vo.setQuantity(entity.getQuantity());
	// vo.setPrice(entity.getPrice());
	// vo.setGift(entity.isGift());
	// vo.setDiscountNote(entity.getDiscountNote());
	// // 把整理好的資料裝進voList裡
	// detailVoList.add(vo);
	// }
	// // 把整理好的訂單明細，塞進去欄位
	// order.setGetOrdersDetailVoList(detailVoList);
	// }
	// return new GetAllOrdersRes(ReplyMessage.SUCCESS.getCode(), //
	// ReplyMessage.SUCCESS.getMessage(), orderList);
	// } catch (Exception e) {
	// throw e;
	// }
	// }

	/* 查詢歷史訂單 */
	@Transactional(readOnly = true) // 只有查詢，寫這段對效能比較好
	public GetAllOrdersRes getAllOrders(HistoricalOrdersReq req) {
		// 會員id檢查，如果是null，就是id有誤
		Members member = membersDao.findById(req.getMemberId());
		if (member == null) {
			return new GetAllOrdersRes(ReplyMessage.MEMBER_NOT_FOUND.getCode(),
					ReplyMessage.MEMBER_NOT_FOUND.getMessage());
		}
		List<Object[]> rawData = ordersDao.getFullOrderHistory(req.getMemberId());
		// 用 Map 來群組化，Key 是 "日期+ID"，Value 是訂單 VO
		// LinkedHashMap 是為了「照順序排」，讓最新下單的排在最前面
		Map<String, GetOrdersVo> orderMap = new LinkedHashMap<>();
		for (Object[] row : rawData) {
			String orderKey = row[1].toString() + row[0].toString(); // 日期 + ID

			// 如果這個訂單還沒在 Map 裡，先建立它
			// 【補充說明：Lambda 表達式】
			// 這裡的 k -> { ... } 是一個 Lambda 表達式。
			// computeIfAbsent 的意思是：如果 orderMap 裡面還沒有這個 orderKey，
			// 就會自動執行後面這個 Lambda 裡面的程式碼，把回傳的 newVo 存進 Map 中並回傳給左邊的 vo。
			// 代替以前一定要寫 if (map.get(key) == null) 的麻煩判斷。
			// 以前寫法:
			// GetOrdersVo vo = orderMap.get(orderKey);
			// if (vo == null) {
			// vo = new GetOrdersVo();
			// orderMap.put(orderKey, vo);
			// }
			GetOrdersVo vo = orderMap.computeIfAbsent(orderKey, k -> {
				// 建立一個新的訂單物件
				GetOrdersVo newVo = new GetOrdersVo();
				newVo.setId(row[0].toString()); // o.id
				newVo.setOrderDateId(row[1].toString()); // o.order_date_id
				newVo.setGlobalAreaId((Integer) row[2]); // o.global_area_id
				newVo.setTotalAmount((BigDecimal) row[3]); // o.total_amount
				newVo.setStatus(row[4].toString()); // o.status
				newVo.setGetOrdersDetailVoList(new ArrayList<>());
				return newVo;
			});

			// 建立明細並塞入該訂單的 List
			GetOrdersDetailVo detail = new GetOrdersDetailVo();
			// 如果是寫 Integer ，DB 回傳是：BigInteger/Long，會直接噴 ClassCastException
			detail.setQuantity(((Number) row[3]).intValue());
			detail.setPrice((BigDecimal) row[4]);
			detail.setGift((Boolean) row[5]);
			detail.setDiscountNote((String) row[6]);
			detail.setName((String) row[7]); // 產品名稱已經在 SQL 抓好了

			vo.getGetOrdersDetailVoList().add(detail);
		}
		// 把 Map 的值轉換成 List
		List<GetOrdersVo> result = new ArrayList<>(orderMap.values());

		return new GetAllOrdersRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), result);
	}

	/* 成立訂單: 外部呼叫的主入口：負責「高併發重試流程」 */
	// 這個方法「不加」@Transactional，這樣裡面的 try-catch 才能重複執行。
	public CreateOrdersRes createOrders(CreateOrdersReq req) {
		// 參數檢查(庫存檢查、金額檢查)
		if (req.isUseDiscount()) {
			Members member = membersDao.findById(req.getMemberId());
			if (!member.isDiscount()) {
				throw new RuntimeException("無優惠可使用");
			}
		}
		// 取得今天的日期字串，例如 "20260328"
		// DateTimeFormatter.ofPattern("yyyyMMdd"): 定義日期格式 .format(...):
		// 把得到的日期轉換成前面定義的格式
		String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		// 設定最大重試次數。如果很多人同時搶號碼，失敗了就重新跑一次迴圈。
		int maxRetries = 5;

		for (int i = 0; i < maxRetries; i++) {
			try {
				// 必須透過 self. 呼叫，否則事務 (@Transactional) 會失效！
				return self.executeInsert(req, todayStr);
			} catch (RuntimeException e) {
				// 判斷是否為「可重試」的異常
				String msg = e.getMessage();
				// 只有當訊息包含「衝突」(樂觀鎖失敗) 或 「Duplicate」(序號重複) 時才進入重試
				if (msg != null && (msg.contains("衝突") || msg.contains("Duplicate")//
						|| msg.contains("Primary"))) {
					// 如果還沒超過重試次數，就繼續跑下一輪 for 迴圈。
					if (i == maxRetries - 1) {
						// 如果重試了 5 次都還是失敗，才拋出錯誤。
						throw new RuntimeException("系統繁忙，請重新結帳");
					}

					// 指數退避: 越多次，時間越久
					long sleepTime = 50 * (long) Math.pow(2, i);

					// 加隨機 jitter（0~50ms）
					sleepTime += ThreadLocalRandom.current().nextInt(0, 50);

					// 上限（最多等 1 秒）
					sleepTime = Math.min(sleepTime, 1000);

					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt(); // 恢復中斷狀態
					}

					// 繼續下一次 for 迴圈 (即重試)
					continue;
				}
				// --- 如果不是以上衝突錯誤，代表是「邏輯錯誤」(如：金額未達門檻、庫存不足) ---
				// 直接把錯誤丟出去給前端，不要浪費資源重試
				throw e;
			} catch (Exception e) {
				// 處理非 RuntimeException 的意外錯誤
				throw new RuntimeException("訂單系統發生非預期錯誤: " + e.getMessage());
			}
		}
		return null;
	}

	/* 成立訂單: 內部執行方法：負責「查詢庫存 + 查詢最大序號 + 寫入資料庫」。 */
	// 加上 @Transactional，確保這段動作在資料庫中是原子性的（要嘛全成功，要嘛全失敗）。
	@Transactional(rollbackFor = Exception.class)
	public CreateOrdersRes executeInsert(CreateOrdersReq req, String todayStr) {

		// 取的購物車清單
		List<OrderCartDetails> cartDetailsList = req.getOrderCartDetailsList();
		// ====== 使用 Map 合併相同 ID 的扣除總量(把商品跟贈品相同的一起計算數量) ======
		// Key: 產品ID, Value: 總數量 (商品 + 贈品)
		Map<Integer, Integer> stockToReduceMap = new HashMap<>();

		/*
		 * 這是一般的 if-else 寫法 for (OrderCartDetails detail : cartDetailsList) { int pid =
		 * detail.getProductId(); int qty = detail.getQuantity();
		 * 
		 * 檢查 Map 裡是不是已經有這個商品 ID 了 if (stockToReduceMap.containsKey(pid)) { //
		 * 【情況A】：已經有了（例如之前跑過商品，現在跑贈品） int oldQty = stockToReduceMap.get(pid); // 取出舊的數量
		 * stockToReduceMap.put(pid, oldQty + qty); // 加完後更新進去 } else { // 【情況 B】：還沒出現過
		 * stockToReduceMap.put(pid, qty); // 直接放進去 } }
		 */

		/*
		 * merge 方法的三個參數意義： detail.getProductId()：我要找哪個 ID？ detail.getQuantity()：如果這個 ID
		 * 第一次出現，預設值(這裡是指數量)是多少？ Integer::sum：如果這個 ID 已經存在了，舊的值跟新的值要怎麼處理？（這裡指定用
		 * sum，也就是「加起來」）
		 */
		for (OrderCartDetails detail : cartDetailsList) {
			stockToReduceMap.merge(detail.getProductId(), detail.getQuantity(), Integer::sum);
		}
		// 將 Map 的 Key 轉成 List 並排序，防止不同執行緒因鎖定順序不同而死結 (Deadlock)
		List<Integer> sortedProductIds = new ArrayList<>(stockToReduceMap.keySet());
		Collections.sort(sortedProductIds);
		// 初始化未稅總金額 (使用 BigDecimal.ZERO 確保精準度)
		BigDecimal subtotal = BigDecimal.ZERO;
		// 迴圈計算總額時，順便把「所有的贈品 ID」存進這個清單
		List<Integer> giftProductIds = new ArrayList<>();
		// 取得該分店所屬國家的稅務設定
		Regions region = regionsDao.findTaxByAreaId(req.getGlobalAreaId());
		if (region == null) {
			throw new RuntimeException("找不到該分店的稅務設定");
		}
		BigDecimal taxRate = region.getTaxRate(); // 稅率
		String taxType = region.getTaxType().name(); // 稅制
		BigDecimal taxAmount = BigDecimal.ZERO; // 稅額
		BigDecimal afterTax = BigDecimal.ZERO; // 含稅

		// ====== 金額計算/贈品id儲存 ======
		// 不是贈品的才要計算金額 / 贈品的Id要存進贈品清單
		for (OrderCartDetails detail : cartDetailsList) {
			if (!detail.isGift()) {
				// 這裡還是需要查一次價格來算總帳
				Products p = productsDao.findById(detail.getProductId());
				// 取的該商品在該分店的價格(未稅)
				BranchInventory inv = branchInventoryDao
						.findByProductIdAndGlobalAreaId(detail.getProductId(), req.getGlobalAreaId())
						.orElseThrow(() -> new RuntimeException("該分店未上架商品 ID: " + detail.getProductId()));
				BigDecimal qty = BigDecimal.valueOf(detail.getQuantity()); // 取的商品購買數量
				if ("INCLUSIVE".equals(taxType)) {
					// --- 內含稅：單價先乘稅率並無條件進位 (產生標價) ---
					BigDecimal priceWithTax = inv.getBasePrice() // priceWithTax = 商品含稅價格
							.multiply(BigDecimal.ONE.add(taxRate)) // 1 * 稅率
							.setScale(0, RoundingMode.UP); // 無條件進位
					// 累加含稅總額
					subtotal = subtotal.add(priceWithTax.multiply(qty));
				} else {
					// --- 外加稅：直接用未稅單價累加 ---
					subtotal = subtotal.add(p.getBasePrice().multiply(qty));
				}

			} else {
				giftProductIds.add(detail.getProductId());
			}
		}
		BigDecimal finalSubtotal; // 未稅小計
		if (taxType.equals("EXCLUSIVE")) { // 外加稅：總金額 = 小計 * (1 * 稅率)
			afterTax = subtotal.multiply(BigDecimal.ONE.add(taxRate));
			if (req.isUseDiscount()) {
				afterTax = afterTax.multiply(new BigDecimal("0.8"));
			}
			afterTax = afterTax.setScale(0, RoundingMode.UP); // 稅後實收總金額(無條件進位)
			// 處理稅額: 總額 - 小計
			finalSubtotal = subtotal;
			if (req.isUseDiscount()) {
				finalSubtotal = finalSubtotal //
						.multiply(new BigDecimal("0.8")).setScale(0, RoundingMode.UP); // 小計
			}
			taxAmount = afterTax.subtract(finalSubtotal);
		} else { // 內含稅：總金額 = 小計，稅額 = 小計 - (小計 / (1 + 稅率))
			afterTax = subtotal;   // 含稅額
			if (req.isUseDiscount()) {
				afterTax = afterTax.multiply(new BigDecimal("0.8")).setScale(0, RoundingMode.UP);
			}
			// 稅額: 總計 - (總計 / (1 + 稅率))
			// 總計 / (1 + 稅率)
			BigDecimal beforeTax = afterTax.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP);
			taxAmount = afterTax.subtract(beforeTax).setScale(0, RoundingMode.UP);
			finalSubtotal = afterTax.subtract(taxAmount);
		}

		// ====== 贈品門檻檢查 ======
		if (!giftProductIds.isEmpty()) { // 判斷贈品清單有沒有資料
			for (Integer giftId : giftProductIds) {
				// promotionGiftsDao 可以根據贈品 ID 查門檻
				BigDecimal giftRule = promotionsGiftsDao //
						.findFullAmountByGiftProductId(req.getPromotionsId(), giftId);

				if (giftRule != null) { // 如果有取得金額
					// 2. 直接拿 total 跟這個金額比
					if (subtotal.compareTo(giftRule) < 0) { // compareTo：這是 BigDecimal 比較大小的標準寫法
						throw new RuntimeException("金額未達門檻 " + giftRule + "，無法領取贈品 ID: " + giftId);
					}
				}
			}
		}

		String newId = ""; // 先宣告變數
		try {
			// ====== 產生新訂單編號 (悲觀鎖排隊入口) ======
			// 去資料庫找今天最後一筆訂單 (DAO 裡面要有 ORDER BY id DESC LIMIT 1)
			Optional<Orders> lastOrder = ordersDao.getOrderByOrderDateId(todayStr);
			int nextSeq = 1; // 預設從 1 開始
			if (lastOrder.isPresent()) {
				// 如果今天有訂單，把最大的序號轉成數字並 +1
				nextSeq = Integer.parseInt(lastOrder.get().getId()) + 1;
			}
			// 將數字格式化為 4 位字串，例如 1 變成 "0001"
			newId = String.format("%04d", nextSeq);

			// ====== 執行庫存扣除 ======
			// 依照排序後的 ID 逐一處理，每個產品 ID 只會執行一次資料庫更新
			for (int productId : sortedProductIds) {
				// 根據key(productId)，取得 使用者想買的數量(含贈品)
				int quantityToBuy = stockToReduceMap.get(productId);
				// 根據商品 id 分店 id 去商品表搜尋庫存
				Products product = productsDao.findById(productId);
				// 1. 查詢該分店的庫存快照
				BranchInventory inv = branchInventoryDao //
						.findByProductIdAndGlobalAreaId(productId, req.getGlobalAreaId()) //
						.orElseThrow(() -> new RuntimeException("找不到分店庫存資料"));
				// 庫存檢查：如果庫存 比要買的數量還少
				if (inv.getStockQuantity() < quantityToBuy) {
					// 拋出例外後，事務會自動回滾，前面扣掉的其他商品庫存也會還回去
					// return new CreateOrdersRes(ReplyMessage.STOCK_NOT_ENOUGH.getCode(),
					// ReplyMessage.STOCK_NOT_ENOUGH.getMessage());
					throw new RuntimeException("商品「" + product.getName() + "」庫存不足");
				}
				// 取得舊的版本號 (這就是你要帶入 SQL 的 ?3)
				int oldVersion = inv.getVersion();
				// 執行「手動樂觀鎖」更新
				int affectedRows = branchInventoryDao //
						.updateBranchStock(productId, req.getGlobalAreaId(), quantityToBuy, oldVersion);
				// 如果回傳 0，代表這期間 version 被動過，拋出異常觸發外層重試
				if (affectedRows == 0) {
					throw new RuntimeException("庫存版本衝突，準備重試");
				}
			}

			// ====== 執行贈品配額扣除 ======
			if (!giftProductIds.isEmpty()) {
				for (Integer giftId : giftProductIds) {
					// 執行原子扣除
					int affectedGiftRows = promotionsGiftsDao.reduceGiftQuota(req.getPromotionsId(), giftId);

					if (affectedGiftRows == 0) {
						// 代表這秒鐘剛好被別人領完了
						throw new RuntimeException("很抱歉，贈品已全數兌換完畢");
					}
				}
			}

			// ====== 執行新增主訂單 ======
			// 客戶端傳 paymentMethod=CASH 時，建立 PENDING_CASH（等現場收款）；其他一律 UNPAID
			String initialStatus = "CASH".equalsIgnoreCase(req.getPaymentMethod()) ? "PENDING_CASH" : "UNPAID";
			ordersDao.insert(newId, todayStr, req.getOrderCartId(), req.getGlobalAreaId(), req.getMemberId(),
					req.getPhone(), finalSubtotal, taxAmount, afterTax, initialStatus, req.isUseDiscount());

			// 成功後回傳結果
			return new CreateOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), //
					newId, todayStr, afterTax);
		} catch (Exception e) {
			// --- 錯誤紀錄 ---
			// logger.error(e.getMessage());
			System.out.println("executeInsert 執行失敗，準備回滾並交由外層判斷: " + e.getMessage());
			throw e;
		}
	}

	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態) */
	@Transactional(rollbackFor = Exception.class)
	public BasicRes pay(PayReq req) {
		// 參數檢查(有在Req那裏自動檢查)
		Orders order = ordersDao.getOrderByOrderDateIdAndId(req.getOrderDateId(), req.getId());
		if (order == null) {
			return new BasicRes(ReplyMessage.ORDER_NUMBER_NOT_FOUND.getCode(), //
					ReplyMessage.ORDER_NUMBER_NOT_FOUND.getMessage());
		}
		// 金額檢查: 使用 compareTo == 0 來比對 BigDecimal，避免 100.0 跟 100 判定不一致的問題
		if (req.getTotalAmount() != null && order.getTotalAmount().compareTo(req.getTotalAmount()) != 0) {
			throw new RuntimeException("支付金額與訂單金額不符！");
		}
		// 檢查訂單狀態：只有「未付款」的訂單可以執行付款
		// 如果訂單已經是 COMPLETED, 則不需要重複付款
		if (OrdersStatus.COMPLETED.equals(order.getStatus())) {
			return new BasicRes(ReplyMessage.SUCCESS.getCode(), "訂單已支付完成，無需重複操作");
		}
		// 如果訂單是其他狀態（如 REFUNDED, CANCELLED），則不允許付款
		if (!OrdersStatus.UNPAID.equals(order.getStatus()) && !OrdersStatus.PENDING_CASH.equals(order.getStatus())) {
			return new BasicRes(ReplyMessage.ORDERS_STATUS_ERROR.getCode(), "訂單狀態錯誤，無法付款");
		}
		try {
			// 新增(更新)的資料(付款方式、交易號碼、狀態)
			// updatePayAnyPending 允許 UNPAID 或 PENDING_CASH 的訂單付款
			int result = ordersDao.updatePayAnyPending(req.getId(), req.getOrderDateId(), //
					req.getPaymentMethod(), req.getTransactionId(), "COMPLETED");
			if (result > 0) {
				// ====== 會員邏輯(點數處理) ======
				// 判斷是會員(> 1)還是訪客(= 1)
				if (order.getMemberId() > 1) {
					// 利用會員id 撈取並鎖定會員資料(點數、9折卷)
					Members member = membersDao.findById(order.getMemberId());
					// 如果是 null 則代表沒有這筆會員資料
					if (member == null) {
						throw new RuntimeException(order.getMemberId() + "錯誤，查無會員資料");
					}
					int addResult = membersDao.addPoint(member.getId()); // 加次數(如果次數 < 9或 > 9 )
					if (addResult == 0) { // 加點失敗，可能次數剛好是 9
						// 次數剛好是9，加完變10 開券
						int couponResult = membersDao.reachFullPointsAndGiveCoupon(member.getId());
						if (couponResult == 0) {
							throw new RuntimeException("會員次數更新失敗");
						}
					}
					if (order.isUseDiscount()) { // 如果有使用優惠劵，須把它關閉、點數變1
						int updated = membersDao.useDiscount(member.getId());
						if (updated == 0) { // 避免客人有兩筆以上同時請求，可能原因: 狂點按鈕，網路延遲
							throw new RuntimeException("優惠券已被使用或不存在");
						}
					}
				}
				return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
			}
			throw new RuntimeException("付款更新失敗");
		} catch (Exception e) {
			throw new RuntimeException("付款失敗");
		}
	}

	/* 訂單狀態: 退款或取消 */
	@Transactional(rollbackFor = Exception.class)
	public BasicRes ordersStatus(RefundedReq req) {
		// 參數檢查(在req有做自動檢查)
		try {
			Orders order = ordersDao.getOrderByOrderDateIdAndId(req.getOrderDateId(), req.getId());
			// 找不到該筆訂單
			if (order == null) { // 找不到該筆訂單
				return new BasicRes(ReplyMessage.ORDER_NUMBER_NOT_FOUND.getCode(), //
						ReplyMessage.ORDER_NUMBER_NOT_FOUND.getMessage());
			}
			String oldStatus = order.getStatus().toString(); // 目前狀態
			String targetStatus = req.getStatus().toString(); // 目標狀態
			// 防呆：只有 COMPLETED 才能退款；只有 UNPAID 才能取消
			if (targetStatus.equalsIgnoreCase("REFUNDED") && !oldStatus.equals("COMPLETED")) {
				return new BasicRes(ReplyMessage.ORDERS_STATUS_ERROR.getCode(), //
						ReplyMessage.ORDERS_STATUS_ERROR.getMessage());
			}
			if (targetStatus.equalsIgnoreCase("CANCELLED") && !oldStatus.equals("UNPAID") && !oldStatus.equals("PENDING_CASH")) {
				return new BasicRes(ReplyMessage.ORDERS_STATUS_ERROR.getCode(), //
						ReplyMessage.ORDERS_STATUS_ERROR.getMessage());
			}

			// 執行訂單狀態更新
			int result = ordersDao.updateOrderStatus(req.getStatus(), req.getId(), req.getOrderDateId());
			// 判斷是否成功
			if (result > 0) {
				// 如果是退款要把次數退回(取消不用走這裡)
				if (targetStatus.equalsIgnoreCase("REFUNDED")) {
					// 如果是會員，次數扣回
					if (order.getMemberId() > 1) {
						Members member = membersDao.findById(order.getMemberId());
						if (member == null) {
							throw new RuntimeException("退款失敗：查無會員資料");
						}
						if (order.isUseDiscount()) {
							// 【情況 A】：當初有使用優惠券 -> 還原券並將次數設回 10
							membersDao.restoreCouponAndPoints(order.getMemberId());
						} else {
							// 【情況 B】：當初沒用優惠券 -> 正常扣回這單加的 1 次數
							membersDao.smartReducePoint(order.getMemberId());
						}
					}
					return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
				} else if (targetStatus.equalsIgnoreCase("CANCELLED")) {
					// 如果是會員且有使用優惠劵
					if (order.getMemberId() > 1 && order.isUseDiscount()) {
						// 這裡可以共用 restoreCouponAndPoints，把券設回 true 並點數設回 10
						membersDao.restoreCouponAndPoints(order.getMemberId());
					}
					return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
				}
			}
			throw new RuntimeException("更新訂單狀態失敗");
		} catch (Exception e) {
			throw new RuntimeException("退款流程發生錯誤: " + e.getMessage());
		}
	}

	/* 報電話號碼查詢今天的訂單 */
	public CreateOrdersRes getOrderByPhone(String phone) {
		String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		GetOrdersVo order = ordersDao.getOrderByPhone(todayStr, phone);
		return new CreateOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(),
				order.getId(), order.getOrderDateId(), order.getTotalAmount());
	}

	/* POS 看板：取得今日所有已付款訂單（含品項與廚房狀態）
	 * 查詢今天日期下所有 COMPLETED 的訂單，並把購物車明細組裝進來
	 */
	public GetTodayOrdersRes getTodayOrders() {
		String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		List<Orders> todayOrders = ordersDao.getTodayCompletedOrders(todayStr);
		List<TodayOrderVo> voList = new ArrayList<>();

		for (Orders order : todayOrders) {
			TodayOrderVo vo = new TodayOrderVo();
			vo.setId(order.getId());
			vo.setOrderDateId(order.getOrderDateId());
			vo.setTotalAmount(order.getTotalAmount());
			vo.setKitchenStatus(order.getKitchenStatus() != null ? order.getKitchenStatus() : "WAITING");
			vo.setPhone(order.getPhone());
			vo.setPaymentStatus(order.getStatus().toString()); // COMPLETED 或 PENDING_CASH

			// 從 order_cart_details 取出該訂單的商品明細
			List<TodayOrderDetailVo> items = new ArrayList<>();
			orderCartDetailsDao.findAllByCartId(order.getOrderCartId()).forEach(detail -> {
				TodayOrderDetailVo detailVo = new TodayOrderDetailVo();
				// 取商品名稱（products 表）
				Products _p = productsDao.findById(detail.getProductId());
				if (_p != null) detailVo.setProductName(_p.getName());
				detailVo.setQuantity(detail.getQuantity());
				detailVo.setGift(detail.isGift());
				items.add(detailVo);
			});
			vo.setItems(items);
			voList.add(vo);
		}
		return new GetTodayOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), voList);
	}

	/* POS 看板：更新廚房狀態（WAITING / COOKING / READY）*/
	@Transactional
	public BasicRes updateKitchenStatus(KitchenStatusReq req) {
		String status = req.getKitchenStatus();
		// 只允許合法的廚房狀態值
		if (!status.equals("WAITING") && !status.equals("COOKING") && !status.equals("READY")) {
			return new BasicRes(ReplyMessage.ORDERS_STATUS_ERROR.getCode(), "廚房狀態值無效，請傳入 WAITING / COOKING / READY");
		}
		int updated = ordersDao.updateKitchenStatus(req.getId(), req.getOrderDateId(), status);
		if (updated == 0) {
			return new BasicRes(ReplyMessage.ORDER_NOT_FOUND.getCode(), "找不到對應訂單");
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 客戶端輪詢：查詢自己訂單的廚房狀態（含 PENDING_CASH 待付款狀態） */
	public BasicRes getOrderStatus(String id, String orderDateId) {
		Orders order = ordersDao.getOrderByOrderDateIdAndId(orderDateId, id);
		if (order == null) {
			return new BasicRes(ReplyMessage.ORDER_NOT_FOUND.getCode(), "找不到對應訂單");
		}
		// 現金待付款：回傳特殊狀態讓前端顯示「請前往櫃台付款」
		if (OrdersStatus.PENDING_CASH.equals(order.getStatus())) {
			return new BasicRes(ReplyMessage.SUCCESS.getCode(), "PENDING_CASH");
		}
		// 已付款但廚房狀態尚未設定時，預設 WAITING
		String kitchenStatus = order.getKitchenStatus() != null ? order.getKitchenStatus() : "WAITING";
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), kitchenStatus);
	}
}
