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
import com.example.global_meals_gradle.service.EcpayService;
import com.example.global_meals_gradle.service.LinePayService;
import com.example.global_meals_gradle.service.OrdersService;

import jakarta.validation.Valid;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class OrdersController {

	@Autowired
	private OrdersService ordersService;
	
	@Autowired
	private EcpayService ecpayService;
	
	@Autowired
	private LinePayService linePayService;

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
	public CreateOrdersRes createOrdersRes(@Valid @RequestBody CreateOrdersReq req) {
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
	
	// 前端點擊「前往付款」時請求的 API
	@GetMapping("/goPay")
	public String goPay(@RequestParam String orderDateId, @RequestParam String id, @RequestParam String way) {
		// 判斷付款方式是否為綠界 (ECPAY) 還是LINE Pay
		if ("ECPAY".equalsIgnoreCase(way)) {
            // 執行綠界刷卡
            return ecpayService.getEcpayForm(orderDateId, id);
        } else if ("LINEPAY".equalsIgnoreCase(way)) {
        	// LINE Pay 回傳的是一個網址 (例如: https://pay-store.line.me/...)
            String payUrl = linePayService.getLinePayLink(orderDateId, id);
            // 這邊要注意：Controller 若要跳轉，不能只傳回字串，通常要用 redirect 或讓前端處理
            return "redirect:" + payUrl;
        }
        return "Unsupported payment method";
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
	
	/**
	 * LINE Pay 支付完成後跳轉回來的網址 (ConfirmUrl)
	 */
	@GetMapping("/linepay/confirm")
	public String linePayConfirm(
	    @RequestParam String transactionId, // LINE Pay 給的交易序號
	    @RequestParam String orderDateId,   // 我們自己傳過去的參數 (透傳)
	    @RequestParam String id,
	    @RequestParam int amount
	) {
	    try {
	        // 執行確認扣款
	        linePayService.confirmPayment(transactionId, amount, orderDateId, id);
	        
	        // 扣款成功後，將客人導向前端的成功頁面
	        return "redirect:https://your-frontend.com/payment-success";
	    } catch (Exception e) {
	        // 失敗則導向錯誤頁面
	        return "redirect:https://your-frontend.com/payment-fail";
	    }
	}
}
