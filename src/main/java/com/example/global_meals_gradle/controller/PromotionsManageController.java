package com.example.global_meals_gradle.controller;

import java.util.HashMap;
import java.util.Map;

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
import com.example.global_meals_gradle.res.PromotionsListRes;
import com.example.global_meals_gradle.vo.GiftItemVo;
import com.example.global_meals_gradle.res.PromotionsRes;
import com.example.global_meals_gradle.service.PromotionsManageService;
import com.example.global_meals_gradle.service.PromotionsService;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/promotions") // WebConfig 會自動補上 /lazybaobao，最終路徑為 /lazybaobao/promotions
public class PromotionsManageController {

	@Autowired
	private PromotionsManageService promotionsManageService;

	@Autowired
	private PromotionsService promotionsService;

	// POST /lazybaobao/promotions/add_gift
	@PostMapping("/add_gift")
	public BasicRes addPromotionGift(@RequestBody PromotionsManageReq req) {
		promotionsManageService.addPromotionGift(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	// DELETE /lazybaobao/promotions/delete/{id}
	@DeleteMapping("/delete/{id}")
	public BasicRes deletePromotion(@PathVariable("id") int id) {
		promotionsManageService.deletePromotion(id);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	// POST /lazybaobao/promotions/get_available_gifts?amount=XXX
	@PostMapping("/get_available_gifts")
	public List<GiftItemVo> getAvailableGifts(@RequestParam("amount") BigDecimal amount) {
		return promotionsService.getAvailableGifts(amount);
	}

	// GET /lazybaobao/promotions/list
	// GET /lazybaobao/promotions/list?globalAreaId=2
	@GetMapping("/list")
	public PromotionsListRes list(@RequestParam(name = "globalAreaId", required = false) Integer globalAreaId) {
		return promotionsManageService.getList(globalAreaId);
	}

	// POST /lazybaobao/promotions/create
	@PostMapping("/create")
	public Map<String, Object> create(@RequestBody PromotionsManageReq req) {
	    int newId = promotionsManageService.createPromotionWithGift(req);
	    Map<String, Object> res = new HashMap<>();
	    res.put("code", ReplyMessage.SUCCESS.getCode());
	    res.put("message", ReplyMessage.SUCCESS.getMessage());
	    res.put("id", newId);
	    return res;
	}


	// POST /lazybaobao/promotions/toggle_status
	@PostMapping("/toggle_status")
	public BasicRes toggle(@RequestBody PromotionsManageReq req) {
		promotionsManageService.togglePromotion(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	// POST /lazybaobao/promotions/update_info
	@PostMapping("/update_info")
	public BasicRes updateInfo(@RequestBody PromotionsManageReq req) {
		promotionsManageService.updatePromotionInfo(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}
	
	// POST /lazybaobao/promotions/update
	@PostMapping("/update")
	public BasicRes update(@RequestBody PromotionsManageReq req) {
	    promotionsManageService.updatePromotionBasic(req);
	    return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	// POST /lazybaobao/promotions/calculate
	@PostMapping("/calculate")
	public PromotionsRes calculate(@Valid @RequestBody PromotionsReq req) {
		return promotionsService.calculate(req, req.getOriginalAmount());
	}
}