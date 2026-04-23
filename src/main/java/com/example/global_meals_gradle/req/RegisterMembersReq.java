package com.example.global_meals_gradle.req;

import org.hibernate.validator.constraints.Length;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterMembersReq {
	
	@NotBlank(message = ValidationMsg.NAME_ERROR)
	private String name;
	
	@NotBlank(message = ValidationMsg.PHONE_ERROR)
	@Length(min = 10, max = 10, message = ValidationMsg.PHONE_ERROR) // 電話號碼10碼
	private String phone;

	/* 前端 BranchService 傳入，合法值：TW / JP / KR，預設 TW */
	private String country = "TW";

	@Size(min = 8, message = ValidationMsg.PASSWORD_ERROR)
	private String password;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
