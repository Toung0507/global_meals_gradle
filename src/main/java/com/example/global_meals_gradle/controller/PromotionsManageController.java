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

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.req.PromotionsManageReq;
import com.example.global_meals_gradle.req.PromotionsReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.GiftItem;
import com.example.global_meals_gradle.res.PromotionsListRes;
import com.example.global_meals_gradle.res.PromotionsRes;
import com.example.global_meals_gradle.service.PromotionsManageService;
import com.example.global_meals_gradle.service.PromotionsService;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/lazybaobao") // 統一加上 lazybaobao 前綴，與前端 api.config.ts 路由一致
public class PromotionsManageController {

	@Autowired
	private PromotionsManageService promotionsManageService;

	@Autowired
	private PromotionsService promotionsService;

	/* 新增贈品至促銷活動
	 * URL: POST /lazybaobao/promotions/add_gift
	 * 路徑從 Promotions/addPromotionGift 改為 promotions/add_gift，對應前端 api.config.ts
	 */
	@PostMapping("promotions/add_gift")
	public BasicRes addPromotionGift(@RequestBody PromotionsManageReq req) {
		promotionsManageService.addPromotionGift(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 刪除促銷活動（同時真刪除底下所有贈品）
	 * URL: DELETE /lazybaobao/promotions/delete/{id}
	 */
	@DeleteMapping("promotions/delete/{id}")
	public BasicRes deletePromotion(@PathVariable("id") int id) {
		promotionsManageService.deletePromotion(id);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 查詢可選贈品清單：傳入消費金額，回傳所有達標的贈品
	 * URL: POST /lazybaobao/promotions/get_available_gifts?amount=XXX
	 * 路徑從 Promotions/getAvailableGifts 改為 promotions/get_available_gifts，對應前端 api.config.ts
	 */
	@PostMapping("promotions/get_available_gifts")
	public List<GiftItem> getAvailableGifts(@RequestParam("amount") BigDecimal amount) {
		return promotionsService.getAvailableGifts(amount);
	}

	/* 取得促銷活動列表
	 * URL: GET /lazybaobao/promotions/list
	 * URL: GET /lazybaobao/promotions/list?globalAreaId=2  （客戶端：回傳全球 + 分店專屬）
	 */
	@GetMapping("promotions/list")
	public PromotionsListRes list(@RequestParam(required = false) Integer globalAreaId) {
		return promotionsManageService.getList(globalAreaId);
	}

	/* 一次建立促銷活動，並選擇性同時新增一筆贈品規則
	 * URL: POST /lazybaobao/promotions/create
	 */
	@PostMapping("promotions/create")
	public BasicRes create(@RequestBody PromotionsManageReq req) {
		promotionsManageService.createPromotionWithGift(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 啟用或停用一個促銷活動
	 * URL: POST /lazybaobao/promotions/toggle_status
	 * 路徑從 /promotions/toggle 改為 promotions/toggle_status，對應前端 api.config.ts
	 */
	@PostMapping("promotions/toggle_status")
	public BasicRes toggle(@RequestBody PromotionsManageReq req) {
		promotionsManageService.togglePromotion(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 更新活動文案（description）與封面圖片（promotion_img）
	 * URL: POST /lazybaobao/promotions/update_info
	 */
	@PostMapping("promotions/update_info")
	public BasicRes updateInfo(@RequestBody PromotionsManageReq req) {
		promotionsManageService.updatePromotionInfo(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 結帳時計算促銷結果（折扣 + 贈品）
	 * URL: POST /lazybaobao/promotions/calculate
	 */
	@PostMapping("promotions/calculate")
	public PromotionsRes calculate(@Valid @RequestBody PromotionsReq req) {
		return promotionsService.calculate(req, req.getOriginalAmount());
	}

}
