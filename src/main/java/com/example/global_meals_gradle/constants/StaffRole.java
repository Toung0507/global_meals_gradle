package com.example.global_meals_gradle.constants;

public enum StaffRole {

	ADMIN("ADMIN"), //
	REGION_MANAGER("REGION_MANAGER"), //
	MANAGER_AGENT("MANAGER_AGENT"), //新增副店長
	STAFF("STAFF");

	private String staffRole;

	private StaffRole(String staffRole) {
		this.staffRole = staffRole;
	}

	public String getStaffRole() {
		return staffRole;
	}

	public void setStaffRole(String staffRole) {
		this.staffRole = staffRole;
	}

}
