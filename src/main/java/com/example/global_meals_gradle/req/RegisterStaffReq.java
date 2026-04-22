package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/*註冊員工需求*/
public class RegisterStaffReq {
	
	/*
	 * @NotBlank: 限制屬性值不能是 1.空子串 2.全空白字串 3.null message
	 * 是指當屬性值違反限制時得到的訊息，等號後面的值必須是常數(final)
	 */
	@NotBlank(message = ValidationMsg.NAME_ERROR)
	private String name;
	
//	@NotBlank(message = ValidationMsg.ACCOUNT_ERROR)//因為帳號是自動產生的
    private String account;
    
	@NotBlank(message = ValidationMsg.PASSWORD_ERROR)
    private String password;
    
	@NotBlank(message = ValidationMsg.ROLE_ERROR)
    private String role;
    
	@Min(value = 1, message = ValidationMsg.GLOBAL_AREA_ID_ERROR)
    private int globalAreaId;
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public int getGlobalAreaId() {
		return globalAreaId;
	}
	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}
	
    
}
