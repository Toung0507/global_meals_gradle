package com.example.global_meals_gradle.entity;

import java.time.LocalDate;

import com.example.global_meals_gradle.constants.StaffRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff")
public class Staff {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "name")
	private String name;

	@Column(name = "account", unique = true)
	private String account;

	@Column(name = "password")
	private String password;

	@Enumerated(EnumType.STRING) // 關鍵：存儲字串
	@Column(name = "role")
	private StaffRole role;

	@Column(name = "global_area_id")
	private int globalAreaId;

	@Column(name = "is_status")
	private boolean status;

	@Column(name = "hire_at")
	private LocalDate hireAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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

	public StaffRole getRole() {
		return role;
	}

	public void setRole(StaffRole role) {
		this.role = role;
	}

	public int getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public LocalDate getHireAt() {
		return hireAt;
	}

	public void setHireAt(LocalDate hireAt) {
		this.hireAt = hireAt;
	}

}
