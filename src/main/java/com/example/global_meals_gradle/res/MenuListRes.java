package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.MenuVo;

public class MenuListRes extends BaseListRes<MenuVo> {
	public MenuListRes(int code, String message, List<MenuVo> data) {
		super(code, message, data);
	}
}
