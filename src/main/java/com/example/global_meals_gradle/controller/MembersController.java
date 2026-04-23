package com.example.global_meals_gradle.controller;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.req.LoginMembersReq;
import com.example.global_meals_gradle.req.RegisterMembersReq;
import com.example.global_meals_gradle.req.UpdatePasswordReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.MembersRes;
import com.example.global_meals_gradle.service.MembersService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/members") // WebConfig 自動補 /lazybaobao，最終為 /lazybaobao/members
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MembersController {

	@Autowired
	private MembersService membersService;

	private final String ATTRIBUTE_KEY = "check_result";

	// POST /lazybaobao/members/register_guest
	@PostMapping("/register_guest")
	public BasicRes registerGuest(@Valid @RequestBody RegisterMembersReq req) {
		return membersService.register(req, false);
	}

	// POST /lazybaobao/members/register_member
	@PostMapping("/register_member")
	public BasicRes registerMember(@Valid @RequestBody RegisterMembersReq req) {
		return membersService.register(req, true);
	}

	// POST /lazybaobao/members/login
	@PostMapping("/login")
	public MembersRes login(@Valid @RequestBody LoginMembersReq req, HttpSession session) {
		session.setMaxInactiveInterval(7200); // 兩小時
		System.out.println(LocalTime.now().withNano(0) + " -- "
				+ req.getPhone() + "：" + session.getId());
		MembersRes res = membersService.login(req);
		if (res.getCode() == ReplyMessage.SUCCESS.getCode()) {
			session.setAttribute(ATTRIBUTE_KEY, res);
		}
		return res;
	}

	// GET /lazybaobao/members/logout
	@GetMapping("/logout")
	public MembersRes logout(HttpSession session) {
		session.invalidate();
		return new MembersRes(ReplyMessage.SUCCESS.getCode(),
				ReplyMessage.SUCCESS.getMessage());
	}

	// POST /lazybaobao/members/update_password
	@PostMapping("/update_password")
	public BasicRes updatePassword(@Valid @RequestBody UpdatePasswordReq req) {
		return membersService.updatePassword(req);
	}
}