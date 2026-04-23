package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.UpdateBranchInventoryReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.BranchInventoryRes;
import com.example.global_meals_gradle.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("lazybaobao/branch_inventory")
public class BranchInventoryController {

	@Autowired
	private ProductService productService;

	// GET lazybaobao/branch_inventory/{areaId}
	@GetMapping("/{areaId}")
	public BranchInventoryRes getByArea(@PathVariable("areaId") int areaId) {
		return productService.getBranchInventory(areaId);
	}

	// POST lazybaobao/branch_inventory/update
	@PostMapping("/update")
	public BasicRes update(@Valid @RequestBody UpdateBranchInventoryReq req) {
		return productService.updateBranchInventory(req);
	}
}
