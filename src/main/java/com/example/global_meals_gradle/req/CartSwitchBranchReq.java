package com.example.global_meals_gradle.req;

import com.example.global_meals_gradle.constants.ValidationMsg;

import jakarta.validation.constraints.Min;

public class CartSwitchBranchReq {
	 @Min(value = 1, message = ValidationMsg.CART_ID_MUST_BE_POSITIVE)
	 private int oldCartId;       // 舊購物車ID（要被清空的那台）
	 @Min(value = 1, message = ValidationMsg.GLOBAL_AREA_ID_MUST_BE_POSITIVE)
	    private int newGlobalAreaId; // 新分店ID（要切換去的分店）
	 @Min(value = 1, message = ValidationMsg.MEMBER_ID_MUST_BE_POSITIVE)
	    private int memberId;        // 目前登入的會員ID
		public int getOldCartId() {
			return oldCartId;
		}
		public void setOldCartId(int oldCartId) {
			this.oldCartId = oldCartId; 	
		}
		public int getNewGlobalAreaId() {
			return newGlobalAreaId;
		}
		public void setNewGlobalAreaId(int newGlobalAreaId) {
			this.newGlobalAreaId = newGlobalAreaId;
		}
		public int getMemberId() {
			return memberId;
		}
		public void setMemberId(int memberId) {
			this.memberId = memberId;
		}
	    
}