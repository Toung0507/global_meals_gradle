package com.example.global_meals_gradle.constants;

public enum ReplyMessage {
	
	// 昱文
	// Service RegionsService
	REGIONS_ID_ERROR(400, "Regions Id Error!!"),//
	
	SUCCESS(200, "Success!!"),//
	TITLE_ERROR(400, "Title Error!!"),//
	DESCRIPTION_ERROR(400, "Description Error!!"),//
	START_DATE_ERROR(400, "Start Date Error!!"),//
	END_DATE_ERROR(400, "End Date Error!!"),//
	QUESTION_ID_ERROR(400, "Question Id Error!!"),//
	QUIZ_ID_ERROR(400, "Quiz Id Error!!"), //
	QUESTION_ERROR(400, "Question Error!!"),//
	TYPE_ERROR(400, "Type Error!!"),//
	OPTIONS_ERROR(400, "Options Error!!"), //
	QUIZ_ID_MISMATCH(400, "Quiz Id Mismatch!!"), //
	QUIZ_UPDATE_FORBIDDEN(400, "Quiz Update Forbidden!!"), //
	QUIZ_NOT_FOUND(404, "Quiz Not Found"), //
	// User
	USER_EMAIL_ERROR(400, "User Email Error!!"), //
	USER_NAME_ERROR(400, "User Name Error!!"), //
	USER_AGE_ERROR(400, "User Age Error!!"), //
	USER_PASSWORD_ERROR(400, "User Password Error!!"), //
	USER_EMAIL_EXISTED(400, "User Email Existed!!"), //
	USER_NOT_FOUND(404, "User Not Found!!"), //
	ANSWER_REQUIRED(400, "Answer Required!!"), //
	EMAIL_NOT_FOUND(404, "Email Not Found!!");
	
	private int code;
	
	private String message;

	private ReplyMessage(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
