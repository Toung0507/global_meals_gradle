package com.example.global_meals_gradle.entity;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Member extends OncePerRequestFilter{

	@Override
	protected void doFilterInternal(HttpServletRequest request, 
	                                HttpServletResponse response, 
	                                FilterChain filterChain) 
	                                throws ServletException, IOException {
	    
	    // 1. 這裡寫你的邏輯（例如：檢查 JWT Token、驗證權限）
	    
	    // 2. 務必加上這行，讓請求繼續往下走，否則請求會卡在這裡死掉
	    filterChain.doFilter(request, response);
	}
}
