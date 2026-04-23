package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.utils.LinePayUtils;
import com.google.gson.Gson;

@Service
public class LinePayService {

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private OrdersService ordersService;

	@Value("${linepay.channel.id}")
	private String channelId;

	@Value("${linepay.channel.secret}")
	private String channelSecret;

	@Value("${payment.ngrok.url}")
	private String ngrokUrl;

	private static final String SANDBOX_BASE = "https://sandbox-api-pay.line.me";

	/** OrdersController 舊呼叫相容 */
	public String getLinePayLink(String orderDateId, String id) {
		return requestPayment(orderDateId, id);
	}

	/** OrdersController 舊呼叫相容（amount 由 service 自行查詢，忽略傳入值） */
	public void confirmPayment(String transactionId, int amount, String orderDateId, String id) {
		confirmPayment(transactionId, orderDateId + "||" + id);
	}

	/**
	 * 步驟一：向 LINE Pay 取得付款連結
	 * @return 付款頁面 URL
	 */
	public String requestPayment(String orderDateId, String id) {
		Orders order = ordersDao.getOrderByOrderDateIdAndId(orderDateId, id);
		int amount = order.getTotalAmount().intValue();

		// LINE Pay 要求唯一 orderId，用 || 分隔方便 confirm 時解析
		String linePayOrderId = orderDateId + "||" + id;

		Map<String, Object> product = new HashMap<>();
		product.put("id", "item_001");
		product.put("name", "懶飽飽點餐服務");
		product.put("quantity", 1);
		product.put("price", amount);

		Map<String, Object> packageObj = new HashMap<>();
		packageObj.put("id", "pkg_01");
		packageObj.put("amount", amount);
		packageObj.put("products", Collections.singletonList(product));

		Map<String, String> redirectUrls = new HashMap<>();
		redirectUrls.put("confirmUrl", ngrokUrl + "/lazybaobao/payment/linepay/confirm");
		redirectUrls.put("cancelUrl", ngrokUrl + "/payment/cancel");

		Map<String, Object> body = new HashMap<>();
		body.put("amount", amount);
		body.put("currency", "TWD");
		body.put("orderId", linePayOrderId);
		body.put("packages", Collections.singletonList(packageObj));
		body.put("redirectUrls", redirectUrls);

		String nonce = UUID.randomUUID().toString();
		String uri = "/v3/payments/request";
		String jsonBody = new Gson().toJson(body);
		String signature = LinePayUtils.encrypt(channelSecret, channelSecret + uri + jsonBody + nonce);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-LINE-ChannelId", channelId);
		headers.set("X-LINE-Authorization-Nonce", nonce);
		headers.set("X-LINE-Authorization", signature);

		HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(SANDBOX_BASE + uri, entity, Map.class);
			Map<String, Object> info = (Map<String, Object>) response.getBody().get("info");
			Map<String, String> paymentUrl = (Map<String, String>) info.get("paymentUrl");
			return paymentUrl.get("web");
		} catch (Exception e) {
			throw new RuntimeException("LINE Pay 請求失敗: " + e.getMessage());
		}
	}

	/**
	 * 步驟二：LINE Pay 重導向後，呼叫 Confirm API 完成扣款
	 * @param transactionId LINE Pay 附帶的交易序號
	 * @param linePayOrderId  格式 orderDateId||id
	 */
	public void confirmPayment(String transactionId, String linePayOrderId) {
		String[] parts = linePayOrderId.split("\\|\\|", 2);
		if (parts.length != 2) throw new RuntimeException("無效的 orderId 格式");
		String orderDateId = parts[0];
		String id = parts[1];

		Orders order = ordersDao.getOrderByOrderDateIdAndId(orderDateId, id);
		int amount = order.getTotalAmount().intValue();

		Map<String, Object> body = new HashMap<>();
		body.put("amount", amount);
		body.put("currency", "TWD");

		String nonce = UUID.randomUUID().toString();
		String uri = "/v3/payments/" + transactionId + "/confirm";
		String jsonBody = new Gson().toJson(body);
		String signature = LinePayUtils.encrypt(channelSecret, channelSecret + uri + jsonBody + nonce);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-LINE-ChannelId", channelId);
		headers.set("X-LINE-Authorization-Nonce", nonce);
		headers.set("X-LINE-Authorization", signature);

		HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(SANDBOX_BASE + uri, entity, Map.class);
			String returnCode = response.getBody().get("returnCode").toString();
			if (!"0000".equals(returnCode)) {
				throw new RuntimeException("LINE Pay 扣款失敗，代碼：" + returnCode);
			}

			PayReq req = new PayReq();
			req.setOrderDateId(orderDateId);
			req.setId(id);
			req.setPaymentMethod("LINEPAY");
			req.setTransactionId(transactionId);
			req.setTotalAmount(new BigDecimal(amount));
			ordersService.pay(req);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Confirm API 呼叫異常", e);
		}
	}
}
