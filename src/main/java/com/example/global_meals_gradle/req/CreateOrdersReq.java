package com.example.global_meals_gradle.req;

import java.math.BigDecimal;
import java.util.List;

import com.example.global_meals_gradle.constants.ValidationMsg;
import com.example.global_meals_gradle.entity.OrderCartDetails;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonProperty;


/* 成立訂單(還沒付款) */
public class CreateOrdersReq {

	@NotBlank(message = ValidationMsg.ORDER_CART_ID_ERROR)
	private String orderCartId;

	private int globalAreaId;

	private int memberId;

	@NotBlank(message = ValidationMsg.PHONE_ERROR)
	private String phone;

	private BigDecimal subtotalBeforeTax;

	private BigDecimal taxAmount;

	private BigDecimal totalAmount;
	
	// ╔══════════════════════════════════════════════════════════════════╗
	// ║  【Bug 修正】欄位名稱開頭大寫，導致 Jackson 無法對應前端傳來的 JSON ║
	// ╚══════════════════════════════════════════════════════════════════╝
	// ❌ 原本錯誤寫法：
	//    private List<OrderCartDetails> OrderCartDetailsList;
	//    （沒有 @JsonProperty 這行）
	//
	// 🔴 錯誤現象：前端傳了 orderCartDetailsList，後端收到的卻是 null，
	//             @NotEmpty 驗證失敗，回傳 400 Bad Request。
	//
	// 📖 為什麼會出錯？
	//    Jackson（Spring Boot 內建的 JSON 轉換工具）在把 JSON 轉成 Java 物件時，
	//    會去找「欄位名稱開頭改小寫後」對應的 JSON key。
	//    但這個欄位名叫 OrderCartDetailsList（大寫 O 開頭），
	//    Jackson 推斷 getter 名稱為 getOrderCartDetailsList → JSON key 為 orderCartDetailsList，
	//    理論上應該沒問題。
	//    然而有些版本的 Jackson 在處理「欄位本身名稱與 getter 不一致」的情況時，
	//    會優先採用欄位名稱（大寫 O 的 OrderCartDetailsList）當 JSON key，
	//    造成前端傳 orderCartDetailsList（小寫）時對應不到，該欄位永遠是 null。
	//
	// ✅ 解決方式：加上 @JsonProperty("orderCartDetailsList")
	//    這個 annotation 的意思是：「強制告訴 Jackson，這個欄位對應 JSON 裡的 key 叫做 orderCartDetailsList」
	//    不論欄位名稱怎麼拼，Jackson 都會乖乖用這個名稱做對應，不再猜測。
	//
	// 💡 根本解法：Java 命名規範規定欄位名稱應該用小寫開頭（lowerCamelCase），
	//    例如應命名為 orderCartDetailsList 而非 OrderCartDetailsList。
	//    遵守命名規範就不需要額外加 @JsonProperty 來補救。
	@NotEmpty(message = ValidationMsg.ORDER_CART_DETAILS_NOT_EMPTY)
	@Valid // 這樣才會去檢查 OrderCartDetails 類別裡面的標註
	@JsonProperty("orderCartDetailsList")
	private List<OrderCartDetails> OrderCartDetailsList;
	
	
	
	public String getOrderCartId() {
		return orderCartId;
	}

	public void setOrderCartId(String orderCartId) {
		this.orderCartId = orderCartId;
	}

	public int getGlobalAreaId() {
		return globalAreaId;
	}

	public void setGlobalAreaId(int globalAreaId) {
		this.globalAreaId = globalAreaId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public BigDecimal getSubtotalBeforeTax() {
		return subtotalBeforeTax;
	}

	public void setSubtotalBeforeTax(BigDecimal subtotalBeforeTax) {
		this.subtotalBeforeTax = subtotalBeforeTax;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<OrderCartDetails> getOrderCartDetailsList() {
		return OrderCartDetailsList;
	}

	public void setOrderCartDetailsList(List<OrderCartDetails> orderCartDetailsList) {
		OrderCartDetailsList = orderCartDetailsList;
	}

}
