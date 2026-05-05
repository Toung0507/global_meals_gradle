package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.NotBlank;


//修改密碼的請求體
//targetId 同樣來自 URL 的 {id} 路徑變數，body 只放「新密碼」
public class ResetStaffPasswordReq {

//	@NotBlank(message = ValidationMsg.OLD_PASSWORD_ERROR)
//	private String oldPassword;

	@NotBlank(message = ValidationMsg.NEW_PASSWORD_ERROR)
	private String newPassword;

//	public String getOldPassword() {
//		return oldPassword;
//	}
//
//	public void setOldPassword(String oldPassword) {
//		this.oldPassword = oldPassword;
//	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}