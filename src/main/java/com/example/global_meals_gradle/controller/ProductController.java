package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.ProductCreateReq;
import com.example.global_meals_gradle.req.ProductUpdateReq;
import com.example.global_meals_gradle.req.ToggleProductReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.ProductsRes;
import com.example.global_meals_gradle.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	// GET lazybaobao/products/active?globalAreaId=X
	@GetMapping("/active")
	public ProductsRes getActive(@RequestParam("globalAreaId") int globalAreaId) {
		return productService.getActiveProducts(globalAreaId);
	}

	// GET lazybaobao/products/get_all?globalAreaId=X
	@GetMapping("/get_all")
	public ProductsRes getAll(@RequestParam("globalAreaId") int globalAreaId) {
		return productService.getAllProducts(globalAreaId);
	}

	// GET lazybaobao/products/{id}/image
	@GetMapping("/{id}/image")
	public String getImage(@PathVariable("id") int id) {
		return productService.getProductImage(id);
	}

	// POST lazybaobao/products/create
	@PostMapping("/create")
	public BasicRes create(@Valid @RequestBody ProductCreateReq req) {
		return productService.createProduct(req);
	}

	// POST lazybaobao/products/update
	@PostMapping("/update")
	public BasicRes update(@Valid @RequestBody ProductUpdateReq req) {
		return productService.updateProduct(req);
	}

	// POST lazybaobao/products/toggle
	@PostMapping("/toggle")
	public BasicRes toggle(@Valid @RequestBody ToggleProductReq req) {
		return productService.toggleProduct(req);
	}
}
