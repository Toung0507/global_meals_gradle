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
import com.example.global_meals_gradle.req.RegisterStaffReq;
import com.example.global_meals_gradle.res.StaffSearchRes;


@Service
public class StaffService {

	/* =====================================================
	 * 	 角色（怕我忘記）：
	 *   ADMIN          = 老闆（不能控制自己）
	 *   REGION_MANAGER = 分店長（縮寫 RM，管一個分店）
	 *   STAFF          = 普通員工（縮寫 ST，負責被管的那位）
	 * ===================================================== */

	@Autowired
	private StaffDao staffDao;

	// BCryptPasswordEncoder：密碼加密器
	// 把密碼丟進去，出來的是一串看不懂的亂碼
	// 即使資料庫被偷也看不到明文密碼
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


	/* =====================================================
	 *  【帳號自動產生器】暫時先用這個方法
	 *  規則：角色前綴 + 四位流水號
	 *  範例：RM0001、ST0042
	 * ===================================================== */
	private String generateAccount(StaffRole targetRole) {

		// 決定前綴是 "RM" 還是 "ST"
		String prefix = (targetRole == StaffRole.REGION_MANAGER) ? "RM" : "ST";

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


	/* =====================================================
	 *  新增RM、ST:ADMIN 建 RM、RM 建自己區的 ST
	 *
	 *     注意！參數從 StaffRole role 改成 Staff operator
	 *     因為我們不只需要登入者的「角色」，
	 *     還需要他的「區域 ID」（RM 建 ST 時要比對區域），
	 *     所以直接傳整個 Staff 物件比較方便！
	 *     Controller 那邊記得改成傳 Session 裡的 Staff 物件進來！
	 * ===================================================== */
	@Transactional(rollbackFor = Exception.class)
	public StaffSearchRes register(RegisterStaffReq req, Staff operator) {

		// ===  基本欄位檢查，空的通通報錯 ===
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
		}

		// RM 只能建自己區域的 ST
		if (creatorRole == StaffRole.REGION_MANAGER) {
			// RM 不能建 ADMIN 或另一個 RM，只能建 ST
			if (targetRole != StaffRole.STAFF) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
			// 跨區建人？不行！你只管你自己的分店
			// req.getGlobalAreaId() 是要建立的新員工的區域
			// operator.getGlobalAreaId() 是登入的 RM 的區域
			// 兩個不一樣 = 想跨區 = 打回去
			if (req.getGlobalAreaId() != operator.getGlobalAreaId()) {
				return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
		}

		// ST - 根本沒有建帳號的資格，請去顧台就好
		if (creatorRole == StaffRole.STAFF) {
			return new StaffSearchRes(ReplyMessage.OPERATE_ERROR.getCode(), //
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// === 檢查自動生成的帳號有沒有撞號 ===
		// 理論上流水號不會重複，但防一下比較安心
		if (staffDao.findByAccount(autoAccount) != null) {
			return new StaffSearchRes(ReplyMessage.REPEAT_ERROR.getCode(), //帳號重複
					ReplyMessage.REPEAT_ERROR.getMessage());
		}

		// === 呼叫 DAO，把資料真正存進資料庫 ===
		staffDao.insert(
				req.getName(),
				autoAccount,                       // 使用自動生成的帳號，不用前端傳
				encoder.encode(req.getPassword()), // 密碼加密再存！裸密碼直接存是大忌
				targetRole.name(),                 // Enum 轉回字串給 SQL 用（例："STAFF"）
				req.getGlobalAreaId()              // 新員工屬於哪個分店
		);

		// === 這樣成功！回傳帳號資訊讓操作者知道 ===
			return new StaffSearchRes(ReplyMessage.SUCCESS.getCode(), //
					ReplyMessage.SUCCESS.getMessage() + autoAccount + req.getName());
	}


	/* =====================================================
	 *  【修改密碼】幫某個員工改密碼（新增功能）
	 *
	 *  規則和新增員工一樣：
	 *  - ADMIN 只能改 RM 的密碼
	 *  - RM 只能改自己區域 ST 的密碼
	 *  - 不能改自己的密碼（請走另一個介面）
	 * ===================================================== */
	@Transactional
	public StaffSearchRes changePassword(int targetId, String newPassword, Staff operator) {

		// 新密碼不可以是空的
		if (!StringUtils.hasText(newPassword)) {
			return new StaffSearchRes(ReplyMessage.NEW_PASSWORD_ERROR.getCode(), //
					ReplyMessage.NEW_PASSWORD_ERROR.getMessage());
		}

		// 去資料庫找要改密碼的目標員工
		// orElse(null)：找得到就給你 Staff 物件；找不到就給你 null（等於空手而回）
		// 就像你去找人，有找到就帶他過來，找不到就帶著空氣回來
		Staff targetStaff = staffDao.findById(targetId).orElse(null);

		if (targetStaff == null) {
			return new StaffSearchRes(ReplyMessage.STAFF_ID_NOT_FOUND.getCode(), //
					ReplyMessage.STAFF_ID_NOT_FOUND.getMessage());
		}

		// 不能改自己的密碼（要改自己的密碼，請走「修改個人密碼」的介面）
		if (targetStaff.getId() == operator.getId()) {
			return new StaffSearchRes(400, "不能用這個介面改自己的密碼喔！");
		}

		// 取得操作者和被操作者的角色
		StaffRole operatorRole = operator.getRole();
		StaffRole targetRole   = targetStaff.getRole();

		// ADMIN 只能改 RM 的密碼
		if (operatorRole == StaffRole.ADMIN) {
			if (targetRole != StaffRole.REGION_MANAGER) {
				return new StaffSearchRes(403, "老闆只能修改分店長的密碼！");
			}
		}
		// RM 只能改自己區域 ST 的密碼
		else if (operatorRole == StaffRole.REGION_MANAGER) {
			if (targetRole != StaffRole.STAFF) {
				return new StaffSearchRes(403, "分店長只能修改自己店員工的密碼！");
			}
			// 跨區一樣不行
			if (targetStaff.getGlobalAreaId() != operator.getGlobalAreaId()) {
				return new StaffSearchRes(403, "不能改其他區域員工的密碼，管好自己的店就好！");
			}
		}
		// ST 完全沒有改密碼的權限
		else {
			return new StaffSearchRes(403, "你是員工，沒有修改其他人密碼的權限！");
		}

		// 把新密碼加密後，用 save() 更新進資料庫
		// save() 是 JPA 的內建方法，它會自動辨識這個物件有 ID → 執行 UPDATE 而不是 INSERT
		targetStaff.setPassword(encoder.encode(newPassword));
		staffDao.save(targetStaff);

		return new StaffSearchRes(200, targetStaff.getName() + " 的密碼已成功修改！記得告訴他新密碼喔 😄");
	}


	/* =====================================================
	 *  【登入】帳密對了，把 Staff 物件丟回去讓 Controller 存 Session
	 *
	 *     這個方法回傳 Staff 物件（不是 StaffSearchRes）
	 *     因為 Controller 需要拿到這個物件存進 Session，
	 *     有錯誤就 throw RuntimeException 讓外面的人知道
	 * ===================================================== */
	public Staff login(String account, String password) {

		if (!StringUtils.hasText(account)) {
			throw new RuntimeException("帳號不可為空！");
		}
		if (!StringUtils.hasText(password)) {
			throw new RuntimeException("密碼不可為空！");
		}

		// 去資料庫用帳號找人（你手寫的那個 SQL）
		Staff staff = staffDao.findByAccount(account);

		// 帳號不存在的話，staffDao 回傳 null
		if (staff == null) {
			throw new RuntimeException("帳號不存在，是不是打錯了？");
		}

		// 比對密碼：
		// 密碼是加密儲存的，沒辦法「解密」再比對
		// encoder.matches(你輸入的明文, 資料庫裡的密文) 會幫你做比對
		// 回傳 true = 密碼正確；false = 打錯了
		if (!encoder.matches(password, staff.getPassword())) {
			throw new RuntimeException("密碼錯誤，再想想看？");
		}

		// 帳號被停權了（is_status = false）？不讓進！
		if (!staff.isStatus()) {
			throw new RuntimeException("此帳號已被停權，請聯絡管理員處理！");
		}

		// 全部通過！把 Staff 物件回傳，Controller 拿到後存進 Session
		return staff;
	}


	/* =====================================================
	 *  【查詢員工清單】
	 *  ADMIN → 查所有 RM（全國分店長名單）
	 *  RM    → 查自己區域底下的 ST
	 *
	 *  ⚠️ 原本多餘的 getAllRM() 和 getMyStaffList() 拿掉了，
	 *     兩個功能整合進這裡，一個方法搞定兩種角色，不用寫兩次！
	 * ===================================================== */
	public StaffSearchRes getStaffList(Staff adminOrRm) {

		// 沒登入（null）或是 ST 身分，不准查！
		if (adminOrRm == null || adminOrRm.getRole() == StaffRole.STAFF) {
			return new StaffSearchRes(403, "你沒有查詢員工清單的權限！");
		}

		// ADMIN → 查所有 RM
		if (adminOrRm.getRole() == StaffRole.ADMIN) {
			List<Staff> list = staffDao.getAllRM();
			return new StaffSearchRes(200, "查詢成功（老闆視角：所有分店長）", list);
		}

		// RM → 只查自己那個區域的 ST
		// getGlobalAreaId() 拿到的是這個 RM 的區域 ID，確保他不能偷看別的分店
		if (adminOrRm.getRole() == StaffRole.REGION_MANAGER) {
			List<Staff> list = staffDao.getSTListById(adminOrRm.getGlobalAreaId());
			return new StaffSearchRes(200, "查詢成功（分店長視角：本店員工）", list);
		}

		// 走到這裡代表角色很奇怪，防呆用的
		return new StaffSearchRes(400, "無效角色，查詢失敗，請聯絡工程師！");
	}


	/* =====================================================
	 *  【停權 / 復權】帳號開關
	 *  - ADMIN 操作 RM
	 *  - RM 操作自己區域的 ST
	 *  - 不能對自己動手
	 *
	 *  ⚠️ 原本有兩個同名 updateStaff() 導致編譯錯誤！
	 *     這裡改名叫 updateStatus，比較清楚這個方法在做什麼 😊
	 * ===================================================== */
	@Transactional
	public StaffSearchRes updateStatus(int targetId, boolean newStatus, Staff operator) {

		// orElse(null)：去資料庫找這個 id 的員工
		// orElse 的意思是「找得到就給我，找不到就用括號裡的替代品（這裡是 null）」
		// 就像你去倉庫找某個編號的商品，有就拿出來，沒有就帶著空氣回來
		Staff targetStaff = staffDao.findById(targetId).orElse(null);

		if (targetStaff == null) {
			return new StaffSearchRes(404, "找不到這個員工，確定 ID 正確嗎？");
		}

		// 不能自己停用自己，不然你要怎麼登入回來解除？🔒
		if (targetStaff.getId() == operator.getId()) {
			return new StaffSearchRes(400, "不能修改自己的帳號狀態，這樣會把自己鎖在門外的！");
		}

		// 取得操作者和被操作者的角色（之後要做權限比對）
		StaffRole operatorRole = operator.getRole();    // 操作者（登入的人）的身分
		StaffRole targetRole   = targetStaff.getRole(); // 被操作者（要改狀態的人）的身分

		// 【ADMIN】只能動 RM，ST 的事讓 RM 處理
		if (operatorRole == StaffRole.ADMIN) {
			if (targetRole != StaffRole.REGION_MANAGER) {
				return new StaffSearchRes(403, "老闆您只能操作分店長，員工的事交給 RM 處理！");
			}
		}
		// 【RM】只能動自己區域的 ST
		else if (operatorRole == StaffRole.REGION_MANAGER) {
			if (targetRole != StaffRole.STAFF) {
				return new StaffSearchRes(403, "分店長只能操作普通員工，越權了！");
			}
			// 跨區管太寬了，你又不是他老闆！
			if (targetStaff.getGlobalAreaId() != operator.getGlobalAreaId()) {
				return new StaffSearchRes(403, "不能操作其他區域的員工，管好自己的店！");
			}
		}
		// 【ST】沒有任何操作權限
		else {
			return new StaffSearchRes(403, "你是員工，沒有操作帳號狀態的權限！");
		}

		// 全部通過，更新資料庫的狀態
		staffDao.updateStatus(targetId, newStatus);

		// newStatus 是 true → 復權；false → 停權
		String action = newStatus ? "復權" : "停權";
		return new StaffSearchRes(200, "操作成功：" + targetStaff.getName() + " 已" + action + "！");
	}
}