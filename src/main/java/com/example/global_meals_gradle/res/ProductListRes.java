package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.entity.Products;

public class ProductListRes extends BaseListRes<Products> {
	public ProductListRes(int code, String message, List<Products> data) {
		super(code, message, data);
	}
}
