package com.example.global_meals_gradle.res;

/**
 * 單一贈品項目
 * 放在 PromotionsRes.receivedGifts 和查詢可選贈品清單的回傳裡
 * 每一筆代表一個可選或已選的贈品
 */
public class GiftItem {

	// promotions_gifts 表的 id
	// 前端顯示可選清單後，使用者選好要帶這個 id 回來告訴 Service 選了哪一筆
	// 若使用者放棄選贈品則傳 0
	private int promotionsGiftsId;

	// 贈品的商品 ID，對應 products.id
	// 也就是 promotions_gifts.gift_product_id 的值
	private int productId;

	// 贈品的商品名稱，從 products 表用 gift_product_id 查出來
	// 若查不到（商品被刪除等異常情況）則顯示 "活動贈品"
	private String productName;

	// 贈品數量：
	//   promotions_gifts.quantity = -1 → 無限供應，設為 -1 讓前端判斷（例如不顯示數量）
	//   promotions_gifts.quantity > 0  → 有限供應，直接帶入該數字
	private int quantity;

	public int getPromotionsGiftsId() {
		return promotionsGiftsId;
	}

	public void setPromotionsGiftsId(int promotionsGiftsId) {
		this.promotionsGiftsId = promotionsGiftsId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
