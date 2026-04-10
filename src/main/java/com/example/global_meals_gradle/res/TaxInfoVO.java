package com.example.global_meals_gradle.res;

import java.math.BigDecimal;

public class TaxInfoVO {

	  private BigDecimal taxRate;
//	   "EXCLUSIVE" = 外加稅（稅額在小計之外額外加上去，total =小計 subtotal + tax）
//	    "INCLUSIVE" = 內含稅（稅額已包在商品定價裡，total = 小計subtotal，不另加稅）
	  private String taxType;
//	  EXCLUSIVE：taxAmount = subtotal × taxRate
	 /* INCLUSIVE:
	  含稅價=原價+原價*稅率=原價（1+稅率）
	  所以原價=含稅價 ÷ (1 + 稅率)
	  稅金 (taxAmount) = 含稅價 (subtotal) - 原價
	  稅金 (taxAmount) = 含稅價 - [ 含稅價 ÷ (1 + 稅率) ]
	  即：taxAmount = subtotal × taxRate ÷ (1 + taxRate)（從含稅金額反推稅額）
	  */
	  private BigDecimal taxAmount;
	  public BigDecimal getTaxRate() {
		  return taxRate;
	  }
	  public void setTaxRate(BigDecimal taxRate) {
		  this.taxRate = taxRate;
	  }
	  public String getTaxType() {
		  return taxType;
	  }
	  public void setTaxType(String taxType) {
		  this.taxType = taxType;
	  }
	  public BigDecimal getTaxAmount() {
		  return taxAmount;
	  }
	  public void setTaxAmount(BigDecimal taxAmount) {
		  this.taxAmount = taxAmount;
	  }
	  
}
