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


import com.example.global_meals_gradle.req.CartRemoveReq;
import com.example.global_meals_gradle.req.CartSyncReq;
import com.example.global_meals_gradle.res.CartViewRes;
import com.example.global_meals_gradle.service.CartService;

import jakarta.validation.Valid;
//------------還沒有編輯完
/*1.@RestController:@Controller + @ResponseBody 的組合
 * 作用：從 Service 拿回來的 Java 物件（如 CartViewRes）直接翻譯成前端看得懂的 JSON 格式。
 * 2.@RequestMapping：定義 URL 路由的基礎路徑。
 * 3.@CrossOrigin(origins = "http://localhost:4200") ：
 * 4.@PathVariable：
 * 5.@RequestParam：
 * 6. @GetMapping("/{cartId}")、@PostMapping("/sync")、@DeleteMapping("/item")、@PostMapping("/coupon")：
 * GET：獲取資料（看）、POST：提交、新增或執行複雜邏輯（動）、DELETE：刪除資料。如何使用： 放在方法上方，括號內填入子路徑
 * DELETE /item 比 POST /deleteItem 更符合國際標準。
 * 7.@Autowired (請大腦進場)：依賴注入（Dependency Injection），
 * 讓 Spring 自動把已經實例化好的 CartService（大腦）裝載到這個變數中，不需要你自己去 new 它
 * @RequestBody (包裹拆封員)：前端傳來的 JSON 字串，會被這個註解強制轉換為 Java 物件，前端必須設定 Content-Type: application/json，否則這層轉換會失敗。
 * @Valid：檢查這個傳進來的物件（Request DTO）是否符合我設定的規則（例如：ID 不能為空、長度要大於零等），： 放在 @RequestBody 的前面。
 * 【為什麼重要】： 它可以防止髒數據進入業務邏輯層，如果資料不合法，Spring 會直接攔截並回傳錯誤訊息，不需要你在 Service 裡面寫一堆 if (req == null)。
 */

@RestController
//@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {
	// 把大腦請進來
	@Autowired
	private CartService cartService;

	//	「查看購物車」方法
	@GetMapping("cart/{cartId}")
	public CartViewRes viewCart(@PathVariable int cartId, @RequestParam int memberId) {
		return cartService.viewCart(cartId, memberId);
	}

	//	「同步商品」方法
	@PostMapping("cart/sync")
	public CartViewRes syncItem(@Valid @RequestBody CartSyncReq req) {
		return cartService.syncItem(req);
	}

	//	刪除單品」方法
	@DeleteMapping("cart/item")
	public CartViewRes removeItem(@Valid @RequestBody CartRemoveReq req) {
		return cartService.removeItem(req);
	}

//	//	「切換折價券」方法----不再購物車裡顯示了
//	@PostMapping("/coupon")
//	public CartViewRes applyCoupon(@Valid @RequestBody CartCouponReq req) {
//		return cartService.applyCoupon(req);
//	}
}
