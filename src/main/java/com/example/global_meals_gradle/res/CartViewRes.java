package com.example.global_meals_gradle.res;

import com.example.global_meals_gradle.vo.CartItemVO;
import java.math.BigDecimal;
import java.util.List;
//-----------可能還需要修改
/** 整個購物車的最終結果 (回傳給前端專用) */
public class CartViewRes extends BasicRes {

  
    private int cartId;
    
   
    private int globalAreaId;
    
  
    private String operationType;
    
    /** 裝填這台車所有的商品與贈品！ */
    private List<CartItemVO> items;
    
    /** 稅前應付總額（所有商品 LineTotal 的總和，必須排除贈品價格） */
    private BigDecimal subtotal;

   
    /** 
     * 套用折價券後的實際應付金額
     * - null = 尚未使用折價券（前端顯示 subtotal 即可）
     * - 有值 = 已套用折價券（前端顯示這個打折後的金額）
     */
    private BigDecimal discountedTotal;

    /** 
     * 告訴前端：這個會員是否有折價券可用？
     * true → 前端顯示「是否使用折價券？」的選擇按鈕
     */
    private boolean hasCoupon;

    
    
   
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    
    public int getGlobalAreaId() { return globalAreaId; }
    public void setGlobalAreaId(int globalAreaId) { this.globalAreaId = globalAreaId; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public List<CartItemVO> getItems() { return items; }
    public void setItems(List<CartItemVO> items) { this.items = items; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountedTotal() { return discountedTotal; }
    public void setDiscountedTotal(BigDecimal discountedTotal) { this.discountedTotal = discountedTotal; }

    public boolean isHasCoupon() { return hasCoupon; }
    public void setHasCoupon(boolean hasCoupon) { this.hasCoupon = hasCoupon; }
}
