package com.example.global_meals_gradle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.global_meals_gradle.dao.StaffDao;
import com.example.global_meals_gradle.entity.Staff;

@Service
public class StaffService {
	
	
	//登入
//	@Autowired
//    private StaffDao staffDao;
//	
//    public Staff login(String account, String password) {
//    	
//        // 1. 呼叫你手寫的 SQL：findByAccount
//        Staff staff = staffDao.findByAccount(account);
//
//        // 2. 檢查是否有這個人
//        if (staff == null) {
//            throw new RuntimeException("帳號不存在");
//        }
//
//        // 3. 比對密碼 (肌肉記憶提醒：實務上要用 BCrypt.checkpw)
//        // 為了讓你先跑通，我們先用最白話的字串比對
//        if (!staff.getPassword().equals(password)) {
//            throw new RuntimeException("密碼錯誤");
//        }
//
//        // 4. 檢查是否被停權 (is_status 是你 DAO 裡的欄位)
//        if (!staff.isStatus()) {
//            throw new RuntimeException("此帳號已被停權，請洽管理員");
//        }
//
//        // 5. 通過驗證，回傳員工資料（後續丟給 Controller 存進 Session）
//        return staff;
//    }
}
