package com.example.global_meals_gradle.res;

import java.util.List;
import com.example.global_meals_gradle.vo.ProductVO;

public class ProductsRes extends BasicRes {

	private List<ProductVO> products;

	public ProductsRes(int code, String message, List<ProductVO> products) {
		super(code, message);
		this.products = products;
	}

	public List<ProductVO> getProducts() { return products; }
	public void setProducts(List<ProductVO> products) { this.products = products; }
}
