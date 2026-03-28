package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.req.RefundedReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.service.OrdersService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class OrdersController {

	@Autowired
	private OrdersService ordersService;
	
	/* 取的該會員的歷史訂單 */
	@PostMapping("GetAllOrdersList")
	public GetAllOrdersRes GetAllOrdersList(@RequestBody HistoricalOrdersReq req) {
		return ordersService.getAllOrders(req);
	}
	
	/* 更改訂單狀態: 退款 REFUNDED 或取消 CANCELLED */
	@PostMapping("ordersStatus")
	public BasicRes ordersStatus(@RequestBody RefundedReq req) {
		return ordersService.ordersStatus(req);
	}
}
