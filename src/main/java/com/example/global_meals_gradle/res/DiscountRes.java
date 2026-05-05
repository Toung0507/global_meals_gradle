package com.example.global_meals_gradle.res; // 宣告此類別所屬的套件路徑

import java.util.List; // 承載多筆 Discount 資料

import com.example.global_meals_gradle.entity.Discount; // discount 表對應的 JPA Entity

public class DiscountRes extends BasicRes { // 繼承 BasicRes（code + message），再擴充 discountList 欄位

	private List<Discount> discountList; // 查詢清單時回傳的折抵記錄清單

	public DiscountRes() { // 無參數建構子（Spring / JSON 反序列化需要）
		super();
	}

	public DiscountRes(int code, String message) { // 只回傳 code + message（無資料時使用）
		super(code, message);
	}

	public DiscountRes(int code, String message, List<Discount> discountList) { // 帶清單的完整建構子
		super(code, message); // 設定 code 與 message
		this.discountList = discountList; // 設定折抵記錄清單
	}

	public List<Discount> getDiscountList() { // 取得折抵記錄清單
		return discountList;
	}

	public void setDiscountList(List<Discount> discountList) { // 設定折抵記錄清單
		this.discountList = discountList;
	}

}
