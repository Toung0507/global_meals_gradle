package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.TaxType;
import com.example.global_meals_gradle.dao.RegionsDao;
import com.example.global_meals_gradle.entity.Regions;
import com.example.global_meals_gradle.req.UpsertRegionsTaxReq;
import com.example.global_meals_gradle.req.UpdateRegionsUsageCapReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.RegionsRes;

@Service
public class RegionsService {

	@Autowired
	private RegionsDao regionsDao;
	
	// 新增/修改稅率
	@Transactional(rollbackFor = Exception.class)
	public BasicRes upsert(UpsertRegionsTaxReq req) {
		/* 參數檢查:使用@Valid */
		// 檢查傳進來的是否為 INCLUSIVE / EXCLUSIVE
		if (!TaxType.check(req.getTaxType())) {
			return new BasicRes(ReplyMessage.TAX_TYPE_ERROR.getCode(), //
					ReplyMessage.TAX_TYPE_ERROR.getMessage());
		}
		
		// 新增國家稅值
		try {
			regionsDao.upsertTax(req.getCountry(), req.getCurrencyCode(), req.getCountryCode(), //
					req.getTaxRate(), req.getTaxType().toUpperCase());
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 修改折扣上限
	@Transactional(rollbackFor = Exception.class)
	public BasicRes updateUsageCap(UpdateRegionsUsageCapReq req) {
		
		/* 更新要檢查 regions 表的 id(PK)，因為存在 DB 中的 id 一定是 大於0 */
		// 直接在 UpdateRegionsReq 做資料驗證即可
//		if(req.getId() <= 0) {
//			return new BasicRes(ReplyMessage.REGIONS_ID_ERROR.getCode(), //
//					ReplyMessage.REGIONS_ID_ERROR.getMessage());
//		}
		// 先確認是否有這筆資料
		Regions regions = regionsDao.getById(req.getId());
		if (regions == null) {
			return new BasicRes(ReplyMessage.REGIONS_ID_NOT_FOUND.getCode(), //
					ReplyMessage.REGIONS_ID_NOT_FOUND.getMessage());
		}
		try {
			regionsDao.updateUsageCap(req.getId(), req.getUsageCap());
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 取得各國稅制清單
	public RegionsRes getAllTax() {
		return new RegionsRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), regionsDao.getAll());
	}
	
}
