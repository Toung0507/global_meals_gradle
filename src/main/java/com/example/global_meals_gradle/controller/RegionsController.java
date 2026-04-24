package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.UpdateRegionsUsageCapReq;
import com.example.global_meals_gradle.req.UpsertRegionsTaxReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.RegionsRes;
import com.example.global_meals_gradle.service.RegionsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/regions") // 將原本的 /lazybaobao 邏輯收攏於 /regions，API 結構更直觀
@Tag(name = "區域稅務管理模組", description = "處理國家稅率設定、使用上限與稅務清單查詢")
public class RegionsController {
	
	@Autowired
	private RegionsService regionsService;
	
	// 新增/修改國家稅值
	@PostMapping("upsert")
	@Operation(summary = "新增或修改國家稅值", description = "若該國家稅務設定不存在則新增，存在則更新")
	public BasicRes upsert(@Valid @RequestBody UpsertRegionsTaxReq req) {
		return regionsService.upsert(req);
	}
	
	// 更改國家稅值
	@PostMapping("update_usage_cap")
	@Operation(summary = "更新使用上限", description = "修改指定國家的使用上限設定")
	public BasicRes updateUsageCap(@Valid @RequestBody UpdateRegionsUsageCapReq req) {
		return regionsService.updateUsageCap(req);
	}
	
	// 取得各國稅率清單
	@GetMapping("get_all_tax")
	@Operation(summary = "取得各國稅率清單", description = "查詢系統內所有國家的稅率與配置清單")
	public RegionsRes getAllTax() {
		return regionsService.getAllTax();
	}

}
