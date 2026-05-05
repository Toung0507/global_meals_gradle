package com.example.global_meals_gradle.service; // 宣告此類別所屬的套件路徑

import java.util.List; // 回傳多筆 Discount 資料

import org.springframework.beans.factory.annotation.Autowired; // 自動注入 Spring Bean
import org.springframework.stereotype.Service; // 標記為 Service 層元件，讓 Spring 掃描並管理
import org.springframework.transaction.annotation.Transactional; // 宣告此方法需要交易管理

import com.example.global_meals_gradle.constants.ReplyMessage; // 統一的回應代碼與訊息列舉
import com.example.global_meals_gradle.dao.DiscountDao; // 操作 discount 表的 DAO 介面
import com.example.global_meals_gradle.dao.RegionsDao; // 操作 regions 表，用於驗證 regionsId 是否存在
import com.example.global_meals_gradle.entity.Discount; // discount 表對應的 JPA Entity
import com.example.global_meals_gradle.req.DiscountReq; // 接收前端傳入的請求參數
import com.example.global_meals_gradle.res.BasicRes; // 通用回應物件（code + message）
import com.example.global_meals_gradle.res.DiscountRes; // 查詢清單用的回應物件

@Service // 標記為 Service 層，Spring 會自動掃描並建立 Bean
public class DiscountService {

	@Autowired // 自動注入 DiscountDao
	private DiscountDao discountDao; // 負責對 discount 表執行 CRUD

	@Autowired // 自動注入 RegionsDao
	private RegionsDao regionsDao; // 負責驗證 regions_id 是否存在於 regions 表

	// =============================================
	// 新增 discount 記錄
	// =============================================

	@Transactional // 寫入操作需要交易，確保失敗時可以 rollback
	public BasicRes create(DiscountReq req) { // 接收新增請求參數

		if (req.getRegionsId() <= 0) { // 驗證 regionsId 必須大於 0（0 或負數無意義）
			return new BasicRes(ReplyMessage.REGIONS_ID_ERROR.getCode(), // 回傳 400
					ReplyMessage.REGIONS_ID_ERROR.getMessage()); // "Regions Id Error!!"
		}

		if (regionsDao.getById(req.getRegionsId()) == null) { // 驗證 regionsId 是否真的存在於 regions 表
			return new BasicRes(ReplyMessage.REGIONS_ID_NOT_FOUND.getCode(), // 回傳 404
					ReplyMessage.REGIONS_ID_NOT_FOUND.getMessage()); // "Regions Id Not Found!!"
		}

		if (req.getUsageCap() <= 0) { // 驗證折抵上限必須大於 0（0 或負數無意義）
			return new BasicRes(ReplyMessage.USAGE_CAP_NOT_FOUND.getCode(), // 回傳 404
					ReplyMessage.USAGE_CAP_NOT_FOUND.getMessage()); // "Usage Cap Not Found!!"
		}

		Discount discount = new Discount(); // 建立 Discount Entity 物件準備寫入
		discount.setRegionsId(req.getRegionsId()); // 設定關聯的國家區域 ID
		discount.setCount(req.getCount()); // 設定消費累積次數（前端未傳時預設為 0）
		discount.setUsageCap(req.getUsageCap()); // 設定折抵上限金額

		discountDao.save(discount); // JPA save() 寫入 discount 表，id 由 AUTO_INCREMENT 自動產生

		return new BasicRes(ReplyMessage.SUCCESS.getCode(), // 回傳 200
				ReplyMessage.SUCCESS.getMessage()); // "Success!!"
	}

	// =============================================
	// 查詢全部 discount 記錄
	// =============================================

	public DiscountRes getList() { // 純查詢，不需要 @Transactional

		List<Discount> list = discountDao.findAllDiscounts(); // 查出 discount 表所有資料

		return new DiscountRes(ReplyMessage.SUCCESS.getCode(), // 回傳 200
				ReplyMessage.SUCCESS.getMessage(), // "Success!!"
				list); // 帶入完整折抵記錄清單
	}

	// =============================================
	// 查詢單筆 discount 記錄
	// =============================================

	public DiscountRes getById(int id) { // 純查詢，不需要 @Transactional

		Discount discount = discountDao.findDiscountById(id); // 以主鍵精準查詢

		if (discount == null) { // 查不到表示此 id 不存在
			return new DiscountRes(ReplyMessage.USAGE_CAP_NOT_FOUND.getCode(), // 回傳 404
					ReplyMessage.USAGE_CAP_NOT_FOUND.getMessage()); // "Usage Cap Not Found!!"
		}

		return new DiscountRes(ReplyMessage.SUCCESS.getCode(), // 回傳 200
				ReplyMessage.SUCCESS.getMessage(), // "Success!!"
				List.of(discount)); // 將單筆結果包成清單回傳
	}

	// =============================================
	// 修改 discount 的折抵上限（usage_cap）
	// =============================================

	@Transactional // 寫入操作需要交易，確保失敗時可以 rollback
	public BasicRes updateUsageCap(DiscountReq req) { // 接收 id 與新的 usageCap

		if (discountDao.findDiscountById(req.getId()) == null) { // 驗證此 discount 記錄是否存在
			return new BasicRes(ReplyMessage.USAGE_CAP_NOT_FOUND.getCode(), // 找不到時回傳 404
					ReplyMessage.USAGE_CAP_NOT_FOUND.getMessage()); // "Usage Cap Not Found!!"
		}

		if (req.getUsageCap() <= 0) { // 驗證新的折抵上限必須大於 0
			return new BasicRes(ReplyMessage.USAGE_CAP_NOT_FOUND.getCode(), // 數值不合法時回傳 404
					ReplyMessage.USAGE_CAP_NOT_FOUND.getMessage()); // "Usage Cap Not Found!!"
		}

		discountDao.updateUsageCap(req.getId(), req.getUsageCap()); // 執行 UPDATE discount SET usage_cap = ? WHERE id = ?

		return new BasicRes(ReplyMessage.SUCCESS.getCode(), // 回傳 200
				ReplyMessage.SUCCESS.getMessage()); // "Success!!"
	}

	// =============================================
	// 修改 discount 的累積次數（count）
	// =============================================

	@Transactional // 寫入操作需要交易，確保失敗時可以 rollback
	public BasicRes updateCount(DiscountReq req) { // 接收 id 與新的 count

		if (discountDao.findDiscountById(req.getId()) == null) { // 驗證此 discount 記錄是否存在
			return new BasicRes(ReplyMessage.COUNT_NOT_FOUND.getCode(), // 找不到時回傳 404
					ReplyMessage.COUNT_NOT_FOUND.getMessage()); // "Count Not Found!!"
		}

		if (req.getCount() < 0) { // 驗證累積次數不能為負數
			return new BasicRes(ReplyMessage.COUNT_NOT_FOUND.getCode(), // 數值不合法時回傳 404
					ReplyMessage.COUNT_NOT_FOUND.getMessage()); // "Count Not Found!!"
		}

		discountDao.updateCount(req.getId(), req.getCount()); // 執行 UPDATE discount SET count = ? WHERE id = ?

		return new BasicRes(ReplyMessage.SUCCESS.getCode(), // 回傳 200
				ReplyMessage.SUCCESS.getMessage()); // "Success!!"
	}

	// =============================================
	// 刪除 discount 記錄（真刪除）
	// =============================================

	@Transactional // 寫入操作需要交易，確保失敗時可以 rollback
	public BasicRes delete(int id) { // 接收要刪除的 discount 主鍵

		if (discountDao.findDiscountById(id) == null) { // 驗證此 discount 記錄是否存在
			return new BasicRes(ReplyMessage.USAGE_CAP_NOT_FOUND.getCode(), // 找不到時回傳 404
					ReplyMessage.USAGE_CAP_NOT_FOUND.getMessage()); // "Usage Cap Not Found!!"
		}

		discountDao.deleteDiscountById(id); // 執行 DELETE FROM discount WHERE id = ?

		return new BasicRes(ReplyMessage.SUCCESS.getCode(), // 回傳 200
				ReplyMessage.SUCCESS.getMessage()); // "Success!!"
	}

}
