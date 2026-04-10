package com.example.global_meals_gradle.req;

public class RegisterReq {
	
	private String name;
    private String account;
    private String password;
    private String role;
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
