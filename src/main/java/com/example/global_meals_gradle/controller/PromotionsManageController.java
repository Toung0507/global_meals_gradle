package com.example.global_meals_gradle.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.req.PromotionsManageReq;
import com.example.global_meals_gradle.req.PromotionsReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.PromotionsListRes;
import com.example.global_meals_gradle.res.PromotionsRes;
import com.example.global_meals_gradle.service.PromotionsManageService;
import com.example.global_meals_gradle.service.PromotionsService;
import com.example.global_meals_gradle.vo.GiftItemVo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/promotions")
@Tag(name = "促銷活動管理模組", description = "處理促銷活動增刪改查、贈品規則及結帳金額計算")
public class PromotionsManageController {

	@Autowired
	private PromotionsManageService promotionsManageService;

	// 查詢可選贈品清單由 PromotionsService 提供，不是 PromotionsManageService
	@Autowired
	private PromotionsService promotionsService;

	/* 新增贈品至促銷活動（對已存在的活動補加贈品） */
	@PostMapping("/addPromotionGift")
	@Operation(summary = "新增贈品規則", description = "對已存在的促銷活動補加贈品條件")
	public BasicRes addPromotionGift(@RequestBody PromotionsManageReq req) {
		promotionsManageService.addPromotionGift(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 刪除促銷活動（同時真刪除底下所有贈品） */
	@DeleteMapping("/deletePromotion/{id}")
	@Operation(summary = "刪除促銷活動", description = "真刪除指定促銷活動及底下所有關聯贈品")
	public BasicRes deletePromotion(@PathVariable("id") int id) {
		promotionsManageService.deletePromotion(id);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 查詢可選贈品清單：傳入消費金額，回傳所有達標的贈品讓使用者選一個 */
	@PostMapping("/getAvailableGifts")
	@Operation(summary = "查詢可選贈品", description = "根據消費金額查詢符合條件的贈品清單")
	public List<GiftItemVo> getAvailableGifts(@RequestBody BigDecimal amount) {
		return promotionsService.getAvailableGifts(amount);
	}

	@GetMapping("/list")
	@Operation(summary = "取得活動列表", description = "取得所有促銷活動及其贈品規則 (管理後台用)")
	public PromotionsListRes list() {
		return promotionsManageService.getList();
	}

	@PostMapping(value = "/create", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "建立促銷活動", description = "建立活動並同時上傳活動圖片（必填），選擇性設定一筆贈品規則")
	public BasicRes create(
			@RequestPart("data") PromotionsManageReq req,
			@RequestPart("image") MultipartFile image) throws IOException {
		promotionsManageService.createPromotionWithGift(req, image.getBytes());
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	@PostMapping("/toggle")
	@Operation(summary = "啟用/停用活動", description = "切換活動狀態 (開啟/關閉)")
	public BasicRes toggle(@RequestBody PromotionsManageReq req) {
		promotionsManageService.togglePromotion(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	@PostMapping(value = "/uploadImage/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "上傳活動圖片", description = "為指定促銷活動上傳宣傳圖片")
	public BasicRes uploadImage(
			@Parameter(description = "活動 ID") @PathVariable("id") int id,
			@Parameter(description = "圖片檔案") @RequestParam("image") MultipartFile image)
			throws IOException {
		promotionsManageService.uploadImage(id, image.getBytes());
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	@GetMapping("/image/{id}")
	@Operation(summary = "取得活動圖片", description = "獲取指定活動的圖片檔案")
	public ResponseEntity<byte[]> getImage(@PathVariable("id") int id) {
		byte[] imageBytes = promotionsManageService.getImage(id);
		if (imageBytes == null) {
			return ResponseEntity.notFound().build();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
	}

	@PostMapping("/calculate")
	@Operation(summary = "計算結帳金額", description = "結帳時套用折扣、檢查贈品並計算最終總額")
	public PromotionsRes calculate(@Valid @RequestBody PromotionsReq req) {
		return promotionsService.calculate(req, req.getOriginalAmount());
	}

}
