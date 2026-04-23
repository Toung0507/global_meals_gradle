package com.example.global_meals_gradle.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.utils.EcpayUtils;

@Service
public class EcpayService {

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private OrdersService ordersService;

	@Value("${ecpay.merchant.id}")
	private String merchantId;

	@Value("${ecpay.hash.key}")
	private String hashKey;

	@Value("${ecpay.hash.iv}")
	private String hashIV;

	@Value("${payment.ngrok.url}")
	private String ngrokUrl;

	private static final String ECPAY_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

	/**
	 * 產生自動送出的 ECPay 付款表單 HTML
	 */
	public String getEcpayForm(String orderDateId, String id) {
		Orders order = ordersDao.getOrderByOrderDateIdAndId(orderDateId, id);
		if (order == null) throw new RuntimeException("訂單不存在");

		// MerchantTradeNo 最多 20 碼英數字
		String rawNo = (orderDateId + id).replaceAll("[^A-Za-z0-9]", "");
		String merchantTradeNo = rawNo.substring(0, Math.min(20, rawNo.length()));

		Map<String, String> params = new HashMap<>();
		params.put("MerchantID", merchantId);
		params.put("MerchantTradeNo", merchantTradeNo);
		params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		params.put("PaymentType", "aio");
		params.put("TotalAmount", String.valueOf(order.getTotalAmount().intValue()));
		params.put("TradeDesc", "GlobalBau_Food_Delivery");
		params.put("ItemName", "懶飽飽點餐服務");
		params.put("ChoosePayment", "Credit");
		// ReturnURL: ECPay server → backend (via Angular proxy)
		params.put("ReturnURL", ngrokUrl + "/lazybaobao/payment/ecpay/callback");
		// OrderResultURL: 付款後瀏覽器跳回前端
		params.put("OrderResultURL", ngrokUrl + "/payment/result");
		params.put("ClientBackURL", ngrokUrl + "/payment/cancel");
		// 備份 orderDateId/id，方便 callback 時取回
		params.put("CustomField1", orderDateId);
		params.put("CustomField2", id);

		String checkMacValue = EcpayUtils.generateCheckMacValue(hashKey, hashIV, params);
		params.put("CheckMacValue", checkMacValue);

		StringBuilder html = new StringBuilder();
		html.append("<form id='ecpayForm' action='").append(ECPAY_URL).append("' method='post'>");
		params.forEach((k, v) ->
			html.append("<input type='hidden' name='").append(k).append("' value='").append(v).append("'>")
		);
		html.append("</form>");
		html.append("<script>document.getElementById('ecpayForm').submit();</script>");
		return html.toString();
	}

	/**
	 * 處理 ECPay 付款結果通知（ReturnURL callback）
	 * ECPay 要求成功回傳 "1|OK"，失敗回傳 "0|error"
	 */
	public String handleCallback(Map<String, String> params) {
		// 複製成可修改的 Map，避免 remove 時出錯
		Map<String, String> mutableParams = new HashMap<>(params);
		String receivedCheckMac = mutableParams.remove("CheckMacValue");
		String expectedCheckMac = EcpayUtils.generateCheckMacValue(hashKey, hashIV, mutableParams);

		if (!expectedCheckMac.equalsIgnoreCase(receivedCheckMac)) {
			return "0|CheckMacValue 驗證失敗";
		}
		if (!"1".equals(mutableParams.get("RtnCode"))) {
			return "0|" + mutableParams.getOrDefault("RtnMsg", "付款失敗");
		}

		String orderDateId = mutableParams.getOrDefault("CustomField1", "");
		String id = mutableParams.getOrDefault("CustomField2", "");
		String tradeNo = mutableParams.getOrDefault("TradeNo", mutableParams.get("MerchantTradeNo"));

		if (orderDateId.isEmpty() || id.isEmpty()) {
			return "1|OK"; // 無法還原訂單 ID，但 ECPay 需要收到 1|OK
		}

		try {
			PayReq req = new PayReq();
			req.setOrderDateId(orderDateId);
			req.setId(id);
			req.setPaymentMethod("ECPAY");
			req.setTransactionId(tradeNo);
			ordersService.pay(req);
		} catch (Exception e) {
			// 記錄錯誤但仍回 1|OK，避免 ECPay 重複通知
			System.err.println("ECPay callback 訂單更新失敗: " + e.getMessage());
		}
		return "1|OK";
	}
}
