package com.example.global_meals_gradle.constants;

public class ValidationMsg {

	/* 定義成 static (全域)，可以直接透過 ValidationMsg.TITLE_ERROR 來呼叫使用 */
	public static final String QUANTITY_CANT_BE_NEGATIVE = "Quantity Cant Be Negative!!";

	public static final String MAX_ORDER_QUANTITY = "Max Order Quantity One At Least!!";

	public static final String START_DATE_ERROR = "Start Date Error!!";

	public static final String END_DATE_ERROR = "End Date Error!!";

	public static final String QUESTION_LIST_IS_EMPTY = "Question List Is Empty!!";

	public static final String QUESTION_ID_ERROR = "Question Id Error!!";

	public static final String QUESTION_ERROR = "Question Error!!";

	/* 訂單 */
	public static final String ORDER_DATE_ID_ERROR = "Order Date Id Error";
	
	public static final String ID_ERROR = "Id Error";  // 訂單id錯誤
	
	public static final String STATUS_ERROR = "Status Error";  // 訂單狀態錯誤
	
	public static final String PAYMENT_METHID_ERROR = "PaymentMethod Error";
	
	public static final String TRANSACTION_ID_ERROR = "TransactionId Error";

	public static final String ORDER_CART_ID_ERROR = "OrderCartId Error";
	
	public static final String PHONE_ERROR = "Phone Error";
	
	public static final String ORDER_CART_DETAILS_NOT_EMPTY = "Order Cart Details Not Empty";

}
