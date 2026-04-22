package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.global_meals_gradle.req.ProductCreateReq;
import com.example.global_meals_gradle.req.ProductUpdateReq;
import com.example.global_meals_gradle.res.AdminProductRes;
import com.example.global_meals_gradle.service.ProductService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
//全局設定此模組的開頭都是 /product
@RequestMapping("/product")
public class ProductController {

	@Autowired
	private ProductService productService;

	@PostMapping(value = "/create", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public AdminProductRes createProduct(@RequestPart("data") @Valid ProductCreateReq req,
			@RequestPart("file") MultipartFile file, HttpSession session) {
		return productService.createProduct(req, file, session);
	}

	@PostMapping(value = "/update", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public AdminProductRes updateProduct(@RequestPart("data") @Valid ProductUpdateReq req, //
			@RequestPart(value = "file", required = false) MultipartFile file, HttpSession session) {
		return productService.updateProduct(req, file, session);
	}

	@GetMapping("/list")
	public AdminProductRes getActiveProducts(HttpSession session) {
		return productService.getActiveProducts(session);
	}

	@GetMapping("/trash")
	public AdminProductRes getDeletedProducts(HttpSession session) {
		return productService.getDeletedProducts(session);
	}

	@GetMapping("/detail/{id}")
	public AdminProductRes getProductDetail(@PathVariable int id, HttpSession session) {
		return productService.getProductById(id, session);
	}

	// 使用 PATCH 代表「部分更新 (Partial Update)」，比起 POST 更精準
	// url = /product/status/5?active=?
	@PatchMapping("/status/{id}")
	public AdminProductRes updateActiveStatus(@PathVariable int id, //
			@RequestParam(value = "active") boolean active, HttpSession session) {
		return productService.updateActiveStatus(id, active, session);
	}
}
