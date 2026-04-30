package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.UpdateRegionsReq;
import com.example.global_meals_gradle.req.CreateRegionsReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.RegionsRes;
import com.example.global_meals_gradle.service.RegionsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/regions") // 將原本的 /lazybaobao 邏輯收攏於 /regions，API 結構更直觀
@Tag(name = "區域稅務管理模組", description = "處理國家稅率設定、使用上限與稅務清單查詢")
public class RegionsController {
	
	@Autowired
	private RegionsService regionsService;
	
	// 新增
	@PostMapping("insert")
	@Operation(summary = "新增國家稅值與折扣上限", description = "新增國家稅務設定與國家折扣上限")
	public BasicRes insert(@Valid @RequestBody CreateRegionsReq req, @Parameter(hidden = true) HttpSession session) {
		return regionsService.insert(req, session);
	}
	
	// 更改
	@PostMapping("update")
	@Operation(summary = "更新國家基本設定", description = "修改指定國家的基本設定")
	public BasicRes updateUsageCap(@Valid @RequestBody UpdateRegionsReq req, @Parameter(hidden = true) HttpSession session) {
		return regionsService.update(req, session);
	}
	
	// 取得各國稅率清單
	@GetMapping("get_all")
	@Operation(summary = "取得各國基本設定清單", description = "查詢系統內所有國家的稅率與配置清單")
	public RegionsRes getAll(@Parameter(hidden = true) HttpSession session) {
		return regionsService.getAll(session);
	}

}
