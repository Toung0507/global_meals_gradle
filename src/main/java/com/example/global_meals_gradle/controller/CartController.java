package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CartClearReq;
import com.example.global_meals_gradle.req.CartRemoveReq;
import com.example.global_meals_gradle.req.CartSelectGiftReq;
import com.example.global_meals_gradle.req.CartSyncReq;
import com.example.global_meals_gradle.res.CartViewRes;
import com.example.global_meals_gradle.service.CartService;

import jakarta.validation.Valid;

/*1.@RestController:@Controller + @ResponseBody 的組合
 * 作用：從 Service 拿回來的 Java 物件（如 CartViewRes）直接翻譯成前端看得懂的 JSON 格式。
 * @RequestBody (包裹拆封員)：前端傳來的 JSON 字串，會被這個註解強制轉換為 Java 物件，前端必須設定 Content-Type: application/json，否則這層轉換會失敗。
 * 2.@RequestMapping：定義 URL 路由的基礎路徑。
 * 3.@CrossOrigin(origins = "http://localhost:4200") ：允許來自特定網域（這裡指 http://localhost:4200）的網頁程式存取你的後端 API。
 * "http://localhost:4200"：Angular 開發環境預設的網址。這行程式碼告訴伺服器：「如果是從這個網址發過來的請求，請放行。」
 * 4.@PathVariable：URL 範例	：/cart/7，參數前標記，這個參數是URL 路徑的一部分（這裡是7），作用：定位資源（這是誰？，預設必填
 * 5.@RequestParam：URL 範例	：/cart?cartId=7，URL 問號 ? 後面，作用：過濾或傳遞參數（屬性為何？），預設必填（可用 required=false 修改）
 * 6. @GetMapping("/{cartId}")、@PostMapping("/sync")、@DeleteMapping("/item")、@PostMapping("/coupon")：
 * GET：獲取資料（看）、POST：提交、新增或執行複雜邏輯（動）、DELETE：刪除資料。如何使用： 放在方法上方，括號內填入子路徑
 * DELETE /item 比 POST /deleteItem 更符合國際標準。
 * .@Autowired (請大腦進場)：依賴注入（Dependency Injection），
 * 讓 Spring 自動把已經實例化好的 CartService（大腦）裝載到這個變數中，不需要你自己去 new 它
 * @Valid：檢查這個傳進來的物件（Request DTO）是否符合我設定的規則（例如：ID 不能為空、長度要大於零等），： 放在 @RequestBody 的前面。
 * 【為什麼重要】： 它可以防止髒數據進入業務邏輯層，如果資料不合法，Spring 會直接攔截並回傳錯誤訊息，不需要你在 Service 裡面寫一堆 if (req == null)。
 */

@RestController
@RequestMapping("/lazybaobao") // 統一加上 lazybaobao 前綴，與前端 api.config.ts 的路由設定一致
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {
//	 @Autowired：讓 Spring 自動注入已建好的 CartService，不需要手動 new
	@Autowired
	private CartService cartService;

	
	/*API 1：查看購物車
	 * HTTP Method：GET（只讀，取得資料）,
	 * URL：GET /cart/{cartId}?memberId=X
	 * 前端呼叫時機：① 頁面初次載入 ② 按下「確認下單」前的最後一次驗算
	 */
	/* API 1：查看購物車
	 * URL: GET /lazybaobao/cart/{cartId}?memberId=X
	 */
	@GetMapping("cart/{cartId}")
	public CartViewRes viewCart(@PathVariable int cartId,
			@RequestParam("memberId") int memberId
	) {
		return cartService.viewCart(cartId, memberId);
	}

	/* API 2：同步商品（加入 / 更新數量）
	 * URL: POST /lazybaobao/cart/sync_item
	 * 路徑從 cart/sync 改為 cart/sync_item，對應前端 api.config.ts
	 */
	@PostMapping("cart/sync_item")
	public CartViewRes syncItem(@Valid @RequestBody CartSyncReq req) {
		return cartService.syncItem(req);
	}

	/* API 3：刪除購物車裡的單一商品
	 * URL: DELETE /lazybaobao/cart/remove_item
	 * 路徑從 cart/item 改為 cart/remove_item，對應前端 api.config.ts
	 */
	@DeleteMapping("cart/remove_item")
	public CartViewRes removeItem(@Valid @RequestBody CartRemoveReq req) {
		return cartService.removeItem(req);
	}

	/* API 4：使用者選擇贈品
	 * URL: POST /lazybaobao/cart/select_gift
	 * 路徑從 cart/gift 改為 cart/select_gift，對應前端 api.config.ts
	 */
	@PostMapping("cart/select_gift")
	public CartViewRes selectGift(@Valid @RequestBody CartSelectGiftReq req) {
		return cartService.selectGift(req);
	}

	/* API 5：清空購物車
	 * URL: DELETE /lazybaobao/cart/clear_cart
	 * 路徑從 cart/clear 改為 cart/clear_cart，對應前端 api.config.ts
	 */
	@DeleteMapping("cart/clear_cart")
	public CartViewRes clearCart(@Valid @RequestBody CartClearReq req) {
		return cartService.clearCart(req);
	}
}
