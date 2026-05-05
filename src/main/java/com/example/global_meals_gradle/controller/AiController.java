package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
//全局設定此模組的開頭都是 /ai
@RequestMapping("/ai")
@Tag(name = "AI 智能輔助模組", description = "提供自動化文案生成、行銷內容設計等相關 API")
public class AiController {
	@Autowired
	private AiService aiService;

	// 1. 商品描述 (單純文字，可視需求用 RequestBody 或 RequestParam)
	@PostMapping(value="/product-desc", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@Operation(summary = "生成商品描述", description = "根據商品名稱、圖片、風格、分類，自動生成適用於菜單的簡短誘人描述")
	public AiRes generateProductDesc(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) 
            @Valid @RequestPart("data") AiProductDescReq req,
            
            @Parameter(description = "商品圖片") 
            @RequestPart("file") MultipartFile file, 
            
            @Parameter(hidden = true) HttpSession session) {
        return aiService.generateProductDesc(req,file, session);
    }

	// 2. 活動文案 (含圖片，使用 consumes = MULTIPART_FORM_DATA_VALUE)
	@PostMapping(value = "/promo-copy", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@Operation(summary = "生成活動宣傳文案", description = "上傳活動圖片並輸入活動名稱，自動生成社群媒體用的宣傳文案")
	public AiRes generatePromoCopy(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) 
            @Valid @RequestPart("data") AiPromotionsReq req,
            
            @Parameter(description = "活動宣傳圖片") 
            @RequestPart("file") MultipartFile file, 
            
            @Parameter(hidden = true) HttpSession session) {
        return aiService.generatePromoCopy(req, file, session);
    }
}
