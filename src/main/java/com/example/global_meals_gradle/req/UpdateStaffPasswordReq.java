package com.example.global_meals_gradle.req;

// 修改密碼專用的請求盒子！
// 需要確認你記得舊密碼，才能換新密碼，不然誰都能改你密碼那不就糟糕了 😱
public class UpdateStaffPasswordReq {

	// 帳號（拿來找到是哪個人）
	private String account;

	// 舊密碼（先確認你是本人！）
	private String oldPassword;

	// 新密碼（你想換成什麼）
	private String newPassword;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
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