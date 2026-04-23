package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.UpsertRegionsTaxReq;
import com.example.global_meals_gradle.req.UpdateRegionsUsageCapReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.RegionsRes;
import com.example.global_meals_gradle.service.RegionsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/regions")   // 合併進來
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true") // 允許 Angular 跨域呼叫
public class RegionsController {
	
	@Autowired
	private RegionsService regionsService;
	
	// 新增/修改國家稅值
	@PostMapping("/upsert")
	public BasicRes upsert(@Valid @RequestBody UpsertRegionsTaxReq req) {
		return regionsService.upsert(req);
	}
	
	// 更改國家稅值
	@PostMapping("/update_usage_cap")
	public BasicRes updateUsageCap(@Valid @RequestBody UpdateRegionsUsageCapReq req) {
		return regionsService.updateUsageCap(req);
	}
	
	// 取得各國稅率清單
	@GetMapping("/get_all_tax")
	public RegionsRes getAllTax() {
		return regionsService.getAllTax();
	}

}
