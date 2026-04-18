package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.MonthlyProductsSalesReq;
import com.example.global_meals_gradle.res.MonthlyProductsSalesRes;
import com.example.global_meals_gradle.service.ProductService;
import jakarta.validation.Valid;

@RestController
public class ProductsController {

	@Autowired
	private ProductService productsService;

	/* 查詢某年某月的商品總銷售量 */
	@PostMapping("products/monthly_products_sales")
	public MonthlyProductsSalesRes getMonthlyProductsSales(@Valid @RequestBody MonthlyProductsSalesReq req) {
		return productsService.getMonthlyProductsSales(req);
	}
}
