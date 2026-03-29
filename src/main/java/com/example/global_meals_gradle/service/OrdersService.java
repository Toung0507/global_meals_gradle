package com.example.global_meals_gradle.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.OrdersStatus;
import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.OrderCartDetailsDao;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.entity.OrderCartDetails;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.req.CreateOrdersReq;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.req.RefundedReq;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.CreateOrdersRes;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.res.GetOrdersDetailVo;
import com.example.global_meals_gradle.res.GetOrdersVo;

@Service
public class OrdersService {

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private OrderCartDetailsDao orderCartDetailsDao;

	@Autowired
	private ProductsDao productsDao;

	// 這樣在呼叫 self.executeInsert 時，Spring 才會啟動 @Transactional 的代理機制。
	@Autowired
	@Lazy // 加上 @Lazy 避免某些 Spring 版本出現循環依賴的警告
	private OrdersService self;

	/* 查詢歷史訂單 */
	@Transactional(readOnly = true) // 只有查詢，寫這段對效能比較好
	public GetAllOrdersRes getAllOrders(HistoricalOrdersReq req) {
		/* 判斷會員id是否存在 */

		try {
			/* 取得主表(Orders)該會員的歷史訂單 */
			List<GetOrdersVo> orderList = ordersDao.getOrderByMemberId(req.getMemberId());
			/* 第一層迴圈跑每一筆訂單拿到各自的明細 */
			for (GetOrdersVo order : orderList) {
				// 根據購物車id，拿到該訂單的明細
				List<OrderCartDetails> detailEntities = orderCartDetailsDao
						.getProductByOrderCartId(order.getOrderCartId());
				// 要把上面拿到的資料，放進vo裡
				List<GetOrdersDetailVo> detailVoList = new ArrayList<>();
				/* 第二層迴圈，利用產品id 去products表取得產品名稱 */
				for (OrderCartDetails entity : detailEntities) {
					GetOrdersDetailVo vo = new GetOrdersDetailVo();
					// 產品id 去products表取得產品名稱
					String productName = productsDao.getProductsNameById(entity.getProductId());
					// 把各個資料塞進去vo
					vo.setName(productName); // 塞進去上個步驟取的名子
					vo.setQuantity(entity.getQuantity());
					vo.setPrice(entity.getPrice());
					vo.setGift(entity.isGift());
					vo.setDiscountNote(entity.getDiscountNote());
					// 把整理好的資料裝進voList裡
					detailVoList.add(vo);
				}
				// 把整理好的訂單明細，塞進去欄位
				order.setGetOrdersDetailVoList(detailVoList);
			}
			return new GetAllOrdersRes(ReplyMessage.SUCCESS.getCode(), //
					ReplyMessage.SUCCESS.getMessage(), orderList);
		} catch (Exception e) {
			throw e;
		}
	}

	/* 成立訂單: 外部呼叫的主入口：負責「高併發重試流程」 */
	// 這個方法「不加」@Transactional，這樣裡面的 try-catch 才能重複執行。
	public CreateOrdersRes createOrders(CreateOrdersReq req) {
		// 參數檢查

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
					Thread.currentThread().interrupt();  // 恢復中斷狀態
				}
			}
		}
		return null;
	}

	/* 成立訂單: 內部執行方法：負責「查詢最大序號 + 寫入資料庫」。 */
	// 加上 @Transactional，確保這段動作在資料庫中是原子性的（要嘛全成功，要嘛全失敗）。
	@Transactional(rollbackFor = Exception.class)
	public CreateOrdersRes executeInsert(CreateOrdersReq req, String todayStr) {

		// 1. 去資料庫找今天最後一筆訂單 (DAO 裡面要有 ORDER BY id DESC LIMIT 1)
		Optional<Orders> lastOrder = ordersDao.getOrderByOrderDateId(todayStr);

		int nextSeq = 1; // 預設從 1 開始
		if (lastOrder.isPresent()) {
			// 2. 如果今天有訂單，把最大的序號轉成數字並 +1
			nextSeq = Integer.parseInt(lastOrder.get().getId()) + 1;
		}

		// 3. 將數字格式化為 5 位字串，例如 1 變成 "00001"
		String newId = String.format("%05d", nextSeq);

		// 4. 執行新增主訂單 (使用你在圖 3 寫的 DAO insert 方法)
		ordersDao.insert(newId, todayStr, req.getOrderCartId(), req.getGlobalAreaId(), req.getMemberId(),
				req.getSubtotalBeforeTax(), req.getTaxAmount(), req.getTotalAmount());

		// 5. 成功後回傳結果
		return new CreateOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 付款完成新增(更新)的資料(付款方式、交易號碼、狀態) */
	public BasicRes pay(PayReq req) {
		// 參數檢查

		// 新增(更新)的資料(付款方式、交易號碼、狀態) (status 在 req 是設 ENUM，在 DAO 是設 String，所以要加 name() )
		ordersDao.updatePay(req.getId(), req.getOrderDateId(), req.getPaymentMethod(), req.getTransactionId(),
				req.getStatus().name());
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 訂單狀態: 退款或取消 */
	public BasicRes ordersStatus(RefundedReq req) {
		// 參數檢查

		// 將前端傳來的字串轉成 Enum 物件(req 是設 String) toUpperCase: 避免大小寫問題
		OrdersStatus targetStatus = OrdersStatus.valueOf(req.getStatus().toUpperCase());
		// 執行訂單狀態更新
		int result = ordersDao.updateOrderStatus(targetStatus.name(), req.getId(), req.getOrderDateId());
		// 判斷是否成功
		if (result > 0) {
			return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
		} else {
			return new BasicRes(ReplyMessage.ORDER_NOT_FOUND.getCode(), ReplyMessage.ORDER_NOT_FOUND.getMessage());
		}
	}
}
