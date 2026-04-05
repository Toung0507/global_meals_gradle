package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CreateRegionsReq;
import com.example.global_meals_gradle.req.UpdateRegionsReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.RegionsRes;
import com.example.global_meals_gradle.service.RegionsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/lazybaobao")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true") // 允許 Angular 跨域呼叫
public class RegionsController {
	
	@Autowired
	private RegionsService regionsService;
	
	// 新增國家稅值
	@PostMapping("regions/create")
	public BasicRes create(@Valid @RequestBody CreateRegionsReq req) {
		return regionsService.create(req);
	}
	
	// 更改國家稅值
	@PostMapping("regions/update")
	public BasicRes update(@Valid @RequestBody UpdateRegionsReq req) {
		return regionsService.update(req);
	}
	
	// 取得各國稅率清單
	@GetMapping("regions/get_all_tax")
	public RegionsRes getAllTax() {
		return regionsService.getAllTax();
	}

}
