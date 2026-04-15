package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.GlobalAreaDao;
import com.example.global_meals_gradle.req.CreateGlobalAreaReq;
import com.example.global_meals_gradle.req.DeleteGlobalAreaReq;
import com.example.global_meals_gradle.req.UpdateGlobalAreaReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.GlobalAreaRes;

@Service
public class GlobalAreaService {

	@Autowired
	private GlobalAreaDao globalAreaDao;

	@Autowired
	private ProductService productService;

	// 新增分店
	@Transactional(rollbackFor = Exception.class)
	public BasicRes create(CreateGlobalAreaReq req) {
		/* 參數檢查:使用@Valid */
		try {
			// 1. 原有的 insert 保持不動
			globalAreaDao.insert(req.getCountry(), req.getBranch(), req.getAddress(), req.getPhone());

			// 2. 最小幅度：馬上查出剛才新增的 ID (建議在 DAO 層加一個 findLastId 方法)
			int newBranchId = globalAreaDao.findLastId();

			// 3. 呼叫你的庫存初始化
			productService.initInventoryForNewBranch(newBranchId);
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}

	// 更新分店
	@Transactional(rollbackFor = Exception.class)
	public BasicRes update(UpdateGlobalAreaReq req) {
		try {
			globalAreaDao.update(req.getId(), req.getBranch(), req.getAddress(), req.getPhone());
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
		for (int id : req.getGlobalAreaIdList()) {
			if (id <= 0) {
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
