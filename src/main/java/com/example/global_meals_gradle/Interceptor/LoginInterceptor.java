package com.example.global_meals_gradle.Interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// @Component 讓 Spring 把這個保全納入管理
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // ⚠️ 超級重要：放行 Angular 的 CORS 預檢請求 (OPTIONS)
        // 瀏覽器在跨域發送 POST/PATCH 之前，會先偷偷發一個 OPTIONS 請求問伺服器「我可以打你嗎？」
        // 這個請求不會帶 Session，如果不放行，你的前端一定會報 CORS 錯誤！
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; 
        }

        // 1. 檢查有沒有 Session
        // request.getSession(false) 代表「有就拿出來，沒有就回傳 null，不要幫我偷建一個新的」
        HttpSession session = request.getSession(false);

        // 2. 驗票邏輯：如果沒 Session，或者 Session 裡面沒有 loginStaff，就是沒買票！
        if (session == null || session.getAttribute("loginStaff") == null) {
            
            // 設定 HTTP 狀態碼為 401 (Unauthorized 未授權)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // 告訴前端我回傳的是 JSON 格式，編碼是 UTF-8 (避免中文亂碼)
            response.setContentType("application/json;charset=UTF-8");
            
            // 寫入你自訂的錯誤訊息 (格式對齊你的 StaffSearchRes)
            String jsonRes = "{\"code\":401, \"message\":\"尚未登入，請先登入！(由安檢閘門攔截)\"}";
            response.getWriter().write(jsonRes);
            
            System.out.println("🚨 攔截器發威：擋下了一個未登入的請求！路徑：" + request.getRequestURI());
            
            return false; // ❌ 擋下！不讓他去 Controller
        }

        return true; // ✅ 有票！放行讓他去 Controller
    }
}