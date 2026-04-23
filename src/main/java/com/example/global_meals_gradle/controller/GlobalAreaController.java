package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CreateGlobalAreaReq;
import com.example.global_meals_gradle.req.DeleteGlobalAreaReq;
import com.example.global_meals_gradle.req.UpdateGlobalAreaReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.GlobalAreaRes;
import com.example.global_meals_gradle.service.GlobalAreaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/lazybaobao") // 統一加上 lazybaobao 前綴，與前端 api.config.ts 路由一致
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class GlobalAreaController {
	
	@Autowired
	private GlobalAreaService globalAreaService;
	
	// 新增分店
	@PostMapping("global_area/create")
	public BasicRes create(@Valid @RequestBody CreateGlobalAreaReq req) {
		return globalAreaService.create(req);
	}
	
	// 修改分店
	@PostMapping("global_area/update")
	public BasicRes update(@Valid @RequestBody UpdateGlobalAreaReq req) {
		return globalAreaService.update(req);
	}
	
	// 取得分店清單
	@GetMapping("global_area/get_all_branch")
	public GlobalAreaRes getAllBranch() {
		return globalAreaService.getAllBranch();
	}
	
	// 刪除分店
	@PostMapping("global_area/delete")
	public BasicRes delete(@Valid @RequestBody DeleteGlobalAreaReq req) {
		return globalAreaService.delete(req);
	}

}
