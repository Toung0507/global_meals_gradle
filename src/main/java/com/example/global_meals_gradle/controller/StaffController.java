package com.example.global_meals_gradle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.ResetStaffPasswordReq;
import com.example.global_meals_gradle.req.LoginStaffReq;
import com.example.global_meals_gradle.req.RegisterStaffReq;
import com.example.global_meals_gradle.req.UpdateStaffPasswordReq;
import com.example.global_meals_gradle.req.UpdateStaffStatusReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.StaffSearchRes;
import com.example.global_meals_gradle.service.StaffService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

// @RestController：告訴 Spring 這個類別是個 Controller
//                  每個方法的回傳值直接轉成 JSON 丟給前端
// @CrossOrigin：允許前端（Angular 在 4200 port）跨域呼叫這個 API
//               allowCredentials = "true" 讓 Cookie（Session）可以跨域傳送
@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/staff")
@Tag(name = "員工管理模組", description = "處理員工登入、權限管理與密碼變更業務")
public class StaffController {

	// Session 的 key 名稱，統一用常數，避免到處打字串打錯
	//宣告一個「不可變的常數」。把 Session 的鑰匙名稱統一寫在這裡
	public static final String SESSION_KEY = "loginStaff";

	@Autowired
	private StaffService staffService;

	// 工具方法：從 Session 取出目前登入的 Staff
	// HttpSession 是 Java 內建的 Session 物件，Spring 會自動注入
	private Staff getLoginStaff(HttpSession session) {
		return (Staff) session.getAttribute(SESSION_KEY);
	}

	// 工具方法：統一的「尚未登入」錯誤回傳
	private StaffSearchRes notLoginRes() {
		return new StaffSearchRes(ReplyMessage.NOT_LOGIN.getCode(), //
				ReplyMessage.NOT_LOGIN.getMessage());
	}


	/* =================================================================
	 *  POST /api/auth/login — 登入
	 *
	 *  @Valid：觸發 LoginStaffReq 裡的 @NotBlank 驗證
	 *           欄位不合格直接回傳 400，不會進到 Service
	 *  HttpSession session：Spring 自動注入 Session 物件
	 * ================================================================= */
	@PostMapping("/auth/login")//看ATM
	@Operation(summary = "員工登入", description = "驗證帳號密碼並建立 Session")
	public StaffSearchRes login(@Valid @RequestBody LoginStaffReq req, //
			@Parameter(hidden = true) HttpSession session) {

		StaffSearchRes res = staffService.login(req);
		
		// code == 200 代表登入成功，把 Staff 存進 Session
		// res.getStaffList().get(0)：Service 把 Staff 放在 List 的第一個位置
		if (res.getCode() == ReplyMessage.SUCCESS.getCode()) {
			session.setAttribute(SESSION_KEY, res.getStaffList().get(0));
			// 關鍵寫法：設定 5 秒後過期
						// 單位是「秒」，5 秒沒發請求，保全 (Interceptor) 就會把你擋下來
						session.setMaxInactiveInterval(86400);
		}

		return res;
	}


	/* =================================================================
	 *  POST /api/auth/logout — 登出
	 *	沒有 request_body ， 所以用 GetMapping
	 *  session.invalidate()：把這個 Session 整個清掉
	 *  下次要操作就要重新登入
	 * ================================================================= */
	@GetMapping("/auth/logout")
	@Operation(summary = "員工登出", description = "銷毀當前 Session")
	public BasicRes logout(HttpSession session) {
		session.invalidate();
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage());
	}


	/* =================================================================
	 *  POST /api/admin/staff — 新增員工（建立 RM 或 ST）
	 * ================================================================= */
	@PostMapping("/admin/staff")
	@Operation(summary = "新增員工", description = "建立新員工帳號 (需管理員權限)")
	public StaffSearchRes register(@Valid @RequestBody RegisterStaffReq req, //
            @Parameter(hidden = true) HttpSession session) {

		Staff operator = getLoginStaff(session);
		
		return staffService.register(req, operator);
	}


	/* =================================================================
	 *  GET /api/admin/staff — 查詢員工清單
	 *  ADMIN → 看所有 RM；RM → 看自己分店的員工
	 * ================================================================= */
	@GetMapping("/admin/staff")
	@Operation(summary = "查詢員工清單", description = "取得員工列表")
	public StaffSearchRes getStaffList(@Parameter(hidden = true) HttpSession session) {

		Staff operator = getLoginStaff(session);

		return staffService.getStaffList(operator);
	}


	/*	 *  {id} 與 @PathVariable int id：這兩個是配套的。
	 *  {id} 是網址上的佔位符（例如 /staff/5/status），@PathVariable 會精準地把網址上的那個 5 抓下來，變成 Java 的變數 id。
	 *  體現「對誰動手」在網址上 (id)，「動什麼手腳」在 Body 裡 (req)，「誰在動手」在 Session 裡 (operator)。
	 */
	
	/* =================================================================
	 *  PATCH /api/admin/staff/{id}/status — 停權 / 復權
	 *
	 *  @PathVariable int id：從 URL 路徑取出 {id}
	 *  例如呼叫 PATCH /api/admin/staff/5/status → id = 5
	 * ================================================================= */
	@PatchMapping("/admin/staff/{id}/status")
	@Operation(summary = "調整員工狀態", description = "停權或復權指定員工")
	public StaffSearchRes updateStatus(//
			@PathVariable("id") int id, //
			@Valid @RequestBody UpdateStaffStatusReq req, //
			@Parameter(hidden = true) HttpSession session) {

		Staff operator = getLoginStaff(session);
		
		return staffService.updateStatus(id, req, operator);
	}


	/* =================================================================
	 *  PATCH /api/admin/staff/{id}/password — 修改密碼
	 * ================================================================= */
	@PatchMapping("/admin/staff/{id}/password")
	@Operation(summary = "管理員修改員工密碼", description = "由管理員強制重置員工密碼")
	public StaffSearchRes changePassword(//
			@PathVariable("id") int id, //
			@Valid @RequestBody ResetStaffPasswordReq req, //
			@Parameter(hidden = true) HttpSession session) {

		Staff operator = getLoginStaff(session);
	
		return staffService.changePassword(id, req, operator);
	}
	// 在 StaffController.java 中新增
	/* =================================================================
	 * PATCH /api/staff/password — 員工自己修改密碼 (需驗證舊密碼)
	 * ================================================================= */
	@PatchMapping("/staff/password")
	@Operation(summary = "員工自行修改密碼", description = "員工驗證舊密碼後變更為新密碼")
	public StaffSearchRes selfChangePassword(//
	        @Valid @RequestBody UpdateStaffPasswordReq req, //
	        HttpSession session) {

	    Staff operator = getLoginStaff(session);
	    // 此處不需 PathVariable id，因為就是改「我」自己的密碼
	    return staffService.selfChangePassword(req, operator);
	}
	/* =================================================================
	 *  PATCH /api/admin/staff/{id}/promote — 晉升
	 * ================================================================= */
	@PatchMapping("/api/admin/staff/{id}/promote")
	@Operation(summary = "晉升員工", description = "將員工權限晉升為副店長或更高層級")
	public StaffSearchRes promoteToMA(@PathVariable("id") int id, //
			@Parameter(hidden = true) HttpSession session) {
		Staff operator = getLoginStaff(session);
        // 此處攔截器會處理登入檢查，不需再寫 if(operator == null)
		return staffService.promoteToManagerAgent(id, operator);
	}

}