package com.example.global_meals_gradle.req;

// 這個 Req 是「停權/復權」專用的請求盒子！
// 就像你要開一扇門，這個盒子裝著：「你要對哪個人出手」跟「停還是開」兩個資訊

//停權 / 復權的請求體
//注意：targetId 不放在這裡，而是放在 URL 的 {id} 路徑變數
//原因：PATCH /api/admin/staff/{id}/status 是 RESTful 風格
//    {id} 代表「你要操作的是哪一個資源」
//    body 只放「你要怎麼改它」，所以只需要 newStatus 就夠了
//
//為什麼不繼承 RegisterStaffReq？
//    RegisterStaffReq 裡有 name、account、password、role、globalAreaId，
//    停權功能完全不需要這些欄位。繼承過來只會讓前端
//    被迫填一堆無關的資料，反而造成困擾，所以獨立一個乾淨的 Req
public class UpdateStaffStatusReq {

	// 被操作的員工 ID（就是資料庫裡那個倒楣鬼的 ID）
//	private int targetId;

	// true = 復權（歡迎回來！），false = 停權（掰掰了您嘞）
	private boolean newStatus;
//
//	public int getTargetId() {
//		return targetId;
//	}
//
//	public void setTargetId(int targetId) {
//		this.targetId = targetId;
//	}

	// boolean 的 getter 習慣用 is 開頭，不是 get！
	public boolean isNewStatus() {
		return newStatus;
	}

	public void setNewStatus(boolean newStatus) {
		this.newStatus = newStatus;
	}
}