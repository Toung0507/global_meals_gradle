package com.example.global_meals_gradle.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.OrderCartDetailsDao;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.entity.OrderCartDetails;
import com.example.global_meals_gradle.entity.Orders;
import com.example.global_meals_gradle.req.HistoricalOrdersReq;
import com.example.global_meals_gradle.res.GetAllOrdersRes;
import com.example.global_meals_gradle.res.GetOrdersDetailVo;
import com.example.global_meals_gradle.res.GetOrdersVo;

public class OrderService {
	
	@Autowired
	private OrdersDao ordersDao;
	
	@Autowired
	private OrderCartDetailsDao orderCartDetailsDao;
	
	@Autowired
	private ProductsDao productsDao;

	/* 查詢歷史訂單 */
	public GetAllOrdersRes getAllOrders(HistoricalOrdersReq req) {
		/* 判斷會員id是否存在 */
		
		/* 取得主表(Orders)該會員的歷史訂單 */
		List<GetOrdersVo> orderList = ordersDao.getOrderByMemberId(req.getMemberId());
		/* 第一層迴圈跑每一筆訂單拿到各自的明細 */
		for (GetOrdersVo order : orderList) {
			// 根據購物車id，拿到該訂單的明細
			List<OrderCartDetails> detailEntities = orderCartDetailsDao.getProductByOrderCartId(order.getOrderCartId());
			// 要把上面拿到的資料，放進vo裡
			List<GetOrdersDetailVo> detailVoList = new ArrayList<>();
			/* 第二層迴圈，利用產品id 去products表取得產品名稱 */
			for (OrderCartDetails entity : detailEntities) {
				GetOrdersDetailVo vo = new GetOrdersDetailVo();
				// 產品id 去products表取得產品名稱
				String productName = productsDao.getProductsNameById(entity.getProductId());
				// 把各個資料塞進去vo
				vo.setName(productName); // 塞進去上個步驟取的名子
				vo.setQuantity(entity.getQuantity());
				vo.setPrice(entity.getPrice());
				vo.setGift(entity.isGift());
				vo.setDiscountNote(entity.getDiscountNote());
				// 把整理好的資料裝進voList裡
				detailVoList.add(vo);
			}
			// 把整理好的訂單明細，塞進去欄位
			order.setGetOrdersDetailVoList(detailVoList);
		}
		return new GetAllOrdersRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), orderList);
	}
}
