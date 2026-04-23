package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.InventoryDetailVo;
import com.example.global_meals_gradle.vo.ProductAdminVo;

public class AdminProductRes extends BasicRes {
	private ProductAdminVo product; // 單一商品
	private List<ProductAdminVo> productList; // 商品清單
	private List<InventoryDetailVo> inventoryList; // 庫存清單

	public AdminProductRes() {
		super();
	}

	public AdminProductRes(int code, String message) {
		super(code, message);
	}

	// 1. 專門給「建立/修改」使用的 (包含商品資訊 + 庫存)
	public AdminProductRes(int code, String message, ProductAdminVo product, List<InventoryDetailVo> inventoryList) {
		super(code, message);
		this.product = product;
		this.inventoryList = inventoryList;
	}

	// 2. 專門給「清單查詢」使用的 (只要回傳商品清單)
	public AdminProductRes(int code, String message, List<ProductAdminVo> productList) {
		super(code, message);
		this.productList = productList;
	}

	// 3. 專門給「查詢單一商品」使用的
	public AdminProductRes(int code, String message, ProductAdminVo product) {
		super(code, message);
		this.product = product;
	}

	public ProductAdminVo getProduct() {
		return product;
	}

	public void setProduct(ProductAdminVo product) {
		this.product = product;
	}

	public List<ProductAdminVo> getProductList() {
		return productList;
	}

	public void setProductList(List<ProductAdminVo> productList) {
		this.productList = productList;
	}

	public List<InventoryDetailVo> getInventoryList() {
		return inventoryList;
	}

	public void setInventoryList(List<InventoryDetailVo> inventoryList) {
		this.inventoryList = inventoryList;
	}

}
