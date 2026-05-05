package com.example.global_meals_gradle.controller; // 宣告此類別所屬的套件路徑

import org.springframework.beans.factory.annotation.Autowired; // 自動注入 Spring Bean
import org.springframework.web.bind.annotation.DeleteMapping; // HTTP DELETE 方法的路由註解
import org.springframework.web.bind.annotation.GetMapping; // HTTP GET 方法的路由註解
import org.springframework.web.bind.annotation.PathVariable; // 從 URL 路徑取得變數（例如 /{id}）
import org.springframework.web.bind.annotation.PostMapping; // HTTP POST 方法的路由註解
import org.springframework.web.bind.annotation.RequestBody; // 從 Request Body 取得 JSON 資料
import org.springframework.web.bind.annotation.RequestMapping; // 設定這個 Controller 的共用路徑前綴
import org.springframework.web.bind.annotation.RestController; // 標記為 REST Controller，回傳值自動序列化為 JSON

import com.example.global_meals_gradle.req.DiscountReq; // discount 管理用的請求參數物件
import com.example.global_meals_gradle.res.BasicRes; // 通用回應物件（code + message）
import com.example.global_meals_gradle.res.DiscountRes; // 查詢清單用的回應物件
import com.example.global_meals_gradle.service.DiscountService; // discount 業務邏輯層

import io.swagger.v3.oas.annotations.Operation; // Swagger：描述單一 API 端點的說明
import io.swagger.v3.oas.annotations.Parameter; // Swagger：描述單一參數的說明
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger：替整個 Controller 加上分組標籤

@RestController // 告訴 Spring 這是一個 REST Controller，所有方法回傳值直接序列化為 JSON
@RequestMapping("/discount") // 這個 Controller 所有端點的 URL 前綴都是 /discount
@Tag(name = "折抵管理模組", description = "處理各國折抵上限(usage_cap)與消費累積次數(count)的新增、查詢、修改、刪除") // Swagger 分組標籤
public class DiscountController {

	@Autowired // 讓 Spring 自動注入 DiscountService 的實例
	private DiscountService discountService; // 負責 discount 的 CRUD 業務邏輯

	/* ===================== 新增 discount 記錄 ===================== */
	@PostMapping("/create") // 對應 POST /discount/create
	@Operation(summary = "新增折抵記錄", description = "新增一筆 discount（需帶入 regionsId、usageCap，count 選填預設 0）") // Swagger 說明
	public BasicRes create(@RequestBody DiscountReq req) { // 從 Request Body 取得新增參數
		return discountService.create(req); // 呼叫 Service 執行新增並回傳結果
	}

	/* ===================== 查詢全部 discount 清單 ===================== */
	@GetMapping("/list") // 對應 GET /discount/list
	@Operation(summary = "查詢全部折抵記錄", description = "取得所有 discount 記錄（含各國折抵上限與累積次數）") // Swagger 說明
	public DiscountRes list() { // 不需要任何參數，直接回傳全部資料
		return discountService.getList(); // 呼叫 Service 查出所有折抵記錄
	}

	/* ===================== 查詢單筆 discount ===================== */
	@GetMapping("/{id}") // 對應 GET /discount/{id}
	@Operation(summary = "查詢單筆折抵記錄", description = "根據 discount 主鍵 id 查詢單筆記錄") // Swagger 說明
	public DiscountRes getById(@Parameter(description = "discount 主鍵 ID") @PathVariable("id") int id) { // 從 URL 路徑取得 discount 主鍵
		return discountService.getById(id); // 呼叫 Service 以主鍵查詢並回傳結果
	}

	/* ===================== 修改 discount 的折抵上限（usage_cap） ===================== */
	@PostMapping("/update-usage-cap") // 對應 POST /discount/update-usage-cap
	@Operation(summary = "修改折抵上限", description = "修改指定 discount 的 usage_cap（需帶入 id 與新的 usageCap）") // Swagger 說明
	public BasicRes updateUsageCap(@RequestBody DiscountReq req) { // 從 Request Body 取得 id 與新的 usageCap
		return discountService.updateUsageCap(req); // 呼叫 Service 執行更新並回傳結果
	}

	/* ===================== 修改 discount 的消費累積次數（count） ===================== */
	@PostMapping("/update-count") // 對應 POST /discount/update-count
	@Operation(summary = "修改累積次數", description = "修改指定 discount 的 count（需帶入 id 與新的 count）") // Swagger 說明
	public BasicRes updateCount(@RequestBody DiscountReq req) { // 從 Request Body 取得 id 與新的 count
		return discountService.updateCount(req); // 呼叫 Service 執行更新並回傳結果
	}

	/* ===================== 刪除 discount 記錄 ===================== */
	@DeleteMapping("/{id}") // 對應 DELETE /discount/{id}
	@Operation(summary = "刪除折抵記錄", description = "真刪除指定 discount 記錄（不可回復）") // Swagger 說明
	public BasicRes delete(@Parameter(description = "discount 主鍵 ID") @PathVariable("id") int id) { // 從 URL 路徑取得要刪除的 discount 主鍵
		return discountService.delete(id); // 呼叫 Service 執行刪除並回傳結果
	}

}
