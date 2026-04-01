package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.RegionsDao;
import com.example.global_meals_gradle.req.CreateRegionsReq;
import com.example.global_meals_gradle.req.UpdateRegionsReq;
import com.example.global_meals_gradle.res.BasicRes;

@Service
public class RegionsService {

	@Autowired
	private RegionsDao regionsDao;
	
	// 新增
	@Transactional(rollbackFor = Exception.class)
	public BasicRes create(CreateRegionsReq req) {
		/* 參數檢查:使用@Valid */
		// 新增國家稅值
		// 在使用 nativeQuery = true 時，JPA 有時無法自動將 TaxType (Enum) 轉換為資料庫認識的字串。建議在傳參時呼叫 .name()。
		try {
			regionsDao.insert(req.getCountry(), req.getCurrencyCode(), req.getTaxRate(), req.getTaxType().name());
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 修改
	@Transactional(rollbackFor = Exception.class)
	public BasicRes update(UpdateRegionsReq req) {
		
		/* 更新要檢查 regions 表的 id(PK)，因為存在 DB 中的 id 一定是 大於0 */
		// 直接在 UpdateRegionsReq 做資料驗證即可
//		if(req.getId() <= 0) {
//			return new BasicRes(ReplyMessage.REGIONS_ID_ERROR.getCode(), //
//					ReplyMessage.REGIONS_ID_ERROR.getMessage());
//		}
		try {
			regionsDao.update(req.getId(), req.getTaxRate(), req.getTaxType().name());
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
}
