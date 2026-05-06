package com.example.global_meals_gradle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient restClient() {
		// 使用 builder 可以進行更多自定義設定
		// 如果所有請求都有共同的基礎路徑，可以在設定builder()與build()之間加上 .baseUrl("...")
		return RestClient.builder().build();
	}
	
	/* LinePay */  
	@Bean
    public RestTemplate restTemplate() {
        // 使用 SimpleClientHttpRequestFactory 來設定連線逾時
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 設定連線逾時（Connection Timeout）- 5秒
        factory.setConnectTimeout(5000);
        // 設定讀取逾時（Read Timeout）- 5秒
        factory.setReadTimeout(5000);

        return new RestTemplate(factory);
    }

}
