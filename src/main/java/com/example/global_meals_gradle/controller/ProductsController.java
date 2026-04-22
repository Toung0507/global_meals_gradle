package com.example.global_meals_gradle.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.MonthlyProductsSalesReq;
import com.example.global_meals_gradle.res.MonthlyProductsSalesRes;
import com.example.global_meals_gradle.service.ProductService;

import jakarta.servlet.http.HttpSession;

@RestController
public class ProductsController {
	// Session 的 key 名稱，與 StaffController 保持一致
	// static final：這個類別永遠不變的固定值，放在所有 @Autowired 的上方
	private static final String SESSION_KEY = "loginStaff";
	 @Autowired
	    private ProductService productService;

	/* =================================================================
	 *  GET /api/rm/monthly-sales — 分店長查詢：某年某月該分店所有商品銷售量
	 *
	 *  呼叫範例：
	 *    GET /api/rm/monthly-sales?year=2026&month=4
	 *
	 *  不需要傳 globalAreaId！分店長的分店 ID 從 Session 取（安全設計）
	 * ================================================================= */
	@GetMapping("/api/rm/monthly-sales")
	public MonthlyProductsSalesRes getMonthlySalesByBranch(
	        @RequestParam Integer year,
	        @RequestParam Integer month,
	        HttpSession session) {

	    // Step 1：從 Session 取出登入的 Staff
	    Staff operator = (Staff) session.getAttribute(SESSION_KEY);

	    // Step 2：如果 Session 裡沒有 Staff，代表未登入
	    if (operator == null) {
	        return new MonthlyProductsSalesRes(
	                ReplyMessage.NOT_LOGIN.getCode(),
	                ReplyMessage.NOT_LOGIN.getMessage());
	    }

	    // Step 3：組裝 Req 物件（只需要 year + month，分店ID從 Session 取）
	    MonthlyProductsSalesReq req = new MonthlyProductsSalesReq();
	    req.setYear(year);
	    req.setMonth(month);

	    // Step 4：把 operator 傳給 Service，Service 裡再做「角色是不是 RM」的判斷
	    return productService.getMonthlySalesByBranch(req, operator);
	}

	/* =================================================================
	 *  GET /api/admin/top5-monthly-sales — 老闆查詢：某年某月指定國家前5名
	 *
	 *  呼叫範例：
	 *    GET /api/admin/top5-monthly-sales?year=2026&month=4&regionId=1
	 *
	 *  regionId：前端國家下拉選單選擇後傳入（老闆有權選任何國家，安全的）
	 * ================================================================= */
	@GetMapping("/api/admin/top5-monthly-sales")
	public MonthlyProductsSalesRes getTop5MonthlySalesByRegion(
	        @RequestParam Integer year,
	        @RequestParam Integer month,
	        @RequestParam Integer regionId,
	        HttpSession session) {

	    // Step 1：取 Session 登入者
	    Staff operator = (Staff) session.getAttribute(SESSION_KEY);

	    // Step 2：未登入檢查
	    if (operator == null) {
	        return new MonthlyProductsSalesRes(
	                ReplyMessage.NOT_LOGIN.getCode(),
	                ReplyMessage.NOT_LOGIN.getMessage());
	    }

	    // Step 3：組裝 Req（帶入 regionId，老闆查詢必填）
	    MonthlyProductsSalesReq req = new MonthlyProductsSalesReq();
	    req.setYear(year);
	    req.setMonth(month);
	    req.setRegionId(regionId);

	    // Step 4：傳給 Service，Service 裡再確認角色是否為 ADMIN
	    return productService.getTop5MonthlySalesByRegion(req, operator);
	}

}