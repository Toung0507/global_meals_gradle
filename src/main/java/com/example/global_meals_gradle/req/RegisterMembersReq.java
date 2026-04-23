package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterMembersReq {
	
	@NotBlank(message = ValidationMsg.NAME_ERROR)
	private String name;
	
	// 合理的國際電話長度範圍 (通常 7~15 碼)
	@NotBlank(message = ValidationMsg.PHONE_ERROR)
    @Size(min = 7, max = 16, message = ValidationMsg.PHONE_ERROR)
	private String phone;
	
	// 國家代碼（ISO 3166-1 alpha-2）：例如 "TW"、"JP"、"KR"
    // 用途：傳給 PhoneValidatorUtil.isValid(phone, countryCode) 做各國格式驗證
    // 預設值為 "TW"（台灣），若前端沒傳，預設以台灣驗證（維持向後相容）
	// 驗證國家代碼：必須為 2 碼大寫英文字母
	@NotBlank(message = ValidationMsg.COUNTRY_CODE_CANT_BE_EMPTY)
	@Pattern(regexp = "^[A-Z]{2}$", message = ValidationMsg.COUNTRY_CODE_ERROR)
    private String countryCode = "TW";
	
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

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
