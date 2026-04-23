package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.NotNull;

public class ProductUpdateReq {

	@NotNull(message = "更新時必須提供 ID")
	private Integer id;

	private String name;
	private String category;
	private String description;
	private String imageBase64;

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getImageBase64() { return imageBase64; }
	public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
