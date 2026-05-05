package com.example.global_meals_gradle.controller;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/members")
@Tag(name = "會員管理模組", description = "處理會員註冊、登入、登出及密碼變更業務")
public class MembersController {
	
	@Autowired
	private MembersService membersService;
	
	public static final String ATTRIBUTE_KEY = "check_result";
	
	// 訪客註冊
	@PostMapping("register_guest")
	@Operation(summary = "訪客註冊", description = "建立訪客帳號")
	public BasicRes registerGuest(@Valid @RequestBody RegisterMembersReq req) {
		return membersService.register(req, false);
	}
	
	// 會員註冊
	@PostMapping("register_member")
	@Operation(summary = "會員註冊", description = "建立正式會員帳號")
	public BasicRes registerMember(@Valid @RequestBody RegisterMembersReq req) {
		return membersService.register(req, true);
	}
	
	// 會員登入
	@PostMapping("login")
	@Operation(summary = "會員登入", description = "會員登入並建立 Session")
	public MembersRes login(@Valid @RequestBody LoginMembersReq req,  //
			@Parameter(hidden = true) HttpSession session) {
		/* 修改 session_id 的存活時間： 預設時間是 30 分鐘，
		 * 在有效的活時間內持續有跟 server 溝通，相同的 session_id 就不會失效 */
		// withNano(0) 是把毫秒的部分 取消掉
		session.setMaxInactiveInterval(7200); // 兩小時
		System.out.println(LocalTime.now().withNano(0) + " -- " //
				+ req.getPhone() + "：" + session.getId()); //
		MembersRes res = membersService.login(req); 
		if(res.getCode() == ReplyMessage.SUCCESS.getCode()) { //
			session.setAttribute(ATTRIBUTE_KEY, res); //
		}
		return res;
	}
	
	@GetMapping("/logout")
	@Operation(summary = "會員登出", description = "銷毀當前 Session")
	public MembersRes logout(@Parameter(hidden = true) HttpSession session) {
		/* 直接讓 session 失效，當下一次的溝通的時候 即為新的 session */
		session.invalidate();
		return new MembersRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 會員修改密碼
	@PostMapping("/update-password")
	@Operation(summary = "修改密碼", description = "更新會員登入密碼")
	public BasicRes updatePassword(@Valid @RequestBody UpdatePasswordReq req) {
		return membersService.updatePassword(req);
	}
	
}
