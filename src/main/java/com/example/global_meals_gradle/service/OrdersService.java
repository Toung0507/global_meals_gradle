package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.example.global_meals_gradle.dao.MembersDao;
import com.example.global_meals_gradle.dao.OrderCartDetailsDao;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.dao.PromotionsGiftsDao;
import com.example.global_meals_gradle.entity.Members;
import com.example.global_meals_gradle.entity.OrderCartDetails;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.req.CreateOrdersReq;
import com.example.global_meals_gradle.req.DiscountReq;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.req.RefundedReq;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.CreateOrdersRes;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.res.GetOrdersDetailVo;
import com.example.global_meals_gradle.res.GetOrdersVo;
import com.example.global_meals_gradle.res.TotalAmountRes;

/*	待做:
 * 	成立訂單那邊的庫存需不需要以分店做區分;已經有未稅金額，但還需要做稅率跟含稅總金額
 */

@Service
public class OrdersService {

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private OrderCartDetailsDao orderCartDetailsDao;

	@Autowired
	private ProductsDao productsDao;

	@Autowired
	private MembersDao membersDao;

	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;

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
			detail.setQuantity(((Number) row[3]).intValue()); // 如果是寫 Integer ，DB 回傳是：BigInteger/Long，會直接噴
																// ClassCastException
			detail.setPrice((BigDecimal) row[4]);
			detail.setGift((Boolean) row[5]);
			detail.setDiscountNote((String) row[6]);
			detail.setName((String) row[7]); // 產品名稱已經在 SQL 抓好了

			vo.getGetOrdersDetailVoList().add(detail);
		}
		// 把 Map 的值轉換成 List
		List<GetOrdersVo> result = new ArrayList<>(orderMap.values());

		return new GetAllOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), result);
	}

	/* 成立訂單: 外部呼叫的主入口：負責「高併發重試流程」 */
	// 這個方法「不加」@Transactional，這樣裡面的 try-catch 才能重複執行。
	public CreateOrdersRes createOrders(CreateOrdersReq req) {
		// 參數檢查(庫存檢查、金額檢查)

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
			} catch (Exception e) {
				// 發生衝突了！(例如主鍵重複)
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
			}
		}
		return null;
	}

	/* 成立訂單: 內部執行方法：負責「查詢庫存 + 查詢最大序號 + 寫入資料庫」。 */
	// 加上 @Transactional，確保這段動作在資料庫中是原子性的（要嘛全成功，要嘛全失敗）。
	@Transactional(rollbackFor = Exception.class)
	public CreateOrdersRes executeInsert(CreateOrdersReq req, String todayStr) {
		// ====== 產生新訂單編號 (悲觀鎖排隊入口) ======
		// 去資料庫找今天最後一筆訂單 (DAO 裡面要有 ORDER BY id DESC LIMIT 1)
		Optional<Orders> lastOrder = ordersDao.getOrderByOrderDateId(todayStr);
		int nextSeq = 1; // 預設從 1 開始
		if (lastOrder.isPresent()) {
			// 如果今天有訂單，把最大的序號轉成數字並 +1
			nextSeq = Integer.parseInt(lastOrder.get().getId()) + 1;
		}
		// 將數字格式化為 4 位字串，例如 1 變成 "0001"
		String newId = String.format("%04d", nextSeq);

		// ====== 處理商品與扣庫存 (樂觀鎖) ======
		// 初始化總金額 (使用 BigDecimal.ZERO 確保精準度)
		BigDecimal total = BigDecimal.ZERO;
		// 迴圈計算總額時，順便把「所有的贈品 ID」存進這個清單
		List<Integer> giftProductIds = new ArrayList<>();
		// 取的購物車清單
		List<OrderCartDetails> cartDetailsList = req.getOrderCartDetailsList();
		// [防止死鎖]：將商品按照 ID 從小到大排序 Comparator.comparingInt 用來建立比較器，依照某個「int 型別的 key」來排序。
		cartDetailsList.sort(Comparator.comparingInt(o -> o.getProductId()));
		for (OrderCartDetails detail : cartDetailsList) {
			// 取得 商品id 與 使用者想買的數量
			int productId = detail.getProductId();
			int quantityToBuy = detail.getQuantity();
			// 根據商品 id 去商品表搜尋庫存並鎖定該資料，避免超賣
			Products product = productsDao.findById(productId);
			// 安全檢查：萬一資料庫找不到這個商品 ID
			if (product == null) {
				// return 不會觸發回滾，要用 throw
				// 假設購物車有 A、B 兩個商品。先扣了 A 的庫存，接著檢查 B 時發現庫存不足，你用了 return。這時，A
				// 的庫存已經被扣掉了，且不會還原，但訂單卻沒成立。這就是所謂的「資料不一致」。
				// return new CreateOrdersRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(),
				// ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
				throw new RuntimeException("商品編號 " + productId + " 不存在");
			}
			// 庫存檢查：如果庫存 比要買的數量還少
			if (product.getStockQuantity() < quantityToBuy) {
				// 拋出例外後，事務會自動回滾，前面扣掉的其他商品庫存也會還回去
				// return new CreateOrdersRes(ReplyMessage.STOCK_NOT_ENOUGH.getCode(),
				// ReplyMessage.STOCK_NOT_ENOUGH.getMessage());
				throw new RuntimeException("商品「" + product.getName() + "」庫存不足");
			}
			// 取得舊的版本號 (這就是你要帶入 SQL 的 ?3)
			int oldVersion = product.getVersion();
			// 計算扣除後的庫存 (這就是 ?2)
			int newStock = product.getStockQuantity() - detail.getQuantity();
			// 執行「手動樂觀鎖」更新
			int affectedRows = productsDao.updateStockWithOptimisticLock(product.getId(), newStock,
					product.getVersion());
			// 如果回傳 0，代表這期間 version 被動過，拋出異常觸發外層重試
			if (affectedRows == 0) {
				throw new RuntimeException("庫存版本衝突，準備重試");
			}

			// ====== 金額計算/贈品id儲存 ======
			// 不是贈品的才要計算金額 / 贈品的Id要存進贈品清單
			if (!detail.isGift()) {
				// 將數量轉為 BigDecimal 後與單價相乘，累加到 total
				BigDecimal itemPrice = product.getBasePrice();
				BigDecimal itemQty = BigDecimal.valueOf(quantityToBuy);
				total = total.add(itemPrice.multiply(itemQty)); // BigDecimal 是不可變的，加完後的結果必須重新賦值給 total。
			} else {
				giftProductIds.add(detail.getProductId());
			}
		}

		// ====== 贈品門檻檢查 ======
		if (!giftProductIds.isEmpty()) { // 判斷贈品清單有沒有資料
			for (Integer giftId : giftProductIds) {
				// promotionGiftsDao 可以根據贈品 ID 查門檻
				BigDecimal giftRule = promotionsGiftsDao.findFullAmountByGiftProductId(giftId);

				if (giftRule != null) { // 如果有取得金額
					// 2. 直接拿 total 跟這個金額比
					if (total.compareTo(giftRule) < 0) { // compareTo：這是 BigDecimal 比較大小的標準寫法
						throw new RuntimeException("金額未達門檻 " + giftRule + "，無法領取贈品 ID: " + giftId);
					}
				}
			}
		}

		// ====== 會員邏輯(點數處理) ======
		// 判斷是會員(> 1)還是訪客(= 1)
		if (req.getMemberId() > 1) {
			// 利用會員id 撈取並鎖定會員資料(點數、9折卷)
			Members member = membersDao.findById(req.getMemberId());
			// 如果是 null 則代表沒有這筆會員資料
			if (member != null) {
				// 判斷有無9折劵，如果沒有，可以集點
				if (member.isDiscount() == false) {
					// 取得目前會員點數
					int memberPoints = member.getOrderCount();
					// 已有沒有9點做判斷，如果已有9點，那要多加一個開啟9折劵的程式
					if (memberPoints < 9) {
						membersDao.addPoint(member.getId());
					} else {
						membersDao.reachFullPointsAndGiveCoupon(member.getId());
					}
				}
			}
		}

		// ====== 執行新增主訂單 ======
		ordersDao.insert(newId, todayStr, req.getOrderCartId(), req.getGlobalAreaId(), req.getMemberId(),
				req.getPhone(), total, req.getTaxAmount(), req.getTotalAmount());

		// 成功後回傳結果
		return new CreateOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), //
				newId, todayStr, req.getTotalAmount());
	}

	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態、如果有使用優惠劵，則總金額需更改與優惠劵須關閉與次數變為1) */
	public BasicRes pay(PayReq req) {
		// 參數檢查(有在Req那裏自動檢查)
		Orders order = ordersDao.getOrderByOrderDateIdAndId(req.getOrderDateId(), req.getId());
		if (order == null) {
			return new BasicRes(ReplyMessage.ORDER_NUMBER_NOT_FOUND.getCode(), //
					ReplyMessage.ORDER_NUMBER_NOT_FOUND.getMessage());
		}
		// 判斷有無使用優惠劵
		if (!req.isUseDiscount()) { // 如果沒有，就只要更新 付款方式、交易號碼、狀態
			if (order.getTotalAmount() != req.getTotalAmount()) { // 沒有使用優惠劵的情況下，兩者要相等
				return new BasicRes(ReplyMessage.TOTAL_AMOUNT_ERROR.getCode(), //
						ReplyMessage.TOTAL_AMOUNT_ERROR.getMessage());
			}
			// 新增(更新)的資料(付款方式、交易號碼、狀態)
			ordersDao.updatePayNotUseDiscount(req.getId(), req.getOrderDateId(), req.getPaymentMethod(), //
					req.getTransactionId(), "COMPLETED");
			return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
		}
		Members member = membersDao.findById(order.getMemberId());
		if (!member.isDiscount()) { // 如果優惠劵的開關是關閉的，那沒有優惠劵可以使用
			return new BasicRes(ReplyMessage.NOT_DISCOUNT_ERROR.getCode(), //
					ReplyMessage.NOT_DISCOUNT_ERROR.getMessage());
		}
		membersDao.useDiscount(order.getMemberId()); // 做更改(次數變一，優惠劵關閉)
		ordersDao.updatePayUseDiscount(req.getId(), req.getOrderDateId(), req.getPaymentMethod(), //
				req.getTransactionId(), "COMPLETED", req.getTotalAmount());
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 訂單狀態: 退款或取消 */
	@Transactional(rollbackFor = Exception.class)
	public BasicRes ordersStatus(RefundedReq req) {
		// 參數檢查(在req有做自動檢查)
		try {
			Orders order = ordersDao.getOrderByOrderDateIdAndId(req.getOrderDateId(), req.getId());
			// 找不到該筆訂單
			if (order == null) { // 找不到該筆訂單
				return new GetAllOrdersRes(ReplyMessage.ORDER_NUMBER_NOT_FOUND.getCode(), //
						ReplyMessage.ORDER_NUMBER_NOT_FOUND.getMessage());
			}
			// 訂單狀態錯誤
			if (!order.getStatus().equals(OrdersStatus.COMPLETED)) {
				return new GetAllOrdersRes(ReplyMessage.ORDERS_STATUS_ERROR.getCode(), //
						ReplyMessage.ORDERS_STATUS_ERROR.getMessage());
			}
			// 執行訂單狀態更新
			int result = ordersDao.updateOrderStatus(req.getStatus(), req.getId(), req.getOrderDateId());
			// 判斷是否成功
			if (result > 0) {
				// 如果是會員，點數扣回
				if (order.getMemberId() > 1) {
					Members member = membersDao.findById(order.getMemberId());
					if (member == null) {
						throw new RuntimeException("退款失敗：查無會員資料");
					}
					if (member.getOrderCount() > 10) {
						membersDao.reducePoint(order.getMemberId());
					} else {
						membersDao.reducePointClose(order.getMemberId());
					}
				}
				return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
			} else {
				throw new RuntimeException("退款失敗");
			}

		} catch (Exception e) {
			throw new RuntimeException("退款失敗");
		}
	}

	/* 報電話號碼查詢最新一單 */
	public CreateOrdersRes getOrderByPhone(String phone) {
		// 取的今天的日期字串，參考成立訂單
		String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		// 取的資料根據電話號碼跟今天日期
		GetOrdersVo order = ordersDao.getOrderByPhone(todayStr, phone);

		return new CreateOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), //
				order.getId(), order.getOrderDateId(), order.getTotalAmount());
	}
	
	/* 判斷有無要使用優惠劵 */  // 這目前用不到，改成前端判斷處理有沒有使用優惠劵的判斷
	@Transactional(rollbackFor = Exception.class)
	public TotalAmountRes useDiscount(DiscountReq req) {
		try {
			Orders order = ordersDao.getOrderByOrderDateIdAndId(req.getOrderDateId(), req.getId());
			if (!req.isUseDiscount()) { // 判斷有無要使用優惠劵
				return new TotalAmountRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
			}
			if (order.getMemberId() <= 1) { // 判斷是訪客
				return new TotalAmountRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
			}
			/* 這裡應該不用做判斷?因為已經交易完成友產生交易序號了
			Members member = membersDao.findById(order.getMemberId());
			if (!member.isDiscount()) { // 判斷該會員有無優惠劵
				return new TotalAmountRes(ReplyMessage.DISCOUNT_ERROR.getCode(),
						ReplyMessage.DISCOUNT_ERROR.getMessage());
			}
			*/
			membersDao.useDiscount(order.getMemberId()); // 做更改(次數變一，優惠劵關閉)
			BigDecimal total = order.getTotalAmount();
			total = total.multiply(new BigDecimal("0.8")); // 總額打8折
			// 無條件進位到整數
			// setScale(0) 代表保留 0 位小數
			total = total.setScale(0, RoundingMode.UP);
			ordersDao.upDateTotalAmount(req.getId(), req.getOrderDateId(), total);

			return new TotalAmountRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), total);
		} catch (Exception e) {
			throw new RuntimeException("無法使用優惠劵");
		}
	}
}
