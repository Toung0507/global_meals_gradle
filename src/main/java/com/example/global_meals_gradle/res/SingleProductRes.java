package com.example.global_meals_gradle.res;

import com.example.global_meals_gradle.vo.MenuVo;

public class SingleProductRes extends BasicRes {
	private MenuVo singleProduct;

	public SingleProductRes() {
		super();
	}

	public SingleProductRes(int code, String message) {
		super(code, message);
	}

	public SingleProductRes(int code, String message, MenuVo singleProduct) {
		super(code, message);
		this.singleProduct = singleProduct;
	}

	public MenuVo getSingleProduct() {
		return singleProduct;
	}

	public void setSingleProduct(MenuVo singleProduct) {
		this.singleProduct = singleProduct;
	}

}
