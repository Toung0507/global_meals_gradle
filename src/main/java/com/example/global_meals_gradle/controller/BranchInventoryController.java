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
@RequestMapping("/branch_inventory") // WebConfig 自動補 /lazybaobao，最終為 /lazybaobao/branch_inventory
public class BranchInventoryController {

	@Autowired
	private BranchInventoryService branchInventoryService;

	// POST /lazybaobao/branch_inventory/update
	@PostMapping("/update")
	public BranchInventoryRes updateInventory(@Validated @RequestBody List<BranchInventoryUpdateReq> reqList) {
		return branchInventoryService.updateInventory(reqList);
	}

	// GET /lazybaobao/branch_inventory/{globalAreaId}
	@GetMapping("/{globalAreaId}")
	public BranchInventoryRes getInventoryByArea(@PathVariable("globalAreaId") int globalAreaId, HttpSession session) {
		return branchInventoryService.getInventoryByGlobalAreaId(globalAreaId, session);
	}

	// GET /lazybaobao/branch_inventory/product/{productId}
	@GetMapping("/product/{productId}")
	public BranchInventoryRes getInventoryByProduct(@PathVariable("productId") int productId, HttpSession session) {
		return branchInventoryService.getInventoryByProductId(productId, session);
	}

	// GET /lazybaobao/branch_inventory/menu/{globalAreaId}
	// ⚠️ 加上 /menu/ 前綴，避免與 /{globalAreaId} 路徑衝突
	@GetMapping("/menu/{globalAreaId}")
	public MenuListRes getMenuByArea(@PathVariable("globalAreaId") int globalAreaId) {
		return branchInventoryService.getMenuByArea(globalAreaId);
	}
}