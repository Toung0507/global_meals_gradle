package com.example.global_meals_gradle.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.example.global_meals_gradle.dao.ExchangeRatesDao;

@Service
public class ExchangeRatesService {

	@Autowired
	ExchangeRatesDao exchangeRatesDao;

	// 這裡注入的就是 RestClientConfig 產生的那個 Bean
	@Autowired
	RestClient restClient;

	/* 如果要從設定檔中取值，要用 @Value ，以及後面的字串格式 "${設定檔中寫的變數}" */
	// 這裡填入您的 API Key
	@Value(value = "${rate.api.key}")
	private String apiKey;

	@Transactional(rollbackFor = Exception.class)
	public void saveRates() {
		/* RestClient版 */
		// 設定 API 請求網址 (以台幣 TWD 為基準)
		String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/TWD";

		Set<String> targetCurrency = new HashSet<>(Arrays.asList("JPY", "KRW"));

		try {
			// 2. 直接使用注入的 restClient，不需要再 new RestClient()
			@SuppressWarnings("unchecked")
			Map<String, Object> response = restClient.get().uri(url).retrieve()//
					.onStatus(
							// 3-1. 實作 Predicate 介面：用來決定要不要攔截這個狀態碼
							// 3-2. 實作 ResponseErrorHandler 介面：定義攔截到錯誤後要做什麼
							new Predicate<HttpStatusCode>() {
								@Override
								public boolean test(HttpStatusCode status) {
									return status.isError(); // 檢查是否為 4xx 或 5xx
								}
							}, //
							new RestClient.ResponseSpec.ErrorHandler() {
								@Override
								public void handle(HttpRequest request, ClientHttpResponse res) throws IOException {
									throw new RuntimeException("匯率 API 回應錯誤，狀態碼：" + res.getStatusCode());
								}
							})
					.body(Map.class); // 4. 將 JSON 回應內容轉換為 Map
			// 3. 設定錯誤處理邏輯(這段是 lambda 表達式寫法)
//                    .onStatus(HttpStatusCode::isError, (request, res) -> {
//                        throw new RuntimeException("匯率 API 回應錯誤，狀態碼：" + res.getStatusCode());
//                    })

			if (response != null && "success".equals(response.get("result"))) {
				// 5. 取得 conversion_rates 節點下的所有幣別數據
				@SuppressWarnings("unchecked")
				Map<String, Object> rates = (Map<String, Object>) response.get("conversion_rates");

				if (rates != null) {
					for (Map.Entry<String, Object> entry : rates.entrySet()) {
						String currencyCode = entry.getKey();
						if (targetCurrency.contains(currencyCode)) {
							// 只有符合 JPY 或 KRW 的資料才會進入這裡
							// 確保數值轉換為 BigDecimal 以維持精確度
							BigDecimal rate = new BigDecimal(entry.getValue().toString());
							// 6. 呼叫 DAO 執行 Upsert (新增或更新匯率)
							exchangeRatesDao.upserRate(currencyCode, rate);
							;

							System.out.println("已更新目標匯率: " + currencyCode + " = " + rate);
						}
					}
					System.out.println("匯率數據更新成功，更新時間：" + LocalDateTime.now());
				}
			}
		} catch (Exception e) {
			// 在這裡可以加入日誌系統記錄錯誤 (如 Log4j 或 Slf4j)
			throw e;
		}
		// =======================================================================================================
		/* RestTemplate版 */
//    	// 1. 初始化 RestTemplate 實例
//        RestTemplate restTemplate = new RestTemplate();
//        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/TWD";
//        
//        try {
//            // 2. 發送 GET 請求。getForEntity 會將 JSON 自動轉為 ResponseEntity 封裝物件
//            // Map.class 代表我們預期回傳的是一個鍵值對結構
//            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//            
//            // 3. 從回應主體 (Body) 中取出 conversion_rates 這個層級的資料
//            Map<String, Object> body = response.getBody();
//            if (body != null && "success".equals(body.get("result"))) {
//                Map<String, Object> rates = (Map<String, Object>) body.get("conversion_rates");
//                
//                // 4. 遍歷 Map 取得各國幣別與匯率
//                for (Map.Entry<String, Object> entry : rates.entrySet()) {
//                    // 將數值轉換為精確的 BigDecimal 並透過 DAO 執行 Upsert (新增或更新)
//                    BigDecimal rateValue = new BigDecimal(entry.getValue().toString());
//                    exchangeRatesDao.upsertRate(entry.getKey(), rateValue);
//                }
//            }
//        } catch (Exception e) {
//            // 5. 錯誤處理：例如網路超時或 API 金鑰錯誤
//            System.err.println("RestTemplate 抓取失敗: " + e.getMessage());
//        }
		// =======================================================================================================
		/* WebClient版 */
//    	// 1. 建立 WebClient 客戶端
//        WebClient webClient = WebClient.create();
//        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/TWD";
//
//        try {
//            // 2. 定義請求方式為 GET 並指定 URL
//            Map<String, Object> response = webClient.get()
//                    .uri(url)
//                    .retrieve() // 3. 執行檢索動作
//                    .bodyToMono(Map.class) // 4. 將回傳流轉換為單一物件 (Mono)
//                    .block(); // 5. 關鍵：在排程任務中，我們使用 block() 強制同步等待結果
//
//            if (response != null && "success".equals(response.get("result"))) {
//                Map<String, Object> rates = (Map<String, Object>) response.get("conversion_rates");
//                for (Map.Entry<String, Object> entry : rates.entrySet()) {
//                    // 6. 執行資料庫寫入
//                    exchangeRatesDao.upsertRate(entry.getKey(), new BigDecimal(entry.getValue().toString()));
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("WebClient 執行異常: " + e.getMessage());
//        }
	}

}
