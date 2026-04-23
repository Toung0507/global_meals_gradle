package com.example.global_meals_gradle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.global_meals_gradle.Interceptor.LoginInterceptor;

// @Configuration 告訴 Spring 這是一個系統設定檔，啟動時要先讀它
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor; // 把剛剛寫的保全請過來

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        // 把保全註冊進去
        registry.addInterceptor(loginInterceptor)
                // 1. 攔截哪些路徑？
                // "/**" 代表攔截這個路徑下的所有 API
                // 根據你的 Controller，需要權限的都是 /api/admin 開頭的
                .addPathPatterns("/api/admin/**")
                .addPathPatterns("/api/staff/**")
                
                // 2. 排除哪些路徑？(不查票的白名單)
                // 登入和登出本來就不需要登入就能按，所以絕對要排除！
                // (雖然我們上面只攔截了 /api/admin，登入是 /api/auth，
                //  但實務上還是習慣明確寫出來，未來擴充才不會亂)
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/logout"
                );
    }
}