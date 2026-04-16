package com.example.global_meals_gradle.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 促銷活動結帳請求參數
 * 由 CartService 呼叫時傳入，包含購物車資訊、會員資訊、折扣選擇、贈品選擇
 * 新增 POST /promotions/calculate 端點後，originalAmount 改由前端直接傳入
 */
public class PromotionsReq {

	// 購物車 ID：用來讓回傳的 Res 對應是哪一張購物車
	// Service 本身不用這個 ID 去查任何東西，只是原封不動帶回去
	// 最小值為 1，0 或負數視為無效
	@Min(value = 1, message = "Cart ID must be at least 1")
	private int cartId;

	// 會員 ID：
	//   = 1 表示訪客，訪客沒有折扣資格，Service 會直接跳過折扣判斷
	//   > 1 表示一般會員，才會去查 members 表判斷是否有折扣券
	// 最小值為 1，0 或負數視為無效
	@Min(value = 1, message = "Member ID must be at least 1")
	private int memberId;

	// 前端勾選「是否使用 9 折券」：
	//   true  = 使用者有勾選，但還要在 Service 確認 members.is_discount = 1 才會實際打折
	//           若 is_discount = 0 卻傳 true，Service 會擋下並回傳 MEMBER_COUPON_NOT_AVAILABLE
	//           折扣金額有上限：台灣最多折 200、日本最多折 1000、韓國最多折 10000
	//   false = 使用者沒勾選，即使有券也不打折
	//   boolean 只有 true/false，不需要 annotation 驗證
	private boolean useCoupon;

	// 使用者從可選贈品清單中選擇的贈品（promotions_gifts.id）：
	//   > 0 → 使用者有選，Service 會驗證這筆贈品是否合法後才扣減配額
	//   = 0 → 使用者放棄選贈品，Service 跳過贈品處理
	private int selectedGiftId;

	// 購物車原始總金額：由前端計算好後傳入
	// POST /promotions/calculate 端點使用，取代原本由 CartService 內部傳入的方式
	// @NotNull 確保前端一定要傳這個欄位，不能為空
	@NotNull(message = "Original amount is required")
	private BigDecimal originalAmount;

	// 結帳分店所在國家（由前端帶入，例如 "台灣"、"日本"、"韓國"）
	// 用來決定會員 9 折優惠的折扣上限：台灣 200、日本 1000、韓國 10000
	// 其他國家不設上限
	private String country;

	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public boolean isUseCoupon() {
		return useCoupon;
	}

	public void setUseCoupon(boolean useCoupon) {
		this.useCoupon = useCoupon;
	}

	public int getSelectedGiftId() {
		return selectedGiftId;
	}

	public void setSelectedGiftId(int selectedGiftId) {
		this.selectedGiftId = selectedGiftId;
	}

	public BigDecimal getOriginalAmount() {
		return originalAmount;
	}

	public void setOriginalAmount(BigDecimal originalAmount) {
		this.originalAmount = originalAmount;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

}
