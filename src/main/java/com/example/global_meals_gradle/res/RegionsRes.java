package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.entity.Regions;

public class RegionsRes extends BasicRes{

	private List<Regions> regionsList;

	public RegionsRes() {
		super();
	}

	public RegionsRes(int code, String message) {
		super(code, message);
	}

	public RegionsRes(int code, String message, List<Regions> regionsList) {
		super(code, message);
		this.regionsList = regionsList;
	}

	public List<Regions> getRegionsList() {
		return regionsList;
	}

	public void setRegionsList(List<Regions> regionsList) {
		this.regionsList = regionsList;
	}
}
