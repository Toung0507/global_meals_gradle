package com.example.global_meals_gradle.req;

import java.util.List;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotEmpty;

public class DeleteGlobalAreaReq {
	
	@NotEmpty(message = ValidationMsg.GLOBAL_AREA_ID_LIST_ERROR)
	private List<Integer> globalAreaIdList;

	public DeleteGlobalAreaReq() {
		super();
	}

	public DeleteGlobalAreaReq(List<Integer> globalAreaIdList) {
		super();
		this.globalAreaIdList = globalAreaIdList;
	}

	public List<Integer> getGlobalAreaIdList() {
		return globalAreaIdList;
	}

	public void setGlobalAreaIdList(List<Integer> globalAreaIdList) {
		this.globalAreaIdList = globalAreaIdList;
	}
	
}
