package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.global_meals_gradle.dao.ExchangeRatesDao;

@Service
public class ExchangeRatesService {
	
	@Autowired
	ExchangeRatesDao exchangeRatesDao;
	
	// 這裡填入您的 API Key
    private String API_KEY = "YOUR_API_KEY";

    public void fetchAndSaveRates() {
//        String url = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/TWD";
//        
//        // 1. 建立 RestTemplate
//        RestTemplate restTemplate = new RestTemplate();
//        
//        try {
//            // 2. 發送 GET 請求並取得結果
//            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//            Map<String, Object> body = response.getBody();
//
//            if (body != null && body.get("result").equals("success")) {
//                // 3. 取得 conversion_rates 節點
//                Map<String, Object> rates = (Map<String, Object>) body.get("conversion_rates");
//
//                // 4. 傳統方式遍歷 Map (不使用 Lambda)
//                for (Map.Entry<String, Object> entry : rates.entrySet()) {
//                    String currencyCode = entry.getKey();
//                    // 將 Object 轉為 Double (處理可能的 Integer 類型)
//                    Double rate = Double.parseDouble(entry.getValue().toString());
//
//                    // 5. 存入 exchange_rates 表
//                    exchangeRateDao.updateExchangeRate(currencyCode, rate);
//
//                    // 6. 同步更新 regions 表
//                    regionDao.updateRegionRate(currencyCode, rate);
//                }
//                System.out.println("匯率同步完成：" + new Date());
//            }
//        } catch (Exception e) {
//            System.out.println("匯率抓取失敗：" + e.getMessage());
//        }
    }

}
