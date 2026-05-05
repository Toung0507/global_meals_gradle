package com.example.global_meals_gradle.entity; // 宣告此類別所屬的套件路徑

import jakarta.persistence.*; // 引入所有 JPA 相關注解（@Entity、@Table、@Column 等）

@Entity // 標記為 JPA 實體類別，對應資料庫的一張表
@Table(name = "discount") // 對應資料庫中名為 discount 的表
public class Discount {

	@Id // 標記為主鍵
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 對應 DB 的 AUTO_INCREMENT，id 由資料庫自動產生
	@Column(name = "id") // 對應 discount 表的 id 欄位
	private int id;

	@Column(name = "regions_id") // 對應 discount 表的 regions_id 欄位，外鍵指向 regions.id
	private int regionsId; // 關聯的國家區域 ID

	@Column(name = "count") // 對應 discount 表的 count 欄位
	private int count; // 消費累積次數

	@Column(name = "usage_cap") // 對應 discount 表的 usage_cap 欄位
	private int usageCap; // 折抵上限金額（各國貨幣單位不同）

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
