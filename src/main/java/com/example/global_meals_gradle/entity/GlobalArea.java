package com.example.global_meals_gradle.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "global_area")
public class GlobalArea {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "branch")
	private String branch;

	@Column(name = "address")
	private String address;

	@Column(name = "phone")
	private String phone;

	@Column(name = "regions_id")
	private int regionsId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

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

}
