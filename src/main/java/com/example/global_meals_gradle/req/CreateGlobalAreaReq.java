package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;
import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateGlobalAreaReq {

	@Min(value = 1, message = ValidationMsg.REGIONS_ID_ERROR)
	@JsonAlias("regions_id")
	private int regionsId;

	@NotBlank(message = ValidationMsg.BRANCH_ERROR)
	private String branch;

	@NotBlank(message = ValidationMsg.ADDRESS_ERROR)
	private String address;

	@NotBlank(message = ValidationMsg.PHONE_ERROR) // 避免有10個空格輸入
    @Size(min = 7, max = 16, message = ValidationMsg.PHONE_ERROR) // 合理的國際電話長度範圍 (通常 7~15 碼)
	private String phone;

	public int getRegionsId() {
		return regionsId;
	}

	public void setRegionsId(int regionsId) {
		this.regionsId = regionsId;
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
