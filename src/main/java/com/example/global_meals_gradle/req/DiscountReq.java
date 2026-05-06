package com.example.global_meals_gradle.req; // 宣告此類別所屬的套件路徑

/**
 * discount 表管理用的請求參數，涵蓋以下用途：
 *   - 新增 discount 記錄（regionsId、count、usageCap）
 *   - 修改 usage_cap（id、usageCap）
 *   - 修改 count（id、count）
 *
 * 所有欄位驗證統一在 Service 層手動做
 */
public class DiscountReq {

	// =============================================
	// 修改 / 刪除時使用的主鍵
	// =============================================
	private int id; // discount 表的主鍵（update 端點必填，create 端點不需傳）

	// =============================================
	// 新增時使用的欄位
	// =============================================
	private int regionsId; // 關聯的國家區域 ID（對應 regions.id）；create 端點必填

	// =============================================
	// 新增 / 修改共用欄位
	// =============================================
	private int count; // 消費累積次數；create 端點選填（預設 0），updateCount 端點必填

	private int usageCap; // 折抵上限金額；create 端點必填（> 0），updateUsageCap 端點必填

	public int getId() { // 取得主鍵 id
		return id;
	}

	public void setId(int id) { // 設定主鍵 id
		this.id = id;
	}

	public int getRegionsId() { // 取得關聯的國家區域 ID
		return regionsId;
	}

	public void setRegionsId(int regionsId) { // 設定關聯的國家區域 ID
		this.regionsId = regionsId;
	}

	public int getCount() { // 取得消費累積次數
		return count;
	}

	public void setCount(int count) { // 設定消費累積次數
		this.count = count;
	}

	public int getUsageCap() { // 取得折抵上限金額
		return usageCap;
	}

	public void setUsageCap(int usageCap) { // 設定折抵上限金額
		this.usageCap = usageCap;
	}

}
