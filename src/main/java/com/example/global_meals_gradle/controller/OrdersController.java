package com.example.global_meals_gradle.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.req.CreateOrdersReq;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.req.KitchenStatusReq;
import com.example.global_meals_gradle.req.PayReq;
import com.example.global_meals_gradle.req.RefundedReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.CreateOrdersRes;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.res.GetOrdersByPhoneRes;
import com.example.global_meals_gradle.res.GetTodayOrdersRes;
import com.example.global_meals_gradle.service.EcpayService;
import com.example.global_meals_gradle.service.LinePayService;
import com.example.global_meals_gradle.service.OrdersService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders") // WebConfig 自動補 /lazybaobao，最終為 /lazybaobao/orders
public class OrdersController {

	@Autowired
	private OrdersService ordersService;

	@Autowired
	private EcpayService ecpayService;

	@Autowired
	private LinePayService linePayService;

	// POST /lazybaobao/orders/get_all_orders
	@PostMapping("/get_all_orders")
	public GetAllOrdersRes GetAllOrdersList(@RequestBody HistoricalOrdersReq req, HttpSession httpSession) {
		return ordersService.getAllOrders(req, httpSession);
	}

	// POST /lazybaobao/orders/update_status
	@PostMapping("/update_status")
	public BasicRes ordersStatus(@Valid @RequestBody RefundedReq req, HttpSession httpSession) {
		return ordersService.ordersStatus(req, httpSession);
	}

	// POST /lazybaobao/orders/create_order
	@PostMapping("/create_order")
	public CreateOrdersRes createOrdersRes(@Valid @RequestBody CreateOrdersReq req, HttpSession httpSession) {
		return ordersService.createOrders(req, httpSession);
	}

	// POST /lazybaobao/orders/pay
	@PostMapping("/pay")
	public BasicRes pay(@Valid @RequestBody PayReq req) {
		return ordersService.pay(req);
	}

	// GET /lazybaobao/orders/get_order_by_phone?phone=xxx
	@GetMapping("/get_order_by_phone")
	public GetOrdersByPhoneRes getOrderByPhone(@RequestParam("phone") String phone) {
		return ordersService.getOrderByPhone(phone);
	}

	// GET /lazybaobao/orders/today_orders
	@GetMapping("/today_orders")
	public GetTodayOrdersRes getTodayOrders() {
		return ordersService.getTodayOrders();
	}

	// POST /lazybaobao/orders/kitchen_status
	@PostMapping("/kitchen_status")
	public BasicRes updateKitchenStatus(@Valid @RequestBody KitchenStatusReq req) {
		return ordersService.updateKitchenStatus(req);
	}

	// GET /lazybaobao/orders/order_status?id=&orderDateId=
	@GetMapping("/order_status")
	public BasicRes getOrderStatus(@RequestParam("id") String id,
			@RequestParam("orderDateId") String orderDateId) {
		return ordersService.getOrderStatus(id, orderDateId);
	}

	// GET /lazybaobao/orders/go_pay?orderDateId=&id=&way=
	// ⚠️ 從 /goPay 改名為 /go_pay，統一命名風格
	@GetMapping("/go_pay")
	public String goPay(@RequestParam("orderDateId") String orderDateId,
			@RequestParam("id") String id,
			@RequestParam("way") String way) {
		if ("ECPAY".equalsIgnoreCase(way)) {
			return ecpayService.getEcpayForm(orderDateId, id);
		} else if ("LINEPAY".equalsIgnoreCase(way)) {
			String payUrl = linePayService.getLinePayLink(orderDateId, id);
			return "redirect:" + payUrl;
		}
		return "Unsupported payment method";
	}

	// POST /lazybaobao/orders/payment_callback
	// ⚠️ 綠界金流 callback，從 /api/payment/callback 改為 /payment_callback
	@PostMapping("/payment_callback")
	public String handlePaymentNotify(@RequestParam Map<String, String> params) {
		String rtnCode = params.get("RtnCode");
		String merchantTradeNo = params.get("MerchantTradeNo");

		if ("1".equals(rtnCode)) {
			String orderDateId = merchantTradeNo.substring(0, 8);
			String id = merchantTradeNo.substring(8).split("T")[0];

			String ecpayPaymentType = params.get("PaymentType");
			String myPaymentMethod = "UNKNOWN";
			if (ecpayPaymentType.contains("Credit")) {
				myPaymentMethod = "CREDIT_CARD";
			} else if (ecpayPaymentType.contains("LINEPAY")) {
				myPaymentMethod = "LINE_PAY";
			} else {
				myPaymentMethod = "ONLINE_OTHER";
			}

			String tradeAmt = params.get("TradeAmt");
			PayReq req = new PayReq();
			req.setOrderDateId(orderDateId);
			req.setId(id);
			req.setPaymentMethod(myPaymentMethod);
			req.setTransactionId(params.get("TradeNo"));
			if (tradeAmt != null) {
				req.setTotalAmount(new BigDecimal(tradeAmt));
			}

			BasicRes res = ordersService.pay(req);
			if (res.getCode() == 200) {
				return "1|OK";
			}
		}
		return "0|Fail";
	}

	// GET /lazybaobao/orders/linepay_confirm?transactionId=&orderDateId=&id=&amount=
	// ⚠️ 從 /linepay/confirm 改為 /linepay_confirm
	@GetMapping("/linepay_confirm")
	public String linePayConfirm(@RequestParam("transactionId") String transactionId,
			@RequestParam("orderDateId") String orderDateId,
			@RequestParam("id") String id,
			@RequestParam("amount") int amount) {
		try {
			linePayService.confirmPayment(transactionId, amount, orderDateId, id);
			return "redirect:https://your-frontend.com/payment-success";
		} catch (Exception e) {
			return "redirect:https://your-frontend.com/payment-fail";
		}
	}
}