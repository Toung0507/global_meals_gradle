package com.example.global_meals_gradle.service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.utils.EcpayUtils;

@Service
public class EcpayService {

	@Autowired
    private OrdersDao ordersDao;
	
	public String getEcpayForm(String orderDateId, String id) {
		
        Orders order = ordersDao.getOrderByOrderDateIdAndId(orderDateId, id);
        if (order == null) throw new RuntimeException("訂單不存在");

        // 建立一個 Map 存放所有綠界要求的 API 參數
        Map<String, String> params = new HashMap<>();
        params.put("MerchantID", "2000132");  // 商店編號 (測試用)
        params.put("MerchantTradeNo", orderDateId + id);  // 訂單編號 (唯一)
        // 交易時間: 格式需固定 "2026/04/12 12:00:00"
        params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss") //
        		.format(new java.util.Date())); 
        params.put("PaymentType", "aio");  // 交易類型 (固定為 aio)
        params.put("TotalAmount", String.valueOf(order.getTotalAmount().intValue()));  // 交易金額 (轉為整數)
        params.put("TradeDesc", "GlobalBau Food Delivery");  // 交易描述
        params.put("ChoosePayment", "Credit");  // 指定支付方式：信用卡
        params.put("ReturnURL", "https://你的外網網址/api/payment/callback");  // 付款成功後綠界通知後端的網址
        params.put("OrderResultURL", "https://你的外網網址/payment/success");  // 客人付完款後跳轉回來的網頁
        
        // 計算最難的 CheckMacValue
        // 設定加密用的 Key 和 IV (這兩個值非常重要，不可洩漏)
        String hashKey = "5294y06JbCWpE5vM"; // 綠界測試環境 Key
        String hashIV = "v77hoKGq4uF8dnAC";  // 綠界測試環境 IV
        // 呼叫 Utils 計算 CheckMacValue (簽章)
        String checkMacValue = EcpayUtils.generateCheckMacValue(hashKey, hashIV, params);
        // 將計算好的簽章放進參數 Map
        params.put("CheckMacValue", checkMacValue);

        // 使用 StringBuilder 組裝自動跳轉的 HTML Form
        StringBuilder html = new StringBuilder();
        // action 設為綠界測試環境的支付頁面地址
        html.append("<form id='payForm' action='https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5' method='post'>");
        // params 是一個 Map，裡面存了所有要給綠界的資料（key 是名稱，value 是值）
        params.forEach((key, value) -> {
        	// 這裡是在寫 HTML。每次跑這行，就會產生一行：
            // <input type='hidden' name='MerchantID' value='2000132'>
            // type='hidden' 就是讓這個框框在網頁上隱形
            html.append("<input type='hidden' name='").append(key).append("' value='").append(value).append("'>");
        });
        // 所有的參數都生出來後，寫上表單的結尾標籤
        html.append("</form>");
        // 加入 JavaScript 腳本，讓頁面載入後自動觸發表單送出 (submit)
        html.append("</form><script>document.getElementById('payForm').submit();</script>");
        
        return html.toString();
    }
}
