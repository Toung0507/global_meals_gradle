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
	
	public static final String QUESTION_ERROR = "Question Error!!";
	public static final String QUESTION_LIST_IS_EMPTY = "Question List Is Empty!!";
	


}
