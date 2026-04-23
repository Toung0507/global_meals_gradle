package com.example.global_meals_gradle.res;

import java.util.List;

public class BaseListRes<T> extends BasicRes {
	private List<T> data;

	public BaseListRes() {
		super();
	}

	public BaseListRes(int code, String message) {
		super(code, message);
	}

	public BaseListRes(int code, String message, List<T> data) {
		super(code, message);
		this.data = data;
	}

	public BaseListRes(List<T> data) {
		super();
		this.data = data;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

}
