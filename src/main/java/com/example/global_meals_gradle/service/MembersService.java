package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.MembersDao;
import com.example.global_meals_gradle.entity.Members;
import com.example.global_meals_gradle.req.LoginMembersReq;
import com.example.global_meals_gradle.req.RegisterMembersReq;
import com.example.global_meals_gradle.req.UpdatePasswordReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.MembersRes;
import com.example.global_meals_gradle.utils.PhoneValidatorUtils;

@Service
public class MembersService {

	@Autowired
	private MembersDao membersDao;
	
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	// 註冊
	@Transactional(rollbackFor = Exception.class)
	public BasicRes register(RegisterMembersReq req, boolean isMembers) {
		
		// 電話號碼格式驗證
	    // PhoneValidatorUtil.isValid()：使用 libphonenumber 驗證該電話在指定國家是否合法
	    // 參數1：req.getPhone()     → 使用者輸入的電話號碼
	    // 參數2：req.getCountryCode() → 使用者所在國家（前端傳入，預設 "TW"）
	    // 若驗證失敗（false），直接回傳錯誤，不繼續往下執行
	    if (!PhoneValidatorUtils.isValid(req.getPhone(), req.getCountryCode())) {
	        return new BasicRes(ReplyMessage.PHONE_ERROR.getCode(), //
	        		ReplyMessage.PHONE_ERROR.getMessage());
	    }
	    // 標準化：把電話號碼轉成 E.164 格式再存進 DB
	    // 這樣不管使用者輸入 "0912345678" 還是 "+886912345678"，存進去都是 "+886912345678"
	    // 讓資料庫格式統一，避免重複帳號問題（"0912345678" 和 "+886912345678" 其實是同一個號碼）
	    String normalizedPhone = PhoneValidatorUtils.toE164Format(req.getPhone(), req.getCountryCode());
		try {
			// 判斷是會員還是訪客
			if (isMembers) {
				// 會員註冊：只需檢查電話是否被「其他正式會員」佔用
				// 用標準化後的號碼做重複檢查 + 寫入
				if(membersDao.getByPhoneExcludeGuest(normalizedPhone) != null) {
					return new BasicRes(ReplyMessage.PHONE_HAS_EXISTED.getCode(), //
							ReplyMessage.PHONE_HAS_EXISTED.getMessage());
				}
				// 會員註冊需額外檢查密碼是否存在
				if(req.getPassword() == null || req.getPassword().isBlank()) {
					return new BasicRes(ReplyMessage.PASSWORD_NOT_ENTERED.getCode(), //
							ReplyMessage.PASSWORD_NOT_ENTERED.getMessage());
				}
				membersDao.registerMember(req.getName(), normalizedPhone, req.getCountryCode(), encoder.encode(req.getPassword()));
			} else {
				// 訪客註冊：只需檢查是否佔用到「正式會員(id != 1)」的手機
	            if (membersDao.getByPhoneExcludeGuest(normalizedPhone) != null) {
	                return new BasicRes(ReplyMessage.PHONE_HAS_EXISTED.getCode(), //
							ReplyMessage.PHONE_HAS_EXISTED.getMessage());
	            }
	            // 覆蓋 id = 1 的資料
				membersDao.registerGuest(req.getName(), normalizedPhone, req.getCountryCode());
			}
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
	
	// 登入
	public MembersRes login(LoginMembersReq req) {
		
		// 電話驗證
	    if (!PhoneValidatorUtils.isValid(req.getPhone(), req.getCountryCode())) {
	        return new MembersRes(ReplyMessage.PHONE_ERROR.getCode(), //
	        		ReplyMessage.PHONE_ERROR.getMessage());
	    }
	    // 標準化後再查詢（DB 裡存的是 E.164 格式）
	    String normalizedPhone = PhoneValidatorUtils.toE164Format(req.getPhone(), req.getCountryCode());
		Members members = membersDao.getByPhone(normalizedPhone);
		// 檢查會員是否存在
		// 只有 id != 1 且有密碼的才能登入 (訪客 id=1 沒有密碼，不允許走登入流程)
		if(members != null && members.getId() != 1 && members.getPassword() != null) {
			// 比對密碼
			if(encoder.matches(req.getPassword(), members.getPassword())) {
				// 先清空，防止加密後的 Hash 傳回前端
				members.setPassword(null);
				return new MembersRes(ReplyMessage.SUCCESS.getCode(), //
						ReplyMessage.SUCCESS.getMessage(), members);
			}
		}
		return new MembersRes(ReplyMessage.PHONE_OR_PASSWORD_ERROR.getCode(), //
				ReplyMessage.PHONE_OR_PASSWORD_ERROR.getMessage());
	}
	
	// 修改會員密碼
	@Transactional(rollbackFor = Exception.class)
	public BasicRes updatePassword(UpdatePasswordReq req) {
		
		// 訪客 (id=1) 不允許修改密碼
		if(req.getId() == 1) {
			return new BasicRes(ReplyMessage.GUEST_CANT_UPDATE.getCode(), //
					ReplyMessage.GUEST_CANT_UPDATE.getMessage());
		}
		
		try {
			Members members = membersDao.findById(req.getId());
			// 檢查會員是否存在，以及訪客不會有密碼
			if(members == null || members.getPassword() == null) {
				return new BasicRes(ReplyMessage.MEMBER_NOT_FOUND.getCode(), //
						ReplyMessage.MEMBER_NOT_FOUND.getMessage());
			}
			// 比對舊密碼
			boolean result = encoder.matches(req.getOldPassword(), members.getPassword());
			if(!result) {
				return new BasicRes(ReplyMessage.OLDPASSWORD_ERROR.getCode(), //
						ReplyMessage.OLDPASSWORD_ERROR.getMessage());
			}
			int res = membersDao.updatePassword(req.getId(), encoder.encode(req.getNewPassword()));
			if(res != 0) {
				return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
						ReplyMessage.SUCCESS.getMessage());
			}
		} catch (Exception e) {
			throw e;
		}
		return new BasicRes(ReplyMessage.UPDATE_FAILED.getCode(), //
				ReplyMessage.UPDATE_FAILED.getMessage());
	}

	// POS 根據手機號碼查詢正式會員
	public MembersRes getMemberByPhone(String phone) {
		// 支援本地格式：09XXXXXXXX → +886XXXXXXXXX
		String normalized = phone.replaceAll("-", "").trim();
		if (normalized.matches("^09\\d{8}$")) {
			normalized = "+886" + normalized.substring(1);
		}
		Members member = membersDao.getByPhone(normalized);
		// 只回傳正式會員（非訪客 id=1、且有密碼）
		if (member != null && member.getId() != 1 && member.getPassword() != null) {
			member.setPassword(null);
			return new MembersRes(ReplyMessage.SUCCESS.getCode(),
					ReplyMessage.SUCCESS.getMessage(), member);
		}
		return new MembersRes(404, "查無此會員");
	}

}
