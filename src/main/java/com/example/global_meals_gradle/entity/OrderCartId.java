package com.example.global_meals_gradle.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OrderCartId implements Serializable {
	
	private String dateId;
	
	private String id;

	public String getDateId() {
		return dateId;
	}

	public void setDateId(String dateId) {
		this.dateId = dateId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

}
