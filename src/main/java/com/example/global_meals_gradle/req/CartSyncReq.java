package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.Min;

/** 同步購物車商品（加入商品 / 更改數量（包括刪除單一商品） */
public class CartSyncReq {

   
	//如果前端沒有傳這個值給我，值是null,表示新建購物車
	//如果某天你們的商品 ID 剛好允許 0，這個驗證就報廢了。所以業界統一規定：只要是可能沒傳的欄位，一律用大寫 Integer 配 @NotNull
	//如果用int,用0撈資料庫回傳Null，那後續很容易出現空指針錯誤
	
    private Integer cartId;
    
    
    /*已有購物車（cartId 有值，舊車）：
     *  後端只需要去 order_cart_details 裡加一條明細，主表 order_cart 不需要動，所以 globalAreaId 傳什麼都不影響，傳 null 後端一樣會忽略它。
     *  這個欄位只在建新車的那次有用。
     */
    private Integer globalAreaId;

    // 商品ID必填（必須是正整數）；用 int 讓 Spring 無法偷塞 null，@Min(1) 擋掉 0 和負數
    @Min(value = 1, message = "商品 ID 必須大於 0")
    private int productId;

   
    @Min(value = 0, message = "數量不能為負數")
    private int quantity;

   
    private String operationType;
    
   // 只要這個欄位在某些情境下會是空的（沒有值），我們就必須用大寫的 Integer，如果用 int ，是用0去撈資料
    private Integer staffId;
   //訪客
    private Integer memberId ;


    
    
    
    
	public Integer getCartId() {
		return cartId;
	}
	public void setCartId(Integer cartId) {
		this.cartId = cartId;
	}
	public Integer getGlobalAreaId() {
		return globalAreaId;
	}
	public void setGlobalAreaId(Integer globalAreaId) {
		this.globalAreaId = globalAreaId;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	public Integer getStaffId() {
		return staffId;
	}
	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}
	public Integer getMemberId() {
		return memberId;
	}
	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}
    
    

}
