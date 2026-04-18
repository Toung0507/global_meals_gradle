package com.example.global_meals_gradle.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.req.MonthlyProductsSalesReq;
import com.example.global_meals_gradle.res.MonthlyProductsSalesRes;
import com.example.global_meals_gradle.res.MonthlyProductsSalesVo;

@Service
public class ProductService {

	@Autowired
	private OrdersDao ordersDao;

	/* 查詢某年某月每個商品的銷售量 */
	@Transactional(readOnly = true)
	public MonthlyProductsSalesRes getMonthlyProductsSales(MonthlyProductsSalesReq req) {

		// 🛡️ 動態防禦：抓今年的年份，限制只能查近 5 年 (今年 ~ 往回推4年)
		int currentYear = LocalDate.now().getYear();

		if (req.getYear() > currentYear || req.getYear() < (currentYear - 4)) {
			throw new RuntimeException("僅提供查詢近 5 年內（" + (currentYear - 4) + " ~ " + currentYear + "年）的數據");
		}

		// 拼成 SQL LIKE 需要的格式："202604%"
		String yearMonth = String.format("%d%02d%%", req.getYear(), req.getMonth());

		// 去資料庫查詢
		List<Object[]> rawData = ordersDao.getMonthlySales(yearMonth);

		// 若查無資料，直接回傳明確訊息，而不是空的清單
		if (rawData.isEmpty()) {
			return new MonthlyProductsSalesRes(ReplyMessage.SUCCESS.getCode(),
					req.getYear() + "年" + req.getMonth() + "月 查無銷售資料");
		}
		// 確認有資料之後，才去做後面的轉換
		// 準備要回傳的獨立 VO 清單
		List<MonthlyProductsSalesVo> salesList = new ArrayList<>();
		for (Object[] row : rawData) {
			MonthlyProductsSalesVo vo = new MonthlyProductsSalesVo();
			vo.setProductName((String) row[0]);
			vo.setTotalQuantity(((Number) row[1]).intValue());
			salesList.add(vo);
		}

		return new MonthlyProductsSalesRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(),
				salesList);
	}
}
