package com.example.global_meals_gradle.req;

public class CartSwitchBranchReq {
	 private int oldCartId;       // 舊購物車ID（要被清空的那台）
	    private int newGlobalAreaId; // 新分店ID（要切換去的分店）
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