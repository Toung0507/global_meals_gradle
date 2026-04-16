package com.example.global_meals_gradle.req;

import org.hibernate.validator.constraints.Length;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginMembersReq {
	
	@NotBlank(message = ValidationMsg.PHONE_ERROR)
	@Length(min = 10, max = 10, message = ValidationMsg.PHONE_ERROR) // 電話號碼10碼
	private String phone;
	
	@NotBlank(message = ValidationMsg.PASSWORD_ERROR)
	@Size(min = 6, message = ValidationMsg.PASSWORD_ERROR) // 密碼至少6碼
	private String password;

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
