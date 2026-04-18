package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CartClearReq;
import com.example.global_meals_gradle.req.CartRemoveReq;
import com.example.global_meals_gradle.req.CartSelectGiftReq;
import com.example.global_meals_gradle.req.CartSwitchBranchReq;
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
//@RequestMapping("/api/cart")
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
	@GetMapping("cart/{cartId}")
	public CartViewRes viewCart(@PathVariable int cartId, // 從 URL 路徑取出 cartId（例：/cart/7）
			@RequestParam int memberId // 從 URL 附加的查詢參數取出 memberId（例：?memberId=3）
	) {
		return cartService.viewCart(cartId, memberId);
	}

	
	/* API 2：同步商品（加入 / 更新數量）
	 * HTTP Method：POST（有寫入操作）
	 *  URL：POST /cart/sync
	 *  前端呼叫時機：使用者手動增減數量且「停手 1 秒後」（前端 Debounce）
	 *  前端 Body 範例：{ "cartId": 7, "productId": 5, "quantity": 3, ... }
	*/
	@PostMapping("cart/sync")
	public CartViewRes syncItem(@Valid @RequestBody CartSyncReq req // @Valid 觸發欄位驗證；@RequestBody 把 JSON 轉成 Java 物件
	) {
		return cartService.syncItem(req);
	}

	/*API 3：刪除購物車裡的單一商品
	 * HTTP Method：DELETE（刪除操作）
	 * URL：DELETE /cart/item
	 * 前端呼叫時機：使用者點擊商品旁邊的「刪除」圖示
	 * 前端 Body 範例：{ "cartId": 7, "productId": 5, "memberId": 3 }
	 */
	@DeleteMapping("cart/item")
	public CartViewRes removeItem(@Valid @RequestBody CartRemoveReq req) {
		return cartService.removeItem(req);
	}

	
	   /* API 4：使用者選擇贈品
		* HTTP Method：POST（有寫入操作）
		* URL：POST /cart/gift
		* 前端呼叫時機：使用者從贈品清單選擇了一個贈品後點擊確認
		* 若使用者直接點「確認下單」而未選任何贈品，前端不呼叫此 API，
		* 代表使用者選擇不領取贈品，後端不會寫入任何贈品明細。
		* 前端 Body 範例（選了大盤雞）：{ "cartId": 7, "memberId": 3, "selectedGiftProductId": 101 }
		*/

	@PostMapping("cart/gift")
	public CartViewRes selectGift(@Valid @RequestBody CartSelectGiftReq req) {
		return cartService.selectGift(req);
	}

	
	/*  API 5：清空購物車
	* HTTP Method：DELETE（刪除操作）
	* URL：DELETE /cart/clear
	* 前端呼叫時機：使用者點擊「清空購物車」按鈕
	* 前端 Body 範例：{ "cartId": 7, "memberId": 3 }
	*/
	@DeleteMapping("cart/clear")
	public CartViewRes clearCart(@Valid @RequestBody CartClearReq req) {
		return cartService.clearCart(req);
	}
//	API5:切換分店
	// POST /cart/switch-branch
	// 前端傳：舊的 cartId + 新的 globalAreaId + memberId
	// 後端回：一台空的新購物車（屬於新分店）
	@PostMapping("/switch-branch")
	public CartViewRes switchBranch(@RequestBody CartSwitchBranchReq req) {
	    return cartService.switchBranch(
	        req.getOldCartId(),
	        req.getNewGlobalAreaId(),
	        req.getMemberId()
	    );
	}

}