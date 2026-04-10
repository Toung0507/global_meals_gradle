package com.example.global_meals_gradle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全域網路配置類別 <br>
 * 作用：統一處理全專案的跨域設定 (CORS) 與 API 路徑路由規範
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	// 配置跨域資源共享 (CORS) 讓前端 (Angular/React) 能夠順利呼叫後端 API，而不會被瀏覽器阻擋

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// 代表套用到專案中所有的 API 路徑
		registry.addMapping("/**")
				// 允許的來源網域：使用 Pattern 可以同時支援本機開發與未來部署後的雲端環境
				.allowedOriginPatterns("http://localhost:4200", // Angular 開發環境
						"https://*.render.com", // 允許所有 Render 部署的子網域
						"http://127.0.0.1:4200" // 有些瀏覽器會識別為不同來源，一併加入
				)
				// 允許的 HTTP 方法：GET (讀取), POST (新增), PUT (修改), DELETE (刪除))
				.allowedMethods("GET", "POST", "PUT", "DELETE")
				// 允許的 Header：* 代表不限制，讓前端可以自定義傳入 Header (如 Authorization)
				.allowedHeaders("*")
				// 【核心重點】允許攜帶憑證：
				// 如果後續要實作 Session 登入或 Cookie 驗證，這行必須設為 true
				.allowCredentials(true);
	}

	//  配置路徑匹配規則 可以在這裡幫所有標註 @RestController 的 API 加上統一的 URL 前綴

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// 統一為所有 API 加上 /lazybaobao 前綴
		// 網址會變成：http://localhost:8080/lazybaobao/各模組
		configurer.addPathPrefix("/lazybaobao", HandlerTypePredicate.forAnnotation(RestController.class));
	}
}