package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginMembersReq {
	
	@NotBlank(message = ValidationMsg.PHONE_ERROR)
	private String phone;
	
	// 驗證國家代碼：必須為 2 碼大寫英文字母
//	@NotBlank(message = ValidationMsg.COUNTRY_CODE_CANT_BE_EMPTY)
//	@Pattern(regexp = "^[A-Z]{2}$", message = ValidationMsg.COUNTRY_CODE_ERROR)
//	private String countryCode = "TW";
	
	@Min(value = 1, message = ValidationMsg.REGIONS_ID_ERROR)
	private int regionsId;
	
	@NotBlank(message = ValidationMsg.PASSWORD_ERROR)
	@Size(min = 6, message = ValidationMsg.PASSWORD_ERROR) // 密碼至少6碼
	private String password;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getRegionsId() {
		return regionsId;
	}

	public void setRegionsId(int regionsId) {
		this.regionsId = regionsId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
