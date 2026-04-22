package com.example.global_meals_gradle.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.StaffRole;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.dao.RegionsDao;
import com.example.global_meals_gradle.entity.Regions;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.MonthlyProductsSalesReq;
import com.example.global_meals_gradle.res.MonthlyProductsSalesRes;
import com.example.global_meals_gradle.res.MonthlyProductsSalesVo;

@Service
public class ProductService {
	private static final int YEAR_MIN = 2020;
	@Autowired
	private OrdersDao ordersDao;
	@Autowired
	private RegionsDao regionsDao;

	/*
	 * =================================================================
	 * 【功能A】分店長查詢：某年某月 該分店 所有商品銷售量
	 *
	 * 權限規則： - 只有 REGION_MANAGER（分店長）可以呼叫此方法 - 分店長只能看自己分店（globalAreaId）的資料，不能跨店查詢 -
	 * globalAreaId 從 Session 的 operator 物件取，不信任前端傳入
	 *
	 * operator：從 Controller 傳進來的 Session 登入者物件
	 * =================================================================
	 */
	@Transactional(readOnly = true)
	public MonthlyProductsSalesRes getMonthlySalesByBranch(MonthlyProductsSalesReq req, Staff operator) {

		// Step 1：確認是分店長
		// operator.getRole() 取出 Session 裡存的 Staff 的身份（StaffRole enum）
		if (operator.getRole() != StaffRole.REGION_MANAGER) {
			return new MonthlyProductsSalesRes(ReplyMessage.OPERATE_ERROR.getCode(),
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// Step 2：年份基本防呆（不能查未來，不能查太久以前）
		int currentYear = java.time.LocalDate.now().getYear();
		if (req.getYear() < YEAR_MIN  || req.getYear() > currentYear) {
			return new MonthlyProductsSalesRes(400, "年份超出合法範圍");
		}
		// 未來月份防呆（加在年份防呆的正後面）──
		// 取得當前月份（1 ~ 12）
		int currentMonth = java.time.LocalDate.now().getMonthValue();
		// 條件：今年 且 查的月份 > 現在這個月 = 查未來，擋掉
		if (req.getYear() == currentYear && req.getMonth() > currentMonth) {
		    // %d 填年份，%02d 填月份（不足兩位補0，例如4→04）
		    return new MonthlyProductsSalesRes(400,
		        String.format("%d年%02d月還未結束，暫無銷售數據", req.getYear(), req.getMonth()));
		}

		// Step 3：把 year + month 組成 SQL LIKE 用的字串，例如 "202604%"
		// %d = 年份原樣輸出，%02d = 月份不足兩位補零（4 → "04"），%% = SQL 的萬用字元 %
		String yearMonth = String.format("%d%02d%%", req.getYear(), req.getMonth());

		// Step 4：取出分店長自己的分店 ID（從 Session 取，不信任前端）
		// 這是安全設計的核心：Session 存在伺服器端，前端無法偽造
		int globalAreaId = operator.getGlobalAreaId();

		// Step 5：呼叫 DAO 查詢該分店的銷售資料
		List<Object[]> rawData = ordersDao.getMonthlySalesByBranch(yearMonth, globalAreaId);
		// ── 空結果語意化──
		// 如果查回來是空的，代表這個月/分店確實沒有任何已完成的訂單
		if (rawData == null || rawData.isEmpty()) {
		    // 回傳 200（不是錯誤），但附上清楚的說明訊息
		    // 傳空清單，讓前端知道是「真的沒有資料」而不是系統錯誤
		    return new MonthlyProductsSalesRes(
		        ReplyMessage.SUCCESS.getCode(),
		        String.format("%d年%02d月查無銷售記錄", req.getYear(), req.getMonth()),
		        new ArrayList<>()  // 明確傳空清單，不是 null
		    );
		}
		
		List<MonthlyProductsSalesVo> salesList = toVoList(rawData);


		return new MonthlyProductsSalesRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(),
				salesList);
	}

	/*
	 * =================================================================
	 * 【功能B】老闆查詢：某年某月 指定國家 所有分店 銷售前5名商品（v2 更新）
	 *
	 * 權限規則： - 只有 ADMIN（老闆）可以呼叫此方法 - 老闆可以選擇任何國家（regionId 由前端傳入是安全的，因為角色已驗過） -
	 * regionId 對應 regions 表的 id 欄位
	 *
	 * operator：從 Controller 傳進來的 Session 登入者物件
	 * =================================================================
	 */
	@Transactional(readOnly = true)
	public MonthlyProductsSalesRes getTop5MonthlySalesByRegion(MonthlyProductsSalesReq req, Staff operator) {

		// Step 1：確認是老闆
		if (operator.getRole() != StaffRole.ADMIN) {
			return new MonthlyProductsSalesRes(ReplyMessage.OPERATE_ERROR.getCode(),
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// Step 2：年份防呆
		int currentYear = java.time.LocalDate.now().getYear();
		if (req.getYear() < YEAR_MIN || req.getYear() > currentYear) {
			return new MonthlyProductsSalesRes(400, "年份超出合法範圍");
		}
		// ── 未來月份防呆
		int currentMonth = java.time.LocalDate.now().getMonthValue();
		if (req.getYear() == currentYear && req.getMonth() > currentMonth) {
		    return new MonthlyProductsSalesRes(400,
		        String.format("%d年%02d月還未結束，暫無銷售數據", req.getYear(), req.getMonth()));
		}

		// Step 3：國家 ID 防呆（老闆查詢必須傳 regionId）
		if (req.getRegionId() == null || req.getRegionId() <= 0) {
			return new MonthlyProductsSalesRes(400, "請選擇國家");
		}
		Regions targetRegion = regionsDao.findById(req.getRegionId()).orElse(null);
		if (targetRegion == null) {
		    return new MonthlyProductsSalesRes(400,
		        "國家 ID " + req.getRegionId() + " 不存在，請重新選擇");
		}
		// Step 4：組成 LIKE 字串
		String yearMonth = String.format("%d%02d%%", req.getYear(), req.getMonth());

		// Step 5：查詢指定國家的所有分店 TOP 5
		// SQL 內部會 JOIN global_area + regions 過濾 r.id = regionId
		List<Object[]> rawData = ordersDao.getTop5MonthlySalesByRegion(yearMonth, req.getRegionId());
		// ── 空結果語意化
				// 如果查回來是空的，代表這個月份/國家確實沒有任何已完成的訂單
				if (rawData == null || rawData.isEmpty()) {
				    return new MonthlyProductsSalesRes(
				        ReplyMessage.SUCCESS.getCode(),
				        String.format("%d年%02d月在此國家查無銷售記錄", req.getYear(), req.getMonth()),
				        new ArrayList<>()  // 明確傳空清單，不是 null
				    );
				}
		// Step 6：轉 VO（與功能A共用同樣格式）
		// 原本那段 for 迴圈，刪掉，改成：
		List<MonthlyProductsSalesVo> salesList = toVoList(rawData);


		return new MonthlyProductsSalesRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(),
				salesList);
	}
	// =====================================================================
	// 私有工具方法：把 DB 原始資料（Object[]）轉換成前端看得懂的 VO 清單
	// Step 6：把 Object[] 轉成前端看得懂的 VO 物件
			// Object[0] = productName（商品名稱，String）
			// Object[1] = totalQuantity（銷售總量）
			//
			// 【為什麼用 ((Number) row[1]).intValue()？】
			// SUM() 在 MySQL 中永遠回傳 DECIMAL，JPA 會把它映射成 Java 的 BigDecimal。
			// 用 (Number) 父類別接住，再呼叫 intValue()，幫我轉成 int（整數）來用,
			//比直接 (Integer) 或 (BigDecimal) 更安全，
			// 不管 DB 回的是 BigDecimal / Long / Integer 都不會噴 ClassCastException。
			// 原本那段 for 迴圈，刪掉，改成：
	// =====================================================================
	private List<MonthlyProductsSalesVo> toVoList(List<Object[]> rawData) {
	    // 建立一個空清單，準備裝填轉換好的 VO
	    List<MonthlyProductsSalesVo> salesList = new ArrayList<>();
	    // 逐一把每列原始資料（Object[]）轉成乾淨的 VO 物件
	    for (Object[] row : rawData) {
	        MonthlyProductsSalesVo vo = new MonthlyProductsSalesVo();
	        // row[0] 是 SQL 第一欄 p.name（商品名稱），強制轉成 String
	     // row[0] 是 p.name（商品名稱），null 時顯示「未知商品」避免前端爆版
	        vo.setProductName(row[0] != null ? (String) row[0] : "未知商品");
	        // row[1] 是 SQL 第二欄 SUM(d.quantity)（銷售總量）
	        // MySQL SUM() 回傳 DECIMAL，JPA 映射成 BigDecimal
	        // 用 Number 父類別接住，再 .intValue() 轉成 int，最安全
	        // null 防護：如果 SUM 回傳 null（理論上 GROUP BY 後不該發生，但保險一下），設為 0
	        vo.setTotalQuantity(row[1] != null ? ((Number) row[1]).intValue() : 0);
	        salesList.add(vo);
	    }
	    return salesList; // 回傳裝好的清單
	}

}
