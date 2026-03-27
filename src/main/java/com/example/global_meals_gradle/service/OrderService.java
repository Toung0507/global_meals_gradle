package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.res.GetAllOrdersRes;

public class OrderService {
	
	@Autowired
	private OrdersDao ordersDao;

	/* 查詢歷史訂單 */
	public GetAllOrdersRes getAllOrders(HistoricalOrdersReq req) {
		/* 判斷會員id是否存在 */
		
		return new GetAllOrdersRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), ordersDao.getOrderByMemberId(req.getMemberId()));
	}
}
