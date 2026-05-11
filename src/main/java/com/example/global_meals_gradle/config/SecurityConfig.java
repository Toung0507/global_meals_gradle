package com.example.global_meals_gradle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 *
 * 目的：本專案使用自訂 LoginInterceptor + HttpSession 做身份驗證，
 *       不使用 Spring Security 內建的身份驗證機制。
 *       此設定確保 Spring Security 不干擾 Session 存活時間，
 *       也不攔截任何請求（交由 LoginInterceptor 負責）。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 關閉 CSRF：前端透過 withCredentials 傳 Cookie，不使用 CSRF Token
            .csrf(csrf -> csrf.disable())
            // 關閉 HTTP Basic 驗證（瀏覽器彈窗）
            .httpBasic(basic -> basic.disable())
            // 關閉表單登入
            .formLogin(form -> form.disable())
            // 關閉預設登出端點（LoginInterceptor 已統一處理）
            .logout(logout -> logout.disable())
            // 放行所有請求：身份驗證由 LoginInterceptor 負責
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // Session 策略：IF_REQUIRED — 有需要才建立，不強制新建，不干擾現有 Session
            // 這樣 session.setMaxInactiveInterval(86400) 的設定才不會被 Security 覆蓋
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }
}
