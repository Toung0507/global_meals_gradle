package com.example.global_meals_gradle.controller;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true") // 允許 Angular 跨域呼叫
public class MembersController {
	
	@Autowired
	private MembersService membersService;
	
	private final String ATTRIBUTE_KEY = "check_result";
	
	// 訪客註冊
	@PostMapping("members/register_guest")
	public BasicRes registerGuest(@Valid @RequestBody RegisterMembersReq req) {
		return membersService.register(req, false);
	}
	
	// 會員註冊
	@PostMapping("members/register_member")
	public BasicRes registerMember(@Valid @RequestBody RegisterMembersReq req) {
		return membersService.register(req, true);
	}
	
	// 會員登入
	@PostMapping("members/login")
	public MembersRes login(@Valid @RequestBody LoginMembersReq req, HttpSession session) {
		/* 修改 session_id 的存活時間： 預設時間是 30 分鐘，
		 * 在有效的活時間內持續有跟 server 溝通，相同的 session_id 就不會失效 */
		// withNano(0) 是把毫秒的部分 取消掉
		session.setMaxInactiveInterval(7200); // 兩小時
		System.out.println(LocalTime.now().withNano(0) + " -- " //
				+ req.getPhone() + "：" + session.getId());
		MembersRes res = membersService.login(req);
		if(res.getCode() == ReplyMessage.SUCCESS.getCode()) {
			session.setAttribute(ATTRIBUTE_KEY, res);
		}
		return res;
	}
	
	// 會員登出
	@GetMapping("members/logout")
	public MembersRes logout(HttpSession session) {
		/* 直接讓 session 失效，當下一次的溝通的時候 即為新的 session */
		session.invalidate();
		return new MembersRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 會員修改密碼
	@PostMapping("members/update_password")
	public BasicRes updatePassword(@Valid @RequestBody UpdatePasswordReq req) {
		return membersService.updatePassword(req);
	}
	
}
