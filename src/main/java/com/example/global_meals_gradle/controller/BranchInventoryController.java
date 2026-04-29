package com.example.global_meals_gradle.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.BranchInventoryUpdateReq;
import com.example.global_meals_gradle.res.BranchInventoryRes;
import com.example.global_meals_gradle.res.MenuListRes;
import com.example.global_meals_gradle.service.BranchInventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;

@RestController
//全局設定此模組的開頭都是 inventory
@RequestMapping("/inventory")
@Tag(name = "分店庫存管理模組",description = "處理分店庫存的更新、查詢及菜單相關業務")
public class BranchInventoryController {
	@Autowired
	private BranchInventoryService branchInventoryService;

	// 1. 處理庫存批次更新
	@PostMapping("/update")
	@Operation(summary = "批次更新分店相關資料", description = "接收一組分店更新商品請求，批次調整商品在分店中的數量、價格、最大購買量")
	public BranchInventoryRes updateInventory( //
			@Validated @RequestBody List<BranchInventoryUpdateReq> reqList) {
		return branchInventoryService.updateInventory(reqList);
	}

	// 2. 用分店 ID 查該店所有商品庫存
	// URL: /inventory/branch/5
	@GetMapping("/branch/{globalAreaId}")
	@Operation(summary = "查詢分店所有商品庫存", description = "透過分店 ID 查詢該店內所有商品的當前庫存狀況")
	public BranchInventoryRes getInventoryByArea(
			@Parameter(description = "分店 ID", example = "5") @PathVariable("globalAreaId") int globalAreaId,
			@Parameter(hidden = true) HttpSession session) {
		return branchInventoryService.getInventoryByGlobalAreaId(globalAreaId, session);
	}

	// 3. 用商品 ID 查該商品在所有分店的庫存
	// URL: /inventory/product/101
	@GetMapping("/product/{productId}")
	@Operation(summary = "查詢特定商品庫存", description = "查詢某個商品在所有分店的庫存分佈狀況")
	public BranchInventoryRes getInventoryByProduct( //
			@Parameter(description = "商品 ID", example = "101") @PathVariable("productId") int productId, //
			@Parameter(hidden = true) HttpSession session) {
		return branchInventoryService.getInventoryByProductId(productId, session);
	}

	// 4. 分店取得菜單
	// URL: /inventory/menu/5
	@GetMapping("/menu/{globalAreaId}")
	@Operation(summary = "取得分店菜單", description = "根據分店 ID 獲取該分店目前販售的菜單列表")
	public MenuListRes getMenuByArea( //
			@Parameter(description = "分店 ID", example = "5") @PathVariable("globalAreaId") int globalAreaId) {
		return branchInventoryService.getMenuByArea(globalAreaId);
	}
}
