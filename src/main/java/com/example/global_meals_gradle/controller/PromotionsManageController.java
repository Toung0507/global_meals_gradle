package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.req.PromotionsManageReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.GiftItem;
import com.example.global_meals_gradle.service.PromotionsManageService;
import com.example.global_meals_gradle.service.PromotionsService;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class PromotionsManageController {

	@Autowired
	private PromotionsManageService promotionsManageService;

	// 查詢可選贈品清單由 PromotionsService 提供，不是 PromotionsManageService
	@Autowired
	private PromotionsService promotionsService;

	/* 新增促銷活動 */
	@PostMapping("Promotions/addPromotion")
	public BasicRes addPromotion(@Valid @RequestBody PromotionsManageReq req) {
		promotionsManageService.addPromotion(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 新增贈品至促銷活動 */
	@PostMapping("Promotions/addPromotionGift")
	public BasicRes addPromotionGift(@Valid @RequestBody PromotionsManageReq req) {
		promotionsManageService.addPromotionGift(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 開關促銷活動：active=true 開啟、active=false 關閉（同步關閉底下所有贈品） */
	@PutMapping("Promotions/togglePromotion")
	public BasicRes togglePromotion(@Valid @RequestBody PromotionsManageReq req) {
		promotionsManageService.togglePromotion(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 刪除促銷活動（同時真刪除底下所有贈品） */
	@DeleteMapping("Promotions/deletePromotion/{id}")
	public BasicRes deletePromotion(@PathVariable("id") int id) {
		promotionsManageService.deletePromotion(id);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 查詢可選贈品清單：傳入消費金額，回傳所有達標的贈品讓使用者選一個 */
	@PostMapping("Promotions/getAvailableGifts")
	public List<GiftItem> getAvailableGifts(@RequestParam("amount") BigDecimal amount) {
		return promotionsService.getAvailableGifts(amount);
	}

}
