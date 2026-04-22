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

import jakarta.servlet.http.HttpSession;

@RestController
//全局設定此模組的開頭都是 inventory
@RequestMapping("/inventory")
public class BranchInventoryController {
	@Autowired
	private BranchInventoryService branchInventoryService;

	// 1. 處理庫存批次更新
	@PostMapping("/update")
	public BranchInventoryRes updateInventory(@Validated @RequestBody List<BranchInventoryUpdateReq> reqList) {
		return branchInventoryService.updateInventory(reqList);
	}

	// 2. 用分店 ID 查該店所有商品庫存
	// URL: /inventory/branch/5
	@GetMapping("/branch/{globalAreaId}")
	public BranchInventoryRes getInventoryByArea(@PathVariable int globalAreaId, HttpSession session) {
		return branchInventoryService.getInventoryByGlobalAreaId(globalAreaId, session);
	}

	// 3. 用商品 ID 查該商品在所有分店的庫存
	// URL: /inventory/product/101
	@GetMapping("/product/{productId}")
	public BranchInventoryRes getInventoryByProduct(@PathVariable int productId, HttpSession session) {
		return branchInventoryService.getInventoryByProductId(productId, session);
	}

	// 4. 分店取得菜單 (假設這個方法不需要 Session 驗證)
	// URL: /inventory/menu/5
	@GetMapping("/menu/{globalAreaId}")
	public MenuListRes getMenuByArea(@PathVariable int globalAreaId) {
		return branchInventoryService.getMenuByArea(globalAreaId);
	}
}
