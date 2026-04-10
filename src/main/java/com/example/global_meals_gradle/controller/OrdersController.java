package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CreateOrdersReq;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.req.RefundedReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.CreateOrdersRes;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.service.OrdersService;

import jakarta.validation.Valid;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class OrdersController {

	@Autowired
	private OrdersService ordersService;

	/* 取的該會員的歷史訂單 */
	@PostMapping("orders/get_all_orders_list")
	public GetAllOrdersRes GetAllOrdersList(@RequestBody HistoricalOrdersReq req) {
		return ordersService.getAllOrders(req);
	}

	/* 更改訂單狀態: 退款 REFUNDED 或取消 CANCELLED */
	@PostMapping("orders/orders_status")
	public BasicRes ordersStatus(@Valid @RequestBody RefundedReq req) {
		return ordersService.ordersStatus(req);
	}

	/* 成立訂單(未結帳) */
	@PostMapping("orders/create_orders")
	public CreateOrdersRes createOrdersRes(@RequestBody CreateOrdersReq req) {
		return ordersService.createOrders(req);
	}

	/* 結帳完成 */
	@PostMapping("orders/pay")
	public BasicRes pay(@Valid @RequestBody PayReq req) {
		return ordersService.pay(req);
	}

	/* 報電話號碼取餐 */
	@GetMapping("orders/get_order_by_phone")
	public CreateOrdersRes getOrderByPhone(@RequestParam("phone") String phone) {
		return ordersService.getOrderByPhone(phone);
	}
}
