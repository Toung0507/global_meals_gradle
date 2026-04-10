package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;

public class LoginStaffReq {

	/*
	 * @NotBlank: 限制屬性值不能是 1.空子串 2.全空白字串 3.null message
	 * 是指當屬性值違反限制時得到的訊息，等號後面的值必須是常數(final)
	 */
	@NotBlank(message = ValidationMsg.ACCOUNT_CANNOT_BE_BLANK)
	private String account;
	
	@NotBlank(message = ValidationMsg.PASSWORD_CANNOT_BE_BLANK)
    private String password;
    
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
