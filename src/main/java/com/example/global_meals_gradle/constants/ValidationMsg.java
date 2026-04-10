package com.example.global_meals_gradle.constants;

public class ValidationMsg {

	/* 定義成 static (全域)，可以直接透過 ValidationMsg.TITLE_ERROR 來呼叫使用 */
	
	// 昱文
	// Entity Products
	public static final String QUANTITY_CANT_BE_NEGATIVE = "Quantity Cant Be Negative!!";
	public static final String MAX_ORDER_QUANTITY = "Max Order Quantity One At Least!!";
	// Req CreateRegions
	public static final String COUNTRY_ERROR = "Country Error!!";
	public static final String CURRENCY_CODE_ERROR = "Currency Code Error!!";
	public static final String TAX_TYPE_ERROR = "Tax Type Error!!";
	public static final String TAX_RATE_ERROR = "Tax Rate Error!!";
	// Req UpdateRegionsReq
	public static final String REGIONS_ID_ERROR = "Regions Id Error!!";
	// Req CreateGlobalAreaReq
	public static final String BRANCH_ERROR = "Branch Error!!";
	public static final String ADDRESS_ERROR = "Address Error!!";
	// Req UpdateGlobalAreaReq
	public static final String GLOBAL_AREA_ID_ERROR = "Global Area Id Error!!";
	// Req DeleteGlobalAreaReq
	public static final String GLOBAL_AREA_ID_LIST_ERROR = "Global Area Id List Error!!";
	// Req ExchangeRatesReq
	public static final String DATE_TIME_ERROR = "Date Time Error!!";
	
	// 劭穎
	/* 訂單 */
	public static final String ORDER_DATE_ID_ERROR = "Order Date Id Error";

	public static final String ID_ERROR = "Id Error"; // 訂單id錯誤

	public static final String STATUS_ERROR = "Status Error"; // 訂單狀態錯誤

	public static final String PAYMENT_METHID_ERROR = "PaymentMethod Error";

	public static final String TRANSACTION_ID_ERROR = "TransactionId Error";

	public static final String ORDER_CART_ID_ERROR = "OrderCartId Error";

	public static final String PHONE_ERROR = "Phone Error";

	public static final String ORDER_CART_DETAILS_NOT_EMPTY = "Order Cart Details Not Empty";
	/* 購物車（艷羽） */
	public static final String CART_ID_MUST_BE_POSITIVE = "CartId Must Be Positive";
	public static final String PRODUCT_ID_MUST_BE_POSITIVE = "ProductId Must Be Positive ";
	public static final String MEMBER_ID_MUST_BE_POSITIVE = "MemberId Must Be Positive";
	
	/* 景翔 */
	/* 職員 */
	public static final String NAME_CANNOT_BE_BLANK = "Name Cannot Be Blank";
	
	public static final String ACCOUNT_CANNOT_BE_BLANK = "Account Cannot Be Blank";
	
	public static final String PASSWORD_CANNOT_BE_BLANK = "Password Cannot Be Blank";

	public static final String ROLE_ERROR = "Role Error";

}
