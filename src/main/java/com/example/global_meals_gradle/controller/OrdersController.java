package com.example.global_meals_gradle.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CreateOrdersReq;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.req.RefundedReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.CreateOrdersRes;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.service.OrdersService;

import jakarta.validation.Valid;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class OrdersController {

	@Autowired
	private OrdersService ordersService;

	/* 取的該會員的歷史訂單 */
	@PostMapping("orders/get_all_orders_list")
	public GetAllOrdersRes GetAllOrdersList(@RequestBody HistoricalOrdersReq req) {
		return ordersService.getAllOrders(req);
	}

	/* 更改訂單狀態: 退款 REFUNDED 或取消 CANCELLED */
	@PostMapping("orders/orders_status")
	public BasicRes ordersStatus(@Valid @RequestBody RefundedReq req) {
		return ordersService.ordersStatus(req);
	}

	/* 成立訂單(未結帳) */
	@PostMapping("orders/create_orders")
	public CreateOrdersRes createOrdersRes(@RequestBody CreateOrdersReq req) {
		return ordersService.createOrders(req);
	}

	/* 現金付款成功 */
	@PostMapping("orders/pay")
	public BasicRes pay(@Valid @RequestBody PayReq req) {
		return ordersService.pay(req);
	}

	/* 報電話號碼取餐 */
	@GetMapping("orders/get_order_by_phone")
	public CreateOrdersRes getOrderByPhone(@RequestParam("phone") String phone) {
		return ordersService.getOrderByPhone(phone);
	}
	
	// 發起支付（產生刷卡頁面)
	public String goPay(String orderId, BigDecimal totalAmount) {
	    // 這裡模擬串接綠界或類似金流的 SDK
	    // 實際開發時，你會帶入 MerchantID, HashKey, HashIV
	    
	    StringBuilder html = new StringBuilder();
	    html.append("<form id='payForm' action='https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5' method='post'>");
	    html.append("<input type='hidden' name='MerchantID' value='2000132'>");
	    html.append("<input type='hidden' name='MerchantTradeNo' value='" + orderId + "'>"); // 你的訂單編號
	    html.append("<input type='hidden' name='TotalAmount' value='" + totalAmount.intValue() + "'>");
	    html.append("<input type='hidden' name='ReturnURL' value='https://你的外網網址/api/payment/callback'>"); // 付款成功通知這台 Server
	    html.append("<input type='hidden' name='CheckMacValue' value='加密後的字串'>"); // 這是最難的部分，SDK會幫你算
	    html.append("</form>");
	    html.append("<script>document.getElementById('payForm').submit();</script>");
	    
	    return html.toString();
	}

	/* 接收金流公司傳的付款成功通知 */
	@PostMapping("/api/payment/callback")
	public String handlePaymentNotify(@RequestParam Map<String, String> params) {
	    // 取得金流公司傳回來的結果 (RtnCode 是綠界的標準)
	    String rtnCode = params.get("RtnCode"); 
	    // 注意：因為我們在發起支付時，MerchantTradeNo 通常會加隨機碼防止重複 (如: 202604110001T123)
	    String merchantTradeNo = params.get("MerchantTradeNo"); 

	    if ("1".equals(rtnCode)) {
	        // 拆解編號：這裡要根據你當時「丟出去」的格式來拆
	        // 假設前 8 位是日期 (20260411)，接著 4 位是序號 (0001)
	        String orderDateId = merchantTradeNo.substring(0, 8);
	        // 如果你有加隨機碼，就用 split 拆開，只拿前面的序號
	        String id = merchantTradeNo.substring(8).split("T")[0];
	        
	        // 取得綠界回傳的原始支付類型 (例如: Credit_Card, LINEPAY, WebATM)
	        String ecpayPaymentType = params.get("PaymentType");
	        String myPaymentMethod = "UNKNOWN";  
	        // 轉換成你系統定義的關鍵字
	        if (ecpayPaymentType.contains("Credit")) {
	            myPaymentMethod = "CREDIT_CARD";
	        } else if (ecpayPaymentType.contains("LINEPAY")) {
	            myPaymentMethod = "LINE_PAY";
	        } else {
	            myPaymentMethod = "ONLINE_OTHER"; // 其他線上支付
	        }
	        // 綠界回傳的欄位名稱通常是 "TradeAmt"(付款金額)
	        String tradeAmt = params.get("TradeAmt");
	        // 封裝成 PayReq 物件，符合 Service 的參數要求
	        PayReq req = new PayReq();
	        req.setOrderDateId(orderDateId);
	        req.setId(id);
	        req.setPaymentMethod(myPaymentMethod); // 付款方式
	        req.setTransactionId(params.get("TradeNo")); // 綠界的交易流水號
	        if (tradeAmt != null) {
	            req.setTotalAmount(new BigDecimal(tradeAmt));
	        }
	        // 呼叫「收錢結案」的共用 Service
	        // 不論現金或刷卡，收完錢都是跑這一支
	        BasicRes res = ordersService.pay(req);

	        if (res.getCode() == 200) {
	            return "1|OK"; // 綠界要求：成功必須回傳 1|OK，不然它會間隔發送通知直到 24 小時
	        }
	    }
	    return "0|Fail"; // 告訴金流公司處理失敗
	}
}
