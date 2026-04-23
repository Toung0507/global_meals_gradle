package com.example.global_meals_gradle.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.req.PaymentInitReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.LinePayRes;
import com.example.global_meals_gradle.service.EcpayService;
import com.example.global_meals_gradle.service.LinePayService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("lazybaobao/payment")
public class PaymentController {

	@Autowired
	private LinePayService linePayService;

	@Autowired
	private EcpayService ecpayService;

	@Value("${payment.ngrok.url}")
	private String ngrokUrl;

	// ── LINE Pay ──────────────────────────────────────────────

	/**
	 * 前端呼叫取得 LINE Pay 付款連結
	 * POST lazybaobao/payment/linepay/request
	 */
	@PostMapping("/linepay/request")
	public LinePayRes linePayRequest(@Valid @RequestBody PaymentInitReq req) {
		try {
			String url = linePayService.requestPayment(req.getOrderDateId(), req.getId());
			return new LinePayRes(200, "success", url);
		} catch (Exception e) {
			return new LinePayRes(500, e.getMessage(), null);
		}
	}

	/**
	 * LINE Pay 付款後重導向此端點（由 Angular proxy 轉發）
	 * GET lazybaobao/payment/linepay/confirm?transactionId=X&orderId=Y
	 */
	@GetMapping("/linepay/confirm")
	public void linePayConfirm(
			@RequestParam String transactionId,
			@RequestParam String orderId,
			HttpServletResponse response) throws IOException {
		try {
			linePayService.confirmPayment(transactionId, orderId);
			response.sendRedirect(ngrokUrl + "/payment/result?status=success&method=linepay");
		} catch (Exception e) {
			response.sendRedirect(ngrokUrl + "/payment/result?status=fail&error=" + e.getMessage());
		}
	}

	// ── ECPay ─────────────────────────────────────────────────

	/**
	 * 前端呼叫取得 ECPay 付款表單 HTML
	 * POST lazybaobao/payment/ecpay/request
	 */
	@PostMapping(value = "/ecpay/request", produces = MediaType.TEXT_HTML_VALUE)
	public String ecpayRequest(@Valid @RequestBody PaymentInitReq req) {
		try {
			return ecpayService.getEcpayForm(req.getOrderDateId(), req.getId());
		} catch (Exception e) {
			return "<p>ECPay 初始化失敗：" + e.getMessage() + "</p>";
		}
	}

	/**
	 * ECPay 付款結果 Server Notification（via Angular proxy）
	 * POST lazybaobao/payment/ecpay/callback
	 * 回傳純文字 "1|OK" 給 ECPay
	 */
	@PostMapping(value = "/ecpay/callback", produces = MediaType.TEXT_PLAIN_VALUE)
	public String ecpayCallback(@RequestParam Map<String, String> params) {
		return ecpayService.handleCallback(params);
	}
}
