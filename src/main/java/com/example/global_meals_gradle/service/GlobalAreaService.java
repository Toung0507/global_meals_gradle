package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.GlobalAreaDao;
import com.example.global_meals_gradle.dao.RegionsDao;
import com.example.global_meals_gradle.entity.GlobalArea;
import com.example.global_meals_gradle.entity.Regions;
import com.example.global_meals_gradle.req.CreateGlobalAreaReq;
import com.example.global_meals_gradle.req.DeleteGlobalAreaReq;
import com.example.global_meals_gradle.req.UpdateGlobalAreaReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.GlobalAreaRes;
import com.example.global_meals_gradle.utils.PhoneValidatorUtils;

@Service
public class GlobalAreaService {
	
	@Autowired
	private GlobalAreaDao globalAreaDao;
	
	@Autowired
	private RegionsDao regionsDao;
	
	// 新增分店
	@Transactional(rollbackFor = Exception.class)
	public BasicRes create(CreateGlobalAreaReq req) {
		/* 參數檢查:使用@Valid */
		// 根據 regionsId 撈取區域資訊
		Regions regions = regionsDao.getById(req.getRegionsId());
		if (regions == null) {
			return new BasicRes(ReplyMessage.REGIONS_ID_NOT_FOUND.getCode(), //
					ReplyMessage.REGIONS_ID_NOT_FOUND.getMessage());
		}
		try {
			String countryCode = regions.getCountryCode();
			// 電話驗證
			if (!PhoneValidatorUtils.isValid(req.getPhone(), countryCode)) {
				return new BasicRes(ReplyMessage.PHONE_ERROR.getCode(), //
						ReplyMessage.PHONE_ERROR.getMessage());
			}
			// 電話標準化
			String normalizedPhone = PhoneValidatorUtils.toE164Format(req.getPhone(), countryCode);
			globalAreaDao.insert(req.getRegionsId(), req.getBranch(), req.getAddress(), normalizedPhone);
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 更新分店
	@Transactional(rollbackFor = Exception.class)
	public BasicRes update(UpdateGlobalAreaReq req) {
		// GlobalArea globalArea = globalAreaDao.findById(req.getId());
		Regions regions = regionsDao.getById(req.getRegionsId());
		if (globalAreaDao.findById(req.getId()) == null) {
			return new BasicRes(ReplyMessage.GLOBAL_AREA_NOT_FOUND.getCode(), //
					ReplyMessage.GLOBAL_AREA_NOT_FOUND.getMessage());
		}
		if (regions == null) {
			return new BasicRes(ReplyMessage.REGIONS_ID_NOT_FOUND.getCode(), //
					ReplyMessage.REGIONS_ID_NOT_FOUND.getMessage());
		}
		try {
			String countryCode = regions.getCountryCode();
			if (!PhoneValidatorUtils.isValid(req.getPhone(), countryCode)) {
				return new BasicRes(ReplyMessage.PHONE_ERROR.getCode(), //
						ReplyMessage.PHONE_ERROR.getMessage());
			}
			String normalizedPhone = PhoneValidatorUtils.toE164Format(req.getPhone(), countryCode);
			globalAreaDao.update(req.getId(), req.getBranch(), req.getAddress(), normalizedPhone);
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 取得分店清單
	public GlobalAreaRes getAllBranch() {
		return new GlobalAreaRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), globalAreaDao.getAll());
	}
	
	// 刪除多個分店
	@Transactional(rollbackFor = Exception.class)
	public BasicRes delete(DeleteGlobalAreaReq req) {
		for(int id : req.getGlobalAreaIdList()) {
			if(id <= 0) {
				return new BasicRes(ReplyMessage.GLOBAL_AREA_ID_ERROR.getCode(), //
						ReplyMessage.GLOBAL_AREA_ID_ERROR.getMessage());
			}
		}
		try {
			globalAreaDao.delete(req.getGlobalAreaIdList());
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}

}
