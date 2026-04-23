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
import com.example.global_meals_gradle.req.UpdateStaffStatusReq;
import com.example.global_meals_gradle.res.StaffSearchRes;

@Service
public class StaffService {

	/*
	 * ===================================================== 角色（怕我忘記）： ADMIN =
	 * 老闆（不能控制自己） REGION_MANAGER = 分店長（縮寫 RM，管一個分店） STAFF = 普通員工（縮寫 ST，負責被管的那位）
	 * =====================================================
	 */

	@Autowired
	private StaffDao staffDao;

	// BCryptPasswordEncoder：密碼加密器
	// 把密碼丟進去，出來的是一串看不懂的亂碼
	// 即使資料庫被偷也看不到明文密碼
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/*
	 * ===================================================== 【帳號自動產生器】暫時先用這個方法
	 * 規則：角色前綴 + 四位流水號 範例：RM0001、ST0042
	 * =====================================================
	 */
	private String generateAccount(StaffRole targetRole) {

		String prefix;
		if (targetRole == StaffRole.REGION_MANAGER) prefix = "RM";
		else if (targetRole == StaffRole.MANAGER_AGENT) prefix = "MA";
		else prefix = "ST";

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

	/*
	 * ===================================================== 新增RM、ST:ADMIN 建 RM、RM
	 * 建自己區的 ST
	 *
	 * 注意！參數從 StaffRole role 改成 Staff operator 因為我們不只需要登入者的「角色」， 還需要他的「區域 ID」（RM 建
	 * ST 時要比對區域）， 所以直接傳整個 Staff 物件比較方便！ Controller 那邊記得改成傳 Session 裡的 Staff 物件進來！
	 * =====================================================
	 */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes register(RegisterStaffReq req, Staff operator) {

		// === 基本欄位檢查，空的通通報錯 ===
		if (!StringUtils.hasText(req.getName())) {
			return new StaffSearchRes(ReplyMessage.NAME_ERROR.getCode(), //
					ReplyMessage.NAME_ERROR.getMessage());
		}
		if (!StringUtils.hasText(req.getPassword())) {
			return new StaffSearchRes(ReplyMessage.PASSWORD_ERROR.getCode(), //
					ReplyMessage.PASSWORD_ERROR.getMessage());
		}
		if (!StringUtils.hasText(req.getRole())) {
			return new StaffSearchRes(ReplyMessage.ROLE_ERROR.getCode(), //
					ReplyMessage.ROLE_ERROR.getMessage());
		}

		// === 取得操作者（登入者）的角色 ===
		// operator 是從 Session 抓出來的 Staff 物件
		// getRole() 會回傳他的身分（StaffRole 型別的 Enum）
		StaffRole creatorRole = operator.getRole();

		// === 把前端傳來的角色字串轉成 Enum ===
		// 前端送過來的是字串，例如 "STAFF"
		// StaffRole.valueOf("STAFF") 會把它變成 StaffRole.STAFF 這個 Enum 常數
		// toUpperCase() 是防呆設計，避免前端手殘傳小寫 "staff" 就壞掉
		StaffRole targetRole;
		try {
			targetRole = StaffRole.valueOf(req.getRole().toUpperCase());
		} catch (IllegalArgumentException e) {
			// valueOf 找不到對應的 Enum 時會丟 IllegalArgumentException
			// 在這裡接住，回傳一個友善的錯誤訊息，而不是讓整個程式直接爆炸
			return new StaffSearchRes(ReplyMessage.ROLE_ERROR.getCode(), //
					ReplyMessage.ROLE_ERROR.getMessage());
		}

		// === 讓系統自動幫新RM、ST產生帳號 ===
		// 不讓前端自己傳帳號，是為了統一格式，避免有人傳奇奇怪怪的帳號名稱
		String autoAccount = generateAccount(targetRole);

		// === 權限把關，你有資格建這個人嗎？===

		// ADMIN - 只能建 RM，想建 ST 或 ADMIN 都不行
		if (creatorRole == StaffRole.ADMIN) {
			if (targetRole != StaffRole.REGION_MANAGER) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
			// RM 只能建自己區域的 ST
		} else if (creatorRole == StaffRole.REGION_MANAGER) {
			// RM 不能建 ADMIN 或另一個 RM，只能建 ST
			if (targetRole != StaffRole.STAFF) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
			// req.getGlobalAreaId() 是要建立的新員工的區域
			// operator.getGlobalAreaId() 是登入的 RM 的區域
			// 兩個不一樣 = 想跨區建人 = 不行
			if (req.getGlobalAreaId() != operator.getGlobalAreaId()) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
		} else {
			// ST 沒有建帳號的權限
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// === 檢查自動生成的帳號有沒有撞號 ===
		// 理論上流水號不會重複，但防一下比較安心(最主要是防同時按)
		if (staffDao.findByAccount(autoAccount) != null) {
			return new StaffSearchRes(ReplyMessage.REPEAT_ERROR.getCode(), // 帳號重複
					ReplyMessage.REPEAT_ERROR.getMessage());
		}

		try {
			staffDao.insert(//
					req.getName(), //
					autoAccount, // 使用自動生成的帳號，不用前端傳
					encoder.encode(req.getPassword()), // 密碼加密再存！裸密碼直接存是大忌
					targetRole.name(), // Enum 轉回字串給 SQL 用（例："STAFF"）
					req.getGlobalAreaId()); // 新員工屬於哪個分店
		} catch (Exception e) {
			throw e;
		} // === 呼叫 DAO，把資料真正存進資料庫 ===

		// === 成供回傳帳號資訊讓操作者知道 ===
		// insert 用的是 native query，不會回傳物件
		// 所以 insert 完再用帳號查一次，把新建好的員工資料放進 Res 回傳
		// 前端從 staffList[0] 就可以看到新帳號的完整資料
		Staff newStaff = staffDao.findByAccount(autoAccount);
		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), List.of(newStaff));
	}

	/*
	 * ===================================================== 【修改密碼】幫某個員工改密碼
	 *
	 * 規則和新增員工一樣： - ADMIN 只能改 RM 的密碼 - RM 只能改自己區域 ST 的密碼 - 不能改自己的密碼
	 * =====================================================
	 */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes changePassword(int targetId, ChangePasswordReq req, Staff operator) {

		// 去資料庫找要改密碼的目標員工
		// orElse(null)：找得到就給你 Staff 物件；找不到就給你 null（等於空手而回）
		// JPA 的 findById 回傳的是 Optional<Staff>，不是直接給你 Staff
		// 就像你去找人，有找到就帶他過來，找不到就帶著空氣回來
		Staff targetStaff = staffDao.findById(targetId).orElse(null);

		if (targetStaff == null) {
			return new StaffSearchRes(ReplyMessage.STAFF_ID_NOT_FOUND.getCode(), //
					ReplyMessage.STAFF_ID_NOT_FOUND.getMessage());
		}

		// 不能改自己的密碼
		if (targetStaff.getId() == operator.getId()) {
			return new StaffSearchRes(ReplyMessage.SELF_OPERATE_ERROR.getCode(), //
					ReplyMessage.SELF_OPERATE_ERROR.getMessage());
		}

		// 取得操作者和被操作者的角色
		StaffRole operatorRole = operator.getRole();
		StaffRole targetRole = targetStaff.getRole();

		// ADMIN 只能改 RM 的密碼
		if (operatorRole == StaffRole.ADMIN) {
			if (targetRole != StaffRole.REGION_MANAGER) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
			// RM 只能改自己區域 ST 的密碼
		} else if (operatorRole == StaffRole.REGION_MANAGER) {
			if (targetRole != StaffRole.STAFF) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
			// 跨區一樣不行
			if (targetStaff.getGlobalAreaId() != operator.getGlobalAreaId()) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
			// ST 完全沒有改密碼的權限
		} else {
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
					ReplyMessage.OPERATE_ERROR.getMessage());
		}
		// save() 是 JPA 的內建方法:
		// 從資料庫撈出來的 targetStaff 本來就有 ID，所以這裡會執行 UPDATE
		// 但是有 ID → 執行 UPDATE；沒有 ID → 執行 INSERT
		// 把新密碼加密後，用 save() 更新進資料庫
		targetStaff.setPassword(encoder.encode(req.getNewPassword()));
		try {
			staffDao.save(targetStaff);
		} catch (Exception e) {
			throw e;
		}
		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}

	/*
	 * ===================================================== 【登入】 帳密對了，把 Staff
	 * 物件丟回去讓 Controller 存 Session 回傳 StaffSearchRes 不是 Staff : 因為 Service 統一回傳 Res
	 * 物件，Controller 再從 Res 裡把 Staff 取出來存進 Session 這樣 Controller 只要看 code
	 * 就知道成不成功，不需要 try-catch =====================================================
	 */
	public StaffSearchRes login(LoginStaffReq req) {

		// 去資料庫用帳號找人
		Staff staff = staffDao.findByAccount(req.getAccount());

		// 帳號不存在的話，staffDao 回傳 null
		if (staff == null) {
			return new StaffSearchRes(ReplyMessage.ACCOUNT_NOT_FOUND.getCode(), //
					ReplyMessage.ACCOUNT_NOT_FOUND.getMessage());
		}

		// 比對密碼：
		// 密碼是加密儲存的，不能「解密」，只能「比對」
		// encoder.matches(輸入的明文, 資料庫的密文) 回傳 true = 密碼正確
		if (!encoder.matches(req.getPassword(), staff.getPassword())) {
			return new StaffSearchRes(ReplyMessage.PASSWORD_MISMATCH.getCode(), //
					ReplyMessage.PASSWORD_MISMATCH.getMessage());
		}

		// 帳號被停權了（is_status = false）
		if (!staff.isStatus()) {
			return new StaffSearchRes(ReplyMessage.ACCOUNT_DISABLED.getCode(), //
					ReplyMessage.ACCOUNT_DISABLED.getMessage());
		}

		// 成功！把 Staff 放進 List 回傳，Controller 取出後存進 Session
		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), List.of(staff));
	}

	/*
	 * ===================================================== 【查詢員工清單】 - ADMIN → 查所有
	 * RM - RM → 查自己區域的 ST - ST → 沒有查詢權限
	 * =====================================================
	 */
	public StaffSearchRes getStaffList(Staff operator) {

		StaffRole role = operator.getRole();

		if (role == StaffRole.ADMIN) {
			List<Staff> list = staffDao.getAllRM();
			return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
					ReplyMessage.SUCCESS.getMessage(), list);
		}

		if (role == StaffRole.REGION_MANAGER) {
			List<Staff> list = staffDao.getSTListById(operator.getGlobalAreaId());
			return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
					ReplyMessage.SUCCESS.getMessage(), list);
		}

		// ST 無法查詢
		return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
				ReplyMessage.OPERATE_ERROR.getMessage());
	}

	/*
	 * ===================================================== 【停權 / 復權】帳號開關 - ADMIN -
	 * ADMIN 操作 RM - RM 操作自己區域的 ST - 不能對自己動手
	 * =====================================================
	 */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes updateStatus(int targetId, UpdateStaffStatusReq req, Staff operator) {

		// orElse(null)：去資料庫找這個 id 的員工
		// orElse 的意思是「找得到就給我，找不到就用括號裡的替代品（這裡是 null）」
		// 就像你去倉庫找某個編號的商品，有就拿出來，沒有就帶著空氣回來
		Staff targetStaff = staffDao.findById(targetId).orElse(null);

		if (targetStaff == null) {
			return new StaffSearchRes(ReplyMessage.STAFF_ID_NOT_FOUND.getCode(), //
					ReplyMessage.STAFF_ID_NOT_FOUND.getMessage());
		}

		// 不能自己停用自己，不然你要怎麼登入回來解除
		if (targetStaff.getId() == operator.getId()) {
			return new StaffSearchRes(ReplyMessage.SELF_OPERATE_ERROR.getCode(), //
					ReplyMessage.SELF_OPERATE_ERROR.getMessage());
		}

		// 取得操作者和被操作者的角色（之後要做權限比對）
		StaffRole operatorRole = operator.getRole(); // 操作者（登入的人）的身分
		StaffRole targetRole = targetStaff.getRole(); // 被操作者（要改狀態的人）的身分

		// 【ADMIN】只能動 RM，ST 的事讓 RM 處理
		if (operatorRole == StaffRole.ADMIN) {
			if (targetRole != StaffRole.REGION_MANAGER) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
		}
		// 【RM】只能動自己區域的 ST
		else if (operatorRole == StaffRole.REGION_MANAGER) {
			if (targetRole != StaffRole.STAFF) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
			// 跨區管太寬了，你又不是他老闆！
			if (targetStaff.getGlobalAreaId() != operator.getGlobalAreaId()) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
		} else {
			// ST 沒有任何操作權限
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		try {
			staffDao.updateStatus(targetId, req.isNewStatus());
		} catch (Exception e) {
			throw e;
		}
		return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}
}