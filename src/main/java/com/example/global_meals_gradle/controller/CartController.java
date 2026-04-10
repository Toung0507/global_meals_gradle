package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CartClearReq;
import com.example.global_meals_gradle.req.CartRemoveReq;
import com.example.global_meals_gradle.req.CartSelectGiftReq;
import com.example.global_meals_gradle.req.CartSyncReq;
import com.example.global_meals_gradle.res.CartViewRes;
import com.example.global_meals_gradle.service.CartService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
// 全局設定此模組的開頭都是 /cart
@RequestMapping("/cart")
public class CartController {
	@Autowired
	private CartService cartService;

	/**
	 * API 1：查看購物車 <br>
	 * HTTP Method：GET（只讀，取得資料）<br>
	 * URL：GET /cart/{cartId}?memberId=X <br>
	 * 前端呼叫時機：① 頁面初次載入 ② 按下「確認下單」前的最後一次驗算
	 */
	@GetMapping("/{cartId}")
	public CartViewRes viewCart(@PathVariable int cartId, // 從 URL 路徑取出 cartId（例：/cart/7）
			@RequestParam int memberId) { // 從 URL 附加的查詢參數取出 memberId（例：?memberId=3）
		return cartService.viewCart(cartId, memberId);
	}

	/**
	 * API 2：同步商品（加入 / 更新數量） <br>
	 * HTTP Method：POST（有寫入操作）<br>
	 * URL：POST /cart/sync <br>
	 * 前端呼叫時機：使用者手動增減數量且「停手 1 秒後」（前端 Debounce） <br>
	 * 前端 Body 範例：{ "cartId": 7, "productId": 5, "quantity": 3, ... }
	 */

	@PostMapping("sync")
	// @Valid 觸發欄位驗證
	// @RequestBody 把 JSON 轉成 Java 物件
	public CartViewRes syncItem(@Valid @RequestBody CartSyncReq req) {
		return cartService.syncItem(req);
	}

	/**
	 * API 3：刪除購物車裡的單一商品 <br>
	 * HTTP Method：DELETE（刪除操作）<br>
	 * URL：DELETE /cart/item <br>
	 * 前端呼叫時機：使用者點擊商品旁邊的「刪除」圖示 <br>
	 * 前端 Body 範例：{ "cartId": 7, "productId": 5, "memberId":3 }
	 */

	@DeleteMapping("item")
	public CartViewRes removeItem(@Valid @RequestBody CartRemoveReq req) {
		return cartService.removeItem(req);
	}

	/**
	 * API 4：使用者選擇贈品 <br>
	 * HTTP Method：POST（有寫入操作）<br>
	 * URL：POST /cart/gift <br>
	 * 前端呼叫時機：使用者從贈品清單選擇了一個贈品後點擊確認 <br>
	 * 若使用者直接點「確認下單」而未選任何贈品，前端不呼叫此 API，並代表使用者選擇不領取贈品，後端不會寫入任何贈品明細。 <br>
	 * 前端 Body 範例（選了大盤雞）：{ "cartId": 7, "memberId": 3,"selectedGiftProductId": 101 }
	 */

	@PostMapping("gift")
	public CartViewRes selectGift(@Valid @RequestBody CartSelectGiftReq req) {
		return cartService.selectGift(req);
	}

	/**
	 * API 5：清空購物車 <br>
	 * HTTP Method：DELETE（刪除操作）<br>
	 * URL：DELETE /cart/clear <br>
	 * 前端呼叫時機：使用者點擊「清空購物車」按鈕 <br>
	 * 前端 Body 範例：{ "cartId": 7, "memberId": 3 }
	 */

	@DeleteMapping("clear")
	public CartViewRes clearCart(@Valid @RequestBody CartClearReq req) {
		return cartService.clearCart(req);
	}
}

// ================================
// 一、控制器與資料轉換相關
// ================================

// @RestController = @Controller + @ResponseBody
// 從 Service 拿回來的 Java 物件（如 CartViewRes），會自動轉換成前端看得懂的 JSON 格式。

// @RequestBody（包裹拆封員）
// 作用：將前端傳來的 JSON 字串，強制轉換為 Java 物件。
// 注意：前端必須設定：Content-Type: application/json 否則這層轉換會失敗。

// ================================
// 二、路由與跨網域設定
// ================================

// @RequestMapping
// 定義 URL 路由的「基礎路徑」。

// @CrossOrigin(origins = "http://localhost:4200")
// 允許特定網域呼叫你的後端 API。
// "http://localhost:4200"：Angular 開發環境預設網址。
// 這行的意思是：「如果請求來自這個網址，允許存取。」

// ================================
// 三、URL 參數取得方式 (RESTful API 傳參規範)
// ================================

// @PathVariable（路徑參數）
// URL 範例：/api/products/101
//
// 參數位置：URL 路徑的一部分（例如：101）
//
// 核心作用：「唯一性定位」用來找尋特定、唯一的資源（Identify）。
//
// 語義比喻：這是這筆資料的「身分證字號」，直接刻在網址裡。
//
// 預設狀態：必填（少了它網址就不完整，會報 404）。
//
// 使用時機：取得（GET）、修改（PUT/PATCH）或刪除（DELETE）特定 ID 的資料。

// @RequestParam（請求參數）
// URL 範例：/api/products?category=food&price=500
//
// 參數位置：URL 問號 (?) 後面的鍵值對
//
// 核心作用：「條件性篩選」 用來對結果進行過濾、排序或分頁（Filter / Sort / Paginate）。
//
// 語義比喻：這是查詢時的「篩選標籤」，網址主體不變，但內容因標籤而異。
//
// 預設狀態：必填（但常用 required=false 設為非必填，或搭配 defaultValue 提供預設值）。
//
// 使用時機：搜尋關鍵字、分頁處理（page=1）、或是不適合放在路徑中的非唯一性參數。

// ================================
// 四、HTTP 方法對應
// ================================

// 常見 Mapping：
//
// @GetMapping("/{cartId}")
// @PostMapping("/sync")
// @DeleteMapping("/item")
// @PostMapping("/coupon")
//
// HTTP 方法意義：
// GET     → 獲取資料（看）
// POST    → 新增資料 / 執行邏輯（動）
// DELETE  → 刪除資料
//
// RESTful 建議：DELETE/item 比 POST/deleteItem 更符合國際標準。

// ================================
// 五、依賴注入與驗證
// ================================

// @Autowired（請大腦進場）
// 依賴注入（Dependency Injection）
//
// 讓 Spring 自動把：已建立好的 CartService（大腦）注入到這個變數中，不需要自己 new。

// @Valid
// 作用：檢查傳入的 Request DTO是否符合規則，
// 例如：
// - ID 不能為空
// - 長度必須大於 0
//
// 使用位置：放在 @RequestBody 前面
//
// 為什麼重要：
// 可以防止髒資料進入業務邏輯層，
// 若資料不合法：Spring 會直接回傳錯誤，不需要在 Service 裡寫一堆：if (req == null)