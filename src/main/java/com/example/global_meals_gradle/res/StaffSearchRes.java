package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.entity.Staff;

public class StaffSearchRes extends BasicRes {

	private List<Staff> staffList;

	public StaffSearchRes() {
		super();
	}

	public StaffSearchRes(int code, String message) {
		super(code, message);
	}

	public StaffSearchRes(List<Staff> staffList) {
		super();
		this.staffList = staffList;
	}

	public StaffSearchRes(int code, String message, List<Staff> staffList) {
		super(code, message);
		this.staffList = staffList;
	}

	public List<Staff> getStaffList() {
		return staffList;
	}

	public void setStaffList(List<Staff> staffList) {
		this.staffList = staffList;
	}

}
