package com.example.global_meals_gradle.req;

import org.hibernate.validator.constraints.Length;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

public class CreateGlobalAreaReq {

	@NotBlank(message = ValidationMsg.COUNTRY_ERROR)
	private String country;

	@NotBlank(message = ValidationMsg.BRANCH_ERROR)
	private String branch;

	@NotBlank(message = ValidationMsg.ADDRESS_ERROR)
	private String address;

	@NotBlank(message = ValidationMsg.PHONE_ERROR) // 避免有10個空格輸入
	@Length(min = 10, max = 10, message = ValidationMsg.PHONE_ERROR) // 電話號碼10碼
	private String phone;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
