package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpSession;

@Service
public class LinePayService {

	@Autowired
    private OrdersDao ordersDao;
	
	@Autowired
	private OrdersService ordersService;
	
	// 這些是 LINE Pay 提供的開發者測試參數
    private String channelId = "你的ChannelID";  // LINE Pay 提供的識別碼
    private String channelSecret = "你的ChannelSecret";  // LINE Pay 提供的金鑰
    // 測試環境 API 路徑
    private String baseUrl = "https://sandbox-api-pay.line.me/v3/payments/request";

    public String getLinePayLink(String orderDateId, String id) {
    	// 抓取訂單
        Orders order = ordersDao.getOrderByOrderDateIdAndId(orderDateId, id);
        int amount = order.getTotalAmount().intValue();  // 轉成整數金額
        // 組裝給 LINE Pay 的 JSON 資料 (這裡使用 Map 比較快，也可以建 DTO)
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);  // 總金額
        body.put("currency", "TWD");  // 幣別：台幣
        body.put("orderId", orderDateId + id);  // 唯一訂單編號
        
        // 購物籃清單 (LINE Pay 要求必須要有產品清單)
        Map<String, Object> product = new HashMap<>();
        product.put("id", "item_001");  
        product.put("name", "GlobalBau 點餐服務");  // 顯示在手機上的名稱
        product.put("quantity", 1);  
        product.put("price", amount);
        
        Map<String, Object> packageObj = new HashMap<>();
        packageObj.put("id", "pkg_01");  // 包裝 ID
        packageObj.put("amount", amount);  // 此包裝總額
        packageObj.put("products", Collections.singletonList(product));  // 放入產品
        // 將單個包裝物件包裝成 List，放進 body 的 packages 欄位（LINE 要求一定要是清單格式）
        body.put("packages", Collections.singletonList(packageObj));
        
        // 設定跳轉網址 (付完錢或取消要跳去哪)
        Map<String, String> redirectUrls = new HashMap<>();
        redirectUrls.put("confirmUrl", "https://your-domain.com/api/payment/linepay/confirm"); // 後端確認扣款的 API
        redirectUrls.put("cancelUrl", "https://your-domain.com/payment/cancel"); // 客人取消支付
        // 將設定好的「確認網址」與「取消網址」放入 body 中
        body.put("redirectUrls", redirectUrls);

        // LINE Pay 最難的部分：計算 Header 簽章
        String nonce = UUID.randomUUID().toString();  // 隨機字串，防止重放攻擊
        String uri = "/v3/payments/request";  // API 的路徑
        String jsonBody = new Gson().toJson(body);  // 使用 Gson 轉成 JSON 字串
        // 簽章組合規則：Secret + URI + JSONBody + Nonce
        String signature = LinePayUtils.encrypt(channelSecret, channelSecret + uri + jsonBody + nonce);

        // 發送 HTTP Request (這裡推薦使用 RestTemplate 或 WebClient)
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);  // 告訴對方我是 JSON
        headers.set("X-LINE-ChannelId", channelId);  // 告訴 LINE 這筆請求是哪一個 Channel ID 發出的
        // 放入剛才產生的隨機字串（Nonce），確保這筆請求是唯一的，防止被駭客重複發送
        headers.set("X-LINE-Authorization-Nonce", nonce);
        // 放入最核心的「數位簽章（Signature）」，證明這封信真的是你本人發出的，且內容沒被改過
        headers.set("X-LINE-Authorization", signature);

        // 將「JSON 內容(body)」和「標籤(headers)」打包成一個完整的 HttpEntity（連線實體）
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        
        // 5. 發送請求並解析
        try {
        	// 使用 POST 方法將包裹寄到 requestUrl，並規定對方回傳的東西要轉成 Map 格式
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, entity, Map.class);
            
            // 拿到 webPaymentUrl，這是讓客人跳轉去支付的連結
            Map<String, Object> info = (Map<String, Object>) response.getBody().get("info");
            Map<String, String> paymentUrl = (Map<String, String>) info.get("paymentUrl");
            return paymentUrl.get("web"); // 回傳付款連結
        } catch (Exception e) {
            throw new RuntimeException("LINE Pay 請求失敗: " + e.getMessage());
        }
    }
    
    /**
     * 第二步：確認扣款 (Confirm API)
     */
    public void confirmPayment(String transactionId, int amount, String orderDateId, String id, HttpSession httpSession) {
        // 1. 準備請求 Body (只需金額與幣別)
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("currency", "TWD");

        // 2. 設定簽章參數
        String nonce = UUID.randomUUID().toString();
        // 注意：Confirm 的 URI 包含交易序號 transactionId
        String uri = "/v3/payments/" + transactionId + "/confirm";
        String jsonBody = new Gson().toJson(body);
        
        // 計算簽章
        String signature = LinePayUtils.encrypt(channelSecret, channelSecret + uri + jsonBody + nonce);

        // 3. 設定 Header
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-LINE-ChannelId", channelId);
        headers.set("X-LINE-Authorization-Nonce", nonce);
        headers.set("X-LINE-Authorization", signature);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        // 4. 發送請求
        try {
            String url = "https://sandbox-api-pay.line.me" + uri;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            // 檢查 LINE Pay 回傳碼 (0000 代表成功)
            if ("0000".equals(response.getBody().get("returnCode").toString())) {
                // 【重要】這一步成功了，錢才真的進來！
                // 在這裡呼叫你之前寫好的核心邏輯：加會員點數、改訂單狀態、核銷券
                PayReq req = new PayReq();
                req.setOrderDateId(orderDateId);
                req.setId(id);
                req.setPaymentMethod("LINEPAY");
                req.setTransactionId(transactionId);
                req.setTotalAmount(new BigDecimal(amount));
                
                ordersService.pay(req, httpSession); // 執行訂單完成後的業務邏輯
            } else {
                throw new RuntimeException("LINE Pay 扣款失敗，代碼：" + response.getBody().get("returnCode"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Confirm API 呼叫異常", e);
        }
    }
}
