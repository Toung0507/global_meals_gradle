package com.example.global_meals_gradle.res;

import com.example.global_meals_gradle.entity.Members;

public class MembersRes extends BasicRes {
	
	private Members members;

	public MembersRes() {
		super();
	}

	public MembersRes(int code, String message) {
		super(code, message);
	}

	public MembersRes(int code, String message, Members members) {
		super(code, message);
		this.members = members;
	}

	public Members getMembers() {
		return members;
	}

	public void setMembers(Members members) {
		this.members = members;
	}
	
}
