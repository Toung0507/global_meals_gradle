package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.InventoryDetailVo;

public class BranchInventoryRes extends BaseListRes<InventoryDetailVo> {
	public BranchInventoryRes() {
		super();
	}

	public BranchInventoryRes(int code, String message) {
		super(code, message);
	}

	public BranchInventoryRes(int code, String message, List<InventoryDetailVo> data) {
		super(code, message, data);
	}
}
