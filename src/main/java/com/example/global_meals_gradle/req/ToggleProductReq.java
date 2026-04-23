package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.NotNull;

public class ToggleProductReq {

	@NotNull(message = "商品 ID 不能為空")
	private Integer id;

	private boolean active;

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
}
