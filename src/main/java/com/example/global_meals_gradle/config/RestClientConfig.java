package com.example.global_meals_gradle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
	
	@Bean
	public RestClient restClient() {
		// 使用 builder 可以進行更多自定義設定
		// 如果所有請求都有共同的基礎路徑，可以在設定builder()與build()之間加上 .baseUrl("...")
		return RestClient.builder().build();
	}

}
