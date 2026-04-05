package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.NotNull;

public class ProductUpdateReq extends ProductCreateReq {
	@NotNull(message = "更新時必須提供 ID")
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
