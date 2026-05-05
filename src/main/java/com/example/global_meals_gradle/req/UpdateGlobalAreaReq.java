package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdateGlobalAreaReq {

	@Min(value = 1, message = ValidationMsg.GLOBAL_AREA_ID_ERROR)
	private int id;
	
	private String branch;

	private String address;

    @Size(max = 16, message = ValidationMsg.PHONE_ERROR)
	private String phone;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
}
