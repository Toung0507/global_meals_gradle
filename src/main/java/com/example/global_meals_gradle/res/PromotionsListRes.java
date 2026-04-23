package com.example.global_meals_gradle.res;

import java.util.List;

import com.example.global_meals_gradle.vo.PromotionDetailVo;

/**
 * GET /promotions/list 的回傳物件
 *
 * 結構說明：
 *   code    → HTTP 狀態碼語意（200 = 成功）
 *   message → 描述訊息（"Success!!" 或錯誤原因）
 *   data    → 所有促銷活動的清單，每筆包含活動資訊與其贈品規則
 */
public class PromotionsListRes {

	// 回應碼：對應 ReplyMessage enum 的 code
	// 例如：200（SUCCESS）
	private int code;

	// 回應訊息：對應 ReplyMessage enum 的 message
	// 例如："Success!!"
	private String message;

	// 所有促銷活動清單（包含啟用與停用的），每筆附帶其贈品規則
	private List<PromotionDetailVo> data;

	// 提供 code 和 message 的建構子，方便在 Service 裡一行初始化
	public PromotionsListRes(int code, String message, List<PromotionDetailVo> data) {
		this.code = code;
		this.message = message;
		this.data = data;
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

	public List<PromotionDetailVo> getData() {
		return data;
	}

	public void setData(List<PromotionDetailVo> data) {
		this.data = data;
	}

}
