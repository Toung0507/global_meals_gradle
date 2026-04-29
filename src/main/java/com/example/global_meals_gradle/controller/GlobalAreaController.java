package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/global-area")
@Tag(name = "分店管理模組", description = "提供分店的新增、更新、刪除與列表查詢功能")
public class GlobalAreaController {
	
	@Autowired
	private GlobalAreaService globalAreaService;
	
	// 新增分店
	@PostMapping("create")
	@Operation(summary = "新增分店", description = "在系統中建立一個新的營業分店")
	public BasicRes create(@Valid @RequestBody CreateGlobalAreaReq req) {
		return globalAreaService.create(req);
	}
	
	// 修改分店
	@PostMapping("update")
	@Operation(summary = "修改分店資訊", description = "根據分店 ID 更新其基本資料")
	public BasicRes update(@Valid @RequestBody UpdateGlobalAreaReq req) {
		return globalAreaService.update(req);
	}
	
	// 取得分店清單
	@GetMapping("get_all_branch")
	@Operation(summary = "取得分店清單", description = "獲取目前系統中所有有效的分店列表")
	public GlobalAreaRes getAllBranch() {
		return globalAreaService.getAllBranch();
	}
	
	// 刪除分店
	@PostMapping("delete")
	@Operation(summary = "刪除分店", description = "將指定分店從系統中刪除")
	public BasicRes delete(@Valid @RequestBody DeleteGlobalAreaReq req) {
		return globalAreaService.delete(req);
	}

}
