package com.example.global_meals_gradle.res;

import java.math.BigDecimal;
import java.util.List;

/** 整個購物車的最終結果 (回傳給前端專用) */
public class CartViewRes extends BasicRes {

	private int cartId;

//	private int globalAreaId;

	private String operationType;

	/** 裝填這台車所有的商品與贈品！ */
	private List<CartItemVO> items;

	/** 稅前應付總額（所有商品 LineTotal 的總和，必須排除贈品價格） */
	private BigDecimal subtotal;

	// 使用者「有資格參加」的活動清單（兩層結構：第一層是活動，第二層是各活動的贈品選項）
	// 空清單 [] → 消費金額達不到任何活動門檻 → 前端不顯示「選擇活動」按鈕
	// 有資料   → 前端顯示「選擇活動」按鈕，按下後展開活動下拉選單
//	回傳「以活動為單位」的兩層巢狀結構
	private List<AvailablePromotionVO> availablePromotions;

	// 稅務資訊（稅率、稅的類型、稅額）
	// 如果這台購物車的分店沒有稅務設定，這個欄位是 null
	private TaxInfoVO taxInfo;

	// 最終總計金額 = subtotal + taxAmount（EXCLUSIVE）或 = subtotal（INCLUSIVE）
	// 後端算好直接給前端顯示，前端不需要自己做加總
	private BigDecimal totalAmount;

	// 警告訊息清單（後端在重新驗算時發現的問題）
	// 例如：「「大份薯條」已下架或不存在，請將其移除」
	// 例如：「「牛肉麵」的價格已從 $150 調整為 $180」
	// 如果一切正常，這個清單是空的（不是 null，是空清單）
	private List<String> warningMessages;

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

//	public int getGlobalAreaId() {
//		return globalAreaId;
//	}

//	public void setGlobalAreaId(int globalAreaId) {
//		this.globalAreaId = globalAreaId;
//	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public List<CartItemVO> getItems() {
		return items;
	}

	public void setItems(List<CartItemVO> items) {
		this.items = items;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	
	public List<AvailablePromotionVO> getAvailablePromotions() {
		return availablePromotions;
	}

	public void setAvailablePromotions(List<AvailablePromotionVO> availablePromotions) {
		this.availablePromotions = availablePromotions;
	}

	public TaxInfoVO getTaxInfo() {
		return taxInfo;
	}

	public void setTaxInfo(TaxInfoVO taxInfo) {
		this.taxInfo = taxInfo;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<String> getWarningMessages() {
		return warningMessages;
	}

	public void setWarningMessages(List<String> warningMessages) {
		this.warningMessages = warningMessages;
	}

	/**
	 * 套用折價券後的實際應付金額 - null = 尚未使用折價券（前端顯示 subtotal 即可）<br>
	 * - 有值 = 已套用折價券（前端顯示這個打折後的金額）
	 */
//	private BigDecimal discountedTotal;

	/**
	 * 告訴前端：這個會員是否有折價券可用？ true → 前端顯示「是否使用折價券？」的選擇按鈕
	 */
//	private boolean hasCoupon;

}
