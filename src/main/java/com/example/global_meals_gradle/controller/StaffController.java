package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.service.StaffService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class StaffController {

	@Autowired
	private StaffService staffService;
	
}
