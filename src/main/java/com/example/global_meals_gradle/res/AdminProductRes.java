package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.entity.BranchInventory;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.vo.ProductAdminVo;

//專門給總部建立/查詢商品時回傳用
public class AdminProductRes extends BasicRes {
	private ProductAdminVo product;
	private List<BranchInventory> inventoryList; // 管理員想看各區庫存

	public AdminProductRes() {
		super();
	}

	public AdminProductRes(int code, String message) {
		super(code, message);
	}

	public AdminProductRes(int code, String message, ProductAdminVo product, List<BranchInventory> inventoryList) {
		super(code, message);
		this.product = product;
		this.inventoryList = inventoryList;
	}

	public AdminProductRes(ProductAdminVo product, List<BranchInventory> inventoryList) {
		super();
		this.product = product;
		this.inventoryList = inventoryList;
	}

	public ProductAdminVo getProduct() {
		return product;
	}

	public void setProduct(ProductAdminVo product) {
		this.product = product;
	}

	public List<BranchInventory> getInventoryList() {
		return inventoryList;
	}

	public void setInventoryList(List<BranchInventory> inventoryList) {
		this.inventoryList = inventoryList;
	}

}
