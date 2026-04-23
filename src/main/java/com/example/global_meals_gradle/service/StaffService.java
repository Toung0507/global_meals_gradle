package com.example.global_meals_gradle.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.StaffRole;
import com.example.global_meals_gradle.dao.StaffDao;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.ChangePasswordReq;
import com.example.global_meals_gradle.req.LoginStaffReq;
import com.example.global_meals_gradle.req.RegisterStaffReq;
import com.example.global_meals_gradle.req.UpdateStaffPasswordReq;
import com.example.global_meals_gradle.req.UpdateStaffStatusReq;
import com.example.global_meals_gradle.res.StaffSearchRes;

@Service
public class StaffService {

	/*
	 * 角色（怕我忘記）： ADMIN =老闆（不能控制自己） 
	 * REGION_MANAGER = 分店長（縮寫 RM，管一個分店） 
	 * MANAGER_AGENT  = 副店長（MA）：有日常操作權，但不管人（不能建帳號、不能停權、不能改別人密碼）
	 * STAFF = 普通員工（縮寫 ST，負責被管的那位）
	 * =====================================================
	 */

	@Autowired
	private StaffDao staffDao;

	// BCryptPasswordEncoder：密碼加密器
	// 把密碼丟進去，出來的是一串看不懂的亂碼
	// 即使資料庫被偷也看不到明文密碼
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/* =====================================================
	 * 主功能 1：【新增員工】
	 * ===================================================== */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes register(RegisterStaffReq req, Staff operator) {

		// 1. 基礎防呆檢查
		if (!StringUtils.hasText(req.getName())) {
			return new StaffSearchRes(ReplyMessage.NAME_ERROR.getCode(), ReplyMessage.NAME_ERROR.getMessage());
		}
		if (!StringUtils.hasText(req.getPassword())) {
			return new StaffSearchRes(ReplyMessage.PASSWORD_ERROR.getCode(), ReplyMessage.PASSWORD_ERROR.getMessage());
		}
		if (!StringUtils.hasText(req.getRole())) {
			return new StaffSearchRes(ReplyMessage.ROLE_ERROR.getCode(), ReplyMessage.ROLE_ERROR.getMessage());
		}

		// 2. 字串轉 Enum 保護
		StaffRole targetRole;
		try {
			targetRole = StaffRole.valueOf(req.getRole().toUpperCase());
		} catch (IllegalArgumentException e) {
			return new StaffSearchRes(ReplyMessage.ROLE_ERROR.getCode(), ReplyMessage.ROLE_ERROR.getMessage());
		}

		// 3. 呼叫私有方法：檢查業務權限
		if (!hasRegisterPermission(operator, targetRole, req.getGlobalAreaId())) {
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// 4. 產生帳號與防撞號
		String autoAccount = generateAccount(targetRole);
		if (staffDao.findByAccount(autoAccount) != null) {
			return new StaffSearchRes(ReplyMessage.REPEAT_ERROR.getCode(), ReplyMessage.REPEAT_ERROR.getMessage());
		}
		try {
			// 5. 寫入資料庫 (移除多餘的 try-catch)
			staffDao.insert(
					req.getName(), 
					autoAccount, 
					encoder.encode(req.getPassword()), 
					targetRole.name(),
					req.getGlobalAreaId());
		} catch (Exception e) {
			throw e;
		}
		Staff newStaff = staffDao.findByAccount(autoAccount);
		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), List.of(newStaff));
	}

	/* =====================================================
	 * 主功能 2：【修改密碼】
	 * ===================================================== */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes changePassword(int targetId, ChangePasswordReq req, Staff operator) {

		Staff targetStaff = staffDao.findById(targetId).orElse(null);
		if (targetStaff == null) {
			return new StaffSearchRes(ReplyMessage.STAFF_ID_NOT_FOUND.getCode(), ReplyMessage.STAFF_ID_NOT_FOUND.getMessage());
		}

		// 呼叫私有方法：檢查業務操作權限
		if (!hasOperatePermission(operator, targetStaff)) {
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), ReplyMessage.OPERATE_ERROR.getMessage());
		}

		targetStaff.setPassword(encoder.encode(req.getNewPassword()));
		staffDao.save(targetStaff); // 移除多餘的 try-catch

		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	// 修改自己的密碼
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes selfChangePassword(UpdateStaffPasswordReq req, Staff operator) {
	    
	    // 1. 從資料庫重新抓取目前的員工資料（確保資料是最新的）
	    Staff staff = staffDao.findById(operator.getId()).orElse(null);
	    if (staff == null) {
	        return new StaffSearchRes(ReplyMessage.STAFF_ID_NOT_FOUND.getCode(), ReplyMessage.STAFF_ID_NOT_FOUND.getMessage());
	    }

	    // 2. 核心檢查：驗證舊密碼是否正確
	    // 注意：資料庫存的是加密後的密碼，必須用 encoder.matches 比對
	    if (!encoder.matches(req.getOldPassword(), staff.getPassword())) {
	        return new StaffSearchRes(ReplyMessage.OLDPASSWORD_ERROR.getCode(), ReplyMessage.OLDPASSWORD_ERROR.getMessage());
	    }

	    // 3. 執行修改：加密新密碼並存檔
	    staff.setPassword(encoder.encode(req.getNewPassword()));
	    staffDao.save(staff);

	    return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}
	/* =====================================================
	 * 主功能 3：【停權 / 復權】
	 * ===================================================== */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes updateStatus(int targetId, UpdateStaffStatusReq req, Staff operator) {

		Staff targetStaff = staffDao.findById(targetId).orElse(null);
		if (targetStaff == null) {
			return new StaffSearchRes(ReplyMessage.STAFF_ID_NOT_FOUND.getCode(), ReplyMessage.STAFF_ID_NOT_FOUND.getMessage());
		}

		// 這裡也是呼叫同一個私有方法！因為停權和改密碼的權限規則完全一樣
		if (!hasOperatePermission(operator, targetStaff)) {
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), ReplyMessage.OPERATE_ERROR.getMessage());
		}

		staffDao.updateStatus(targetId, req.isNewStatus()); // 移除多餘的 try-catch

		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	/* =====================================================
	 * 主功能 4：【查詢員工清單】
	 * ===================================================== */
	public StaffSearchRes getStaffList(Staff operator) {
		StaffRole role = operator.getRole();

		if (role == StaffRole.ADMIN) {
			return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), staffDao.getAllRM());
		}
		if (role == StaffRole.REGION_MANAGER) {
			return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), staffDao.getSTListById(operator.getGlobalAreaId()));
		}
		return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), ReplyMessage.OPERATE_ERROR.getMessage());
	}

	/* =====================================================
	 * 主功能 5：【登入】
	 * ===================================================== */
	public StaffSearchRes login(LoginStaffReq req) {
		Staff staff = staffDao.findByAccount(req.getAccount());
		if (staff == null) {
			return new StaffSearchRes(ReplyMessage.ACCOUNT_NOT_FOUND.getCode(), ReplyMessage.ACCOUNT_NOT_FOUND.getMessage());
		}
		if (!encoder.matches(req.getPassword(), staff.getPassword())) {
			return new StaffSearchRes(ReplyMessage.PASSWORD_MISMATCH.getCode(), ReplyMessage.PASSWORD_MISMATCH.getMessage());
		}
		if (!staff.isStatus()) {
			return new StaffSearchRes(ReplyMessage.ACCOUNT_DISABLED.getCode(), ReplyMessage.ACCOUNT_DISABLED.getMessage());
		}
		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), List.of(staff));
	}

	/* =====================================================
	 * 主功能：【指派副店長 / 職務晉升】
	 * 邏輯：RM 將所屬區域的 ST 提升為 MA，並同步更改帳號前綴
	 * 	- 規格：
	 *  - 只有 ADMIN 或 該店的 RM 可以操作
	 *  - 目標必須是同一個 globalAreaId 的 STAFF
	 *  - 採用原地晉升：保留 ID，只改 role + account
	 *  - 不提供降級（規格第四點：沒有降級功能，不佳就直接停權）
	 * ===================================================== */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes promoteToManagerAgent(int targetId, Staff operator) {
		
		// 1. 權限檢查：只有正牌店長 (RM) 或是 老闆 (ADMIN) 可以提拔人
		if (operator.getRole() != StaffRole.REGION_MANAGER && operator.getRole() != StaffRole.ADMIN) {
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// 2. 找出目標員工
		Staff targetStaff = staffDao.findById(targetId).orElse(null);
		if (targetStaff == null) {
			return new StaffSearchRes(ReplyMessage.STAFF_ID_NOT_FOUND.getCode(), ReplyMessage.STAFF_ID_NOT_FOUND.getMessage());
		}

		// 3. 區域檢查：RM 只能提拔自己分店的人（ADMIN 無區域限制）
		if (operator.getRole() == StaffRole.REGION_MANAGER && 
			targetStaff.getGlobalAreaId() != operator.getGlobalAreaId()) {
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// 4. 狀態檢查：只有目前的 STAFF 才能晉升為 MA
		if (targetStaff.getRole() != StaffRole.STAFF) {
			return new StaffSearchRes(ReplyMessage.PROMOTE_TARGET_ERROR.getCode(), //
					ReplyMessage.PROMOTE_TARGET_ERROR.getMessage());
		}

		// 5. 執行晉升邏輯
		// (1) 變更角色 Enum -> 這會連動變更資料庫的 role 欄位
		targetStaff.setRole(StaffRole.MANAGER_AGENT);

		// (2) 手動變更帳號前綴 (例：ST0168 -> MA0168)
		// 這裡利用 replace 將第一個遇到的 ST 換成 MA
		String oldAccount = targetStaff.getAccount();
//		String newAccount = oldAccount.replace("ST", "MA");//只替換第一個出現的 "ST"
		String newAccount = "MA" + oldAccount.substring(2);//直接加上 "MA"，然後把舊帳號從第 2 個字元開始擷取
		targetStaff.setAccount(newAccount);

		// 6. 存檔：JPA save 會執行 UPDATE
		staffDao.save(targetStaff);

		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), List.of(targetStaff));
	}
	/*
	 * =============================================================================
	 * ============ ⬇️⬇️⬇️ 以下是私有輔助方法 (工具箱) ⬇️⬇️⬇️
	 * =============================================================================
	 */

	/*
	 * 【帳號自動產生器】暫時先用這個方法 規則：角色前綴 + 四位流水號 範例：RM0001、ST0042
	 * 
	 */
	private String generateAccount(StaffRole targetRole) {

		// 決定前綴是 "RM" 還是 "ST"
		// 根據角色決定前綴
				String prefix = "ST"; // 預設給 ST
				if (targetRole == StaffRole.REGION_MANAGER) {
					prefix = "RM";
				} else if (targetRole == StaffRole.MANAGER_AGENT) {
					prefix = "MA"; // 判斷如果是副店長，前綴給 MA
				}

		// 去資料庫問這個角色目前最後一個帳號（例如 "RM0015"）
		String lastAccount = staffDao.findLastAccountByRole(targetRole.name());

		int nextNumber = 1; // 如果資料庫是空的，就從 1 號開始，讓他當 001 號元老！

		if (StringUtils.hasText(lastAccount)) {
			// 把前綴切掉，只留數字的部分（"RM0015" → "0015"）
			String numberPart = lastAccount.substring(prefix.length());
			// 轉成整數再 +1（15 → 16），下一位新人的號碼
			nextNumber = Integer.parseInt(numberPart) + 1;
		}

		// %04d ：十進位數字，寬度為四位，不足的在左邊補零
		// 例如 16 → "0016"，讓帳號固定有四位數
		return prefix + String.format("%04d", nextNumber);
	}

	// 工具 2：判斷「新增帳號」的權限
	private boolean hasRegisterPermission(Staff operator, StaffRole targetRole, int targetAreaId) {
		StaffRole operatorRole = operator.getRole();

		if (operatorRole == StaffRole.ADMIN) {
			return targetRole == StaffRole.REGION_MANAGER;
		}
		if (operatorRole == StaffRole.REGION_MANAGER) {
			return targetRole == StaffRole.STAFF && targetAreaId == operator.getGlobalAreaId();
		}
		return false;
	}

	// 工具 3：判斷「對別人動手 (改密碼/停權)」的權限 (共用邏輯！)
	private boolean hasOperatePermission(Staff operator, Staff targetStaff) {

		// 規則 1：絕對不能對自己動手
		if (operator.getId() == targetStaff.getId()) {
			return false;
		}

		StaffRole operatorRole = operator.getRole();
		StaffRole targetRole = targetStaff.getRole();

		// 規則 2：ADMIN 只能動 RM
		if (operatorRole == StaffRole.ADMIN) {
			return targetRole == StaffRole.REGION_MANAGER;
		}

		// 規則 3：RM 可以動自己區域的 ST「以及」MA (副店長)
				if (operatorRole == StaffRole.REGION_MANAGER) {
					boolean isTargetValid = (targetRole == StaffRole.STAFF || targetRole == StaffRole.MANAGER_AGENT);
					return isTargetValid && targetStaff.getGlobalAreaId() == operator.getGlobalAreaId();
				}

		// ST 沒權限
		return false;
	}
}