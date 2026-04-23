package com.example.global_meals_gradle.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "global_area")
public class GlobalArea {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "regions_id")
	private int regionsId;

	@Column(name = "branch")
	private String branch;

	@Column(name = "address")
	private String address;

	@Column(name = "phone")
	private String phone;
	
	// 在 GlobalArea.java 加上這兩個欄位和對應的 getter/setter
	@Column(name = "country")
	private String country;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@Column(name = "country_code")
	private String countryCode;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRegionsId() {
		return regionsId;
	}

	public void setRegionsId(int regionsId) {
		this.regionsId = regionsId;
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

}