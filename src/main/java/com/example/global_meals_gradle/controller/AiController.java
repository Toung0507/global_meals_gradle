package com.example.global_meals_gradle.controller;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.global_meals_gradle.req.AiProductDescReq;
import com.example.global_meals_gradle.req.AiPromotionsReq;
import com.example.global_meals_gradle.res.AiRes;
import com.example.global_meals_gradle.service.AiService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
//全局設定此模組的開頭都是 /ai
@RequestMapping("/ai")
public class AiController {
	@Autowired
	private AiService aiService;

	// 1. 商品描述 (單純文字，可視需求用 RequestBody 或 RequestParam)
	@PostMapping("/product-desc")
	public AiRes generateProductDesc(@Valid @RequestBody AiProductDescReq req, HttpSession session) {
		return aiService.generateProductDesc(req, session);
	}

	// 2. 活動文案 (含圖片，使用 consumes = MULTIPART_FORM_DATA_VALUE)
	@PostMapping(value = "/promo-copy", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public AiRes generatePromoCopy(@Valid @RequestPart("data") AiPromotionsReq req,
			@RequestPart("file") MultipartFile file, HttpSession session) {
		return aiService.generatePromoCopy(req, file, session);
	}
}
