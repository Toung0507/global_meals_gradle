package com.example.global_meals_gradle.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.global_meals_gradle.constants.StaffRole;
import com.example.global_meals_gradle.dao.StaffDao;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.RegisterStaffReq;
import com.example.global_meals_gradle.res.StaffSearchRes;

import jakarta.transaction.Transactional;

@Service
public class StaffService {

	/* ADMIN(老闆), REGION_MANAGER(分店長，縮寫 : RM), STAFF(員工，縮寫 : ST); */
	// ADMIN新增RM / RM新增ST(is_status 預設 true)

	@Autowired
	private StaffDao staffDao;

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/*------格式化編碼------*/
	// 自動生成帳號的邏輯
    private String generateAccount(StaffRole targetRole) {
        String prefix = (targetRole == StaffRole.REGION_MANAGER) ? "RM" : "ST";
        
        // 1. 從資料庫抓出該角色目前最後一個帳號 (例如抓到 "RM0015")
        String lastAccount = staffDao.findLastAccountByRole(targetRole.name());
        
        int nextNumber = 1; // 預設從 1 開始
        
        if (StringUtils.hasText(lastAccount)) {
            // 2. 把前綴去掉，剩下數字 (例如 "RM0015" -> "0015")
            String numberPart = lastAccount.substring(prefix.length());
            // 3. 轉成數字並 +1 (例如 15 -> 16)
            nextNumber = Integer.parseInt(numberPart) + 1;
        }
        
        // 4. 格式化回字串，不足四位補 0 (例如 16 -> "0016")
        // %04d 代表：十進位數字，寬度為 4，不足左邊補 0
        return prefix + String.format("%04d", nextNumber);
    }
    
	// 註冊/建立ST的核心邏輯
	@Transactional
	public String register(RegisterStaffReq req, Staff adminOrRm) {

		if (!StringUtils.hasText(req.getAccount())) {
			throw new RuntimeException("帳號不可為空");
		}
		if (!StringUtils.hasText(req.getPassword())) {
			throw new RuntimeException("密碼不可為空");
		}
		if (!StringUtils.hasText(req.getRole())) {
			throw new RuntimeException("角色不可為空");
		}
		if (!StringUtils.hasText(req.getName())) {
			throw new RuntimeException("姓名不可為空");
		}

		// 1. 權限檢查：建立者是誰？取得建立者的角色 (它是 StaffRole 型別)
		// 1-2. 他是Enum沒辦法直接強制轉換成String，AI回復用Enum去做
		StaffRole creatorRole = adminOrRm.getRole(); // 目前登入者的身分(Enum)

		// 2. 將前端傳來的 String 轉成 StaffRole Enum
		// valueOf 會把 "STAFF" 轉成 StaffRole.STAFF，若字串對不起來會報錯
		// .toUpperCase()使用預設區域設定的規則，將此字串中的所有字元轉換為大寫，用來預防前端傳小寫。
		StaffRole targetRole;
		try {
			targetRole = StaffRole.valueOf(req.getRole());
		} catch (Exception e) {
			throw new RuntimeException("角色格式錯誤");
		}
		// 自動生成帳號
		String autoAccount = generateAccount(targetRole);
		
		// 3. 權限檢查 (用 Enum 比較直接一點，不需要寫死字串)
		// ADMIN 的權限：只能建 RM

		if (creatorRole == StaffRole.ADMIN) {
			if (targetRole != StaffRole.REGION_MANAGER) {
				throw new RuntimeException("權限不足：只有老闆可以建立分店長");
			} // -->創建者是ADMIN進入，選擇創建目標不是RM-->throw new RuntimeException
		}
		// ADMIN的檢查：雖然ADMIN權限最大，但不允許再建另一個 ADMIN
		if (creatorRole == StaffRole.ADMIN) {
			if (targetRole == StaffRole.ADMIN) {
				throw new RuntimeException("系統限制：為了安全性，不允許透過此介面建立第二個老闆帳號");
			}
		}

		// RM 的權限：
		// RM只能建ST
		// RM建ST時，區域必須一致然後只能建ST，不能建ADMIN或別的RM及ST
		if (creatorRole == StaffRole.REGION_MANAGER) {
			if (req.getGlobalAreaId() != adminOrRm.getGlobalAreaId()) {
				throw new RuntimeException("權限不足：您只能建立自己管轄以下的員工");
			}
			// 額外規則：RM 不能建 ADMIN
			if (targetRole == StaffRole.ADMIN) {
				throw new RuntimeException("權限不足：分店長不可建立老闆帳號");
			}
			// RM 只能建 ST，不能建ADMIN或別的RM
			if (targetRole != StaffRole.STAFF) {
				throw new RuntimeException("權限不足：分店長僅能建立普通員工帳號");
			}
		}

		// ST 的權限：ST 誰都不能建
		if (creatorRole == StaffRole.STAFF) {
			throw new RuntimeException("權限不足：普通員工不具備建立帳號的權限");
		}

		// 4. 檢查帳號是否重複
		if (staffDao.findByAccount(autoAccount) != null) {
			throw new RuntimeException("系統生成的帳號重複，請重試");
		}

		// 5. 呼叫 INSERT SQL
		// 我在這裡直接傳入 Req 的資料
		staffDao.insert(//
				req.getName(), //
				autoAccount, //原本是req.getAccount()，但現在帳號是自動產生的所以改用 autoAccount 使用自動生成的帳號
				encoder.encode(req.getPassword()), // 加密
				targetRole.name(), // 使用 .name() 轉回 "STAFF" 字串給 SQL 使用
									// 因為沒辦法強轉Enum所以上面先用 targetRole(目標) = StaffRole.valueOf(req.getRole());
				req.getGlobalAreaId());
		return "建立成功！帳號為：" + autoAccount + " (姓名: " + req.getName() + ")";
	}
	
	/*------登入------*/
	public Staff login(String account, String password) {

		if (!StringUtils.hasText(account)) {
			throw new RuntimeException("帳號不可為空");
		}

		if (!StringUtils.hasText(password)) {
			throw new RuntimeException("密碼不可為空");
		}

		// 1. 呼叫你手寫的 SQL：findByAccount
		Staff staff = staffDao.findByAccount(account);

		// 2. 檢查是否有這個人
		if (staff == null) {
			throw new RuntimeException("帳號不存在");
		}

		// 3. 比對密碼 ->AtmService boolean resul = encoder.matches(password, atm.getPassword());
		if (!encoder.matches(password, staff.getPassword())) {
			throw new RuntimeException("密碼錯誤");
		}

		// 4. 檢查是否被停權 
		if (!staff.isStatus()) {
			throw new RuntimeException("此帳號已被停權，請洽管理員");
		}

		// 5. 通過驗證，回傳 ST 資料（後續丟給 Controller 存進 Session）
		return staff;
	}

	/*------ 查詢功能 ------*/

	// 1. ADMIN 查所有RM
	public List<Staff> getAllRM(Staff admin) {
		// 安全檢查：只有 ADMIN 才能看所有 RM
		if (admin.getRole() != StaffRole.ADMIN) {
			throw new RuntimeException("權限不足：只有老闆可以查看分店長清單");
		}

		return staffDao.getAllRM();
	}

	// 2. RM查自己轄下的ST
	public List<Staff> getMyStaffList(Staff rm) {
		// 安全檢查：只有 RM 才能看 (或者 ADMIN 也可以看，看你的業務需求)
		// 這裡示範 RM 只能看自己這一區的
		if (rm.getRole() != StaffRole.REGION_MANAGER) {
			throw new RuntimeException("權限不足：非分店長身分無法查詢員工清單");
		}

		// 從登入的 RM 物件中取得他的 globalAreaId，確保他不能查別人的區
		return staffDao.getSTListById(rm.getGlobalAreaId());
	}

	// 查詢 ST 清單 
	public StaffSearchRes getStaffList(Staff adminOrRm) {
		// 1. 沒登入或是 ST ，不准查
		if (adminOrRm == null || adminOrRm.getRole() == StaffRole.STAFF) {
			throw new RuntimeException("權限不足，無法查看名單");
		}
		
		List<Staff> list;

		// 2. ADMIN 查所有 RM:如果是ADMIN的話可查所有RM
		if (adminOrRm.getRole() == StaffRole.ADMIN) {
			list = staffDao.getAllRM();
			return new StaffSearchRes(200, "查詢成功 (管理員)", list);
		}

		// 3. RM 查所屬所有 ST :如果是RM可查詢你自己區域的 ST
		if (adminOrRm.getRole() == StaffRole.REGION_MANAGER) {
			list = staffDao.getSTListById(adminOrRm.getGlobalAreaId());
			return new StaffSearchRes(200, "查詢成功 (分店長)", list);
		}

		throw new RuntimeException("無效角色，無法查詢員工清單");
	}

	/*------ 停權/復權 ------*/
	@Transactional
	public String updateStaff(int targetId, boolean newStatus, Staff operator) {

		// 1. 查人:資料庫找準備要被fire的人(我們打工人好可憐)
		Staff targetStaff = staffDao.findById(targetId).orElse(null);

		if (targetStaff == null) {
			throw new RuntimeException("找不到該員工");
		}

		// 2. 不口已停權自己
		if (targetStaff.getId() == operator.getId()) {
			throw new RuntimeException("不可修改自己的帳號狀態");
		}

		// 3. 誰操作、誰被操
		StaffRole operatorRole = operator.getRole();
		StaffRole targetRole = targetStaff.getRole();

		// 4. 權限檢查

		// ADMIN
		if (operatorRole == StaffRole.ADMIN) {

			// ADMIN 只能操作 RM 不能操作 ST
			if (targetRole != StaffRole.REGION_MANAGER) {
				throw new RuntimeException("老闆您只能操作分店長");
			}
		}

		//  RM
		else if (operatorRole == StaffRole.REGION_MANAGER) {

			// 只能操作 ST
			if (targetRole != StaffRole.STAFF) {
				throw new RuntimeException("分店長只能操作所屬分店員工");
			}

			// 不能跨區
			if (targetStaff.getGlobalAreaId() != operator.getGlobalAreaId()) {
				throw new RuntimeException("不能操作其他區域的員工");
			}
		}

		// ST
		else {
			throw new RuntimeException("你沒有權限操作");
		}

		// 5. 更新
		staffDao.updateStatus(targetId, newStatus);

		// 6. 回傳訊息
		String action;
		if (newStatus == true) {
			action = "復權";
		} else {
			action = "停權";
		}

		return "操作成功：" + targetStaff.getName() + " 已" + action;
	}
}
