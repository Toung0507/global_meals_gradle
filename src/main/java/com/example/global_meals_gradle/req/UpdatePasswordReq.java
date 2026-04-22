package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePasswordReq {

	@Min(value = 1, message = ValidationMsg.MEMBERS_ID_ERROR)
	private int id;

	@NotBlank(message = ValidationMsg.PASSWORD_ERROR)
	@Size(min = 6, message = ValidationMsg.PASSWORD_ERROR) // 密碼至少6碼
	private String oldPassword;

	@NotBlank(message = ValidationMsg.PASSWORD_ERROR)
	@Size(min = 6, message = ValidationMsg.PASSWORD_ERROR) // 密碼至少6碼
	private String newPassword;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

}
