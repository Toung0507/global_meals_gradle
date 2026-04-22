package com.example.global_meals_gradle.req;

import org.hibernate.validator.constraints.Length;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

public class RegisterMembersReq {
	
	@NotBlank(message = ValidationMsg.NAME_ERROR)
	private String name;
	
	@NotBlank(message = ValidationMsg.PHONE_ERROR)
	@Length(min = 10, max = 10, message = ValidationMsg.PHONE_ERROR) // 電話號碼10碼
	private String phone;
	
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
