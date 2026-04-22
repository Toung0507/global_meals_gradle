package com.example.global_meals_gradle.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

import java.io.IOException;
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

	/* 新增贈品至促銷活動（對已存在的活動補加贈品） */
	@PostMapping("/promotions/addPromotionGift")
	public BasicRes addPromotionGift(@RequestBody PromotionsManageReq req) {
		promotionsManageService.addPromotionGift(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 刪除促銷活動（同時真刪除底下所有贈品） */
	@DeleteMapping("/promotions/deletePromotion/{id}")
	public BasicRes deletePromotion(@PathVariable("id") int id) {
		promotionsManageService.deletePromotion(id);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* 查詢可選贈品清單：傳入消費金額，回傳所有達標的贈品讓使用者選一個 */
	@PostMapping("/promotions/getAvailableGifts")
	public List<GiftItem> getAvailableGifts(@RequestBody BigDecimal amount) {
		return promotionsService.getAvailableGifts(amount);
	}

	// =============================================
	// 以下為新增的四個端點（對應前端 manager-dashboard promotions 頁籤）
	// =============================================

	/**
	 * GET /promotions/list
	 * 取得所有促銷活動及各自的贈品規則清單（管理端列表頁使用）
	 *
	 * 不需要 Request Body，直接回傳全部資料
	 * 包含啟用與停用的活動，讓管理員看到完整狀態
	 */
	@GetMapping("/promotions/list")
	public PromotionsListRes list() {
		// 委派給 PromotionsManageService.getList()，由 Service 組裝所有活動與贈品
		return promotionsManageService.getList();
	}

	/**
	 * POST /promotions/create
	 * 一次建立促銷活動，並選擇性同時新增一筆贈品規則
	 *
	 * Request Body 欄位說明：
	 *   必填（活動）：name, startTime, endTime
	 *   選填（贈品）：giftProductId（> 0 才會建贈品）、fullAmount、quantity
	 *
	 * 兩個步驟在同一個 @Transactional 內，任一失敗整筆 rollback
	 * 欄位驗證全部在 Service 層手動進行
	 */
	@PostMapping("/promotions/create")
	public BasicRes create(@RequestBody PromotionsManageReq req) {
		// 委派給 createPromotionWithGift()，由 Service 處理驗證與寫入
		promotionsManageService.createPromotionWithGift(req);
		// 成功後回傳 200 + "Success!!"
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/**
	 * POST /promotions/toggle
	 * 啟用或停用一個促銷活動
	 *
	 * Request Body 欄位說明：
	 *   promotionsId → 要切換的活動 ID
	 *   active       → true = 開啟，false = 關閉（關閉時連帶停用底下所有贈品）
	 */
	@PostMapping("/promotions/toggle")
	public BasicRes toggle(@RequestBody PromotionsManageReq req) {
		// 委派給既有的 togglePromotion()，邏輯不重複實作
		promotionsManageService.togglePromotion(req);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/**
	 * POST /promotions/uploadImage/{id}
	 * 上傳促銷活動圖片，存入 promotions.promotion_img
	 *
	 * 前端用 multipart/form-data 格式傳送，欄位名稱為 "image"
	 * 活動 ID 由路徑帶入
	 */
	@PostMapping("/promotions/uploadImage/{id}")
	public BasicRes uploadImage(@PathVariable("id") int id,
			@RequestParam("image") MultipartFile image) throws IOException {
		promotionsManageService.uploadImage(id, image.getBytes());
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/**
	 * GET /promotions/image/{id}
	 * 取得促銷活動圖片，供前端 <img> 標籤直接顯示
	 *
	 * 回傳原始圖片位元組，Content-Type 為 image/jpeg
	 * 若該活動沒有圖片，回傳 404
	 */
	@GetMapping("/promotions/image/{id}")
	public ResponseEntity<byte[]> getImage(@PathVariable("id") int id) {
		byte[] imageBytes = promotionsManageService.getImage(id);
		if (imageBytes == null) {
			return ResponseEntity.notFound().build();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
	}

	/**
	 * POST /promotions/calculate
	 * 結帳時計算促銷結果：驗證並領取贈品、套用會員折扣、無條件進位
	 *
	 * Request Body（PromotionsReq）欄位說明：
	 *   cartId         → 購物車 ID（原封不動帶回回傳值，讓前端對應）
	 *   memberId       → 1 = 訪客（無折扣），> 1 = 會員（查折扣券）
	 *   useCoupon      → true = 使用者勾選使用 9 折券
	 *   selectedGiftId → 使用者選的贈品 ID（0 = 放棄）
	 *   originalAmount → 購物車原始總金額（由前端計算後傳入）
	 *
	 * 回傳 PromotionsRes：finalAmount（最終金額）、receivedGifts（贈品清單）等
	 */
	@PostMapping("/promotions/calculate")
	public PromotionsRes calculate(@Valid @RequestBody PromotionsReq req) {
		// originalAmount 已整合進 PromotionsReq，直接取出傳給 Service
		return promotionsService.calculate(req, req.getOriginalAmount());
	}

}
