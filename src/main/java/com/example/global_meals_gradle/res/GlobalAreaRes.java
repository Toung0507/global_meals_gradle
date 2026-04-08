package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.entity.GlobalArea;

public class GlobalAreaRes extends BasicRes {

	private List<GlobalArea> globalAreaList;

	public GlobalAreaRes() {
		super();
	}

	public GlobalAreaRes(int code, String message) {
		super(code, message);
		// TODO Auto-generated constructor stub
	}

	public GlobalAreaRes(int code, String message, List<GlobalArea> globalAreaList) {
		super(code, message);
		this.globalAreaList = globalAreaList;
	}

	public List<GlobalArea> getGlobalAreaList() {
		return globalAreaList;
	}

	public void setGlobalAreaList(List<GlobalArea> globalAreaList) {
		this.globalAreaList = globalAreaList;
	}

}
