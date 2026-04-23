package com.example.global_meals_gradle.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.StaffRole;
import com.example.global_meals_gradle.dao.BranchInventoryDao;
import com.example.global_meals_gradle.dao.GlobalAreaDao;
import com.example.global_meals_gradle.dao.OrdersDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.dao.RegionsDao;
import com.example.global_meals_gradle.entity.BranchInventory;
import com.example.global_meals_gradle.entity.GlobalArea;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.entity.Regions;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.MonthlyProductsSalesReq;
import com.example.global_meals_gradle.req.ProductCreateReq;
import com.example.global_meals_gradle.req.ProductUpdateReq;
import com.example.global_meals_gradle.res.AdminProductRes;
import com.example.global_meals_gradle.res.MonthlyProductsSalesRes;
import com.example.global_meals_gradle.vo.InventoryDetailVo;
import com.example.global_meals_gradle.vo.MonthlyProductsSalesVo;
import com.example.global_meals_gradle.vo.ProductAdminVo;

import jakarta.servlet.http.HttpSession;

@Service
public class ProductService {

	@Autowired
	private ProductsDao productsDao;

	@Autowired
	private BranchInventoryDao branchInventoryDao;

	@Autowired
	private GlobalAreaDao globalAreaDao;

	private static final String SESSION_KEY = "loginStaff";

	// 新增商品 ( 同時會新增每個分店庫存為 0 )
	@Transactional(rollbackFor = Exception.class)
	public AdminProductRes createProduct(ProductCreateReq req, MultipartFile file, HttpSession session) {
		// 1. 權限檢查
		AdminProductRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return authRes;

		AdminProductRes errorsRes = checkParam(req, file, false, null);
		if (errorsRes != null) {
			return errorsRes;
		}

		try {
			Products product = new Products();
			product.setName(req.getName());
			product.setCategory(req.getCategory());
			product.setDescription(req.getDescription());
			product.setFoodImg(file.getBytes());
			product.setActive(req.isActive());
			productsDao.save(product);

			// 2. 準備清單存放所有分店的庫存紀錄
			List<BranchInventory> inventoryList = new ArrayList<>();
			List<GlobalArea> allBranches = globalAreaDao.getAll();

			// 若沒有分店，不可新增商品
			if (allBranches.isEmpty()) {
				return new AdminProductRes(ReplyMessage.BRANCH_NOT_FOUND.getCode(), //
						ReplyMessage.BRANCH_NOT_FOUND.getMessage());
			}

			// 3. 迴圈初始化庫存
			for (GlobalArea area : allBranches) {
				BranchInventory inventory = new BranchInventory();
				inventory.setProductId(product.getId());
				inventory.setGlobalAreaId(area.getId());
				inventory.setBasePrice(BigDecimal.ZERO);
				inventory.setMaxOrderQuantity(0);
				inventory.setUpdatedAt(LocalDateTime.now());
				inventory.setVersion(1);

				// 將存好的紀錄加入清單，準備回傳給管理員檢查
				inventoryList.add(inventory);
			}

			// 使用 saveAll 存入
			branchInventoryDao.saveAll(inventoryList);

			// 2. 獲取分店 Map
			Map<Integer, String> branchMap = globalAreaDao.getBranchNameMap();

			// 3. 使用 Stream 將 Entity 轉為 VO
			List<InventoryDetailVo> inventoryVoList = inventoryList.stream()
					.map(inv -> convertToInventoryDetailVo(inv, branchMap)).collect(Collectors.toList());

			// 4. 回傳包含完整資訊的 AdminProductRes
			return new AdminProductRes(ReplyMessage.PRODUCT_CREATE_SUCCESS.getCode(), //
					ReplyMessage.PRODUCT_CREATE_SUCCESS.getMessage(), convertToAdminVo(product), inventoryVoList);

		} catch (IOException e) {
			// 圖片讀取失敗
			return new AdminProductRes(ReplyMessage.IMAGE_ERROR.getCode(), //
					ReplyMessage.IMAGE_ERROR.getMessage());
		} catch (Exception e) {
			// 其他任何預期外的錯誤
			return new AdminProductRes(ReplyMessage.SYSTEM_ERROR.getCode(), //
					ReplyMessage.SYSTEM_ERROR.getMessage() + e.getMessage());
		}

	}

	// 修改商品
	@Transactional(rollbackFor = Exception.class)
	public AdminProductRes updateProduct(ProductUpdateReq req, MultipartFile file, HttpSession session) {
		// 1. 權限檢查
		AdminProductRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return authRes;

		Products product = productsDao.findById(req.getId());
		if (product == null) {
			return new AdminProductRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(),
					ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}

		try {
			AdminProductRes errorsRes = checkParam(req, file, true, req.getId());
			if (errorsRes != null) {
				return errorsRes;
			}

			// 更新資訊
			product.setName(req.getName());
			product.setCategory(req.getCategory());
			product.setDescription(req.getDescription());
			product.setActive(req.isActive());

			// 如果有傳入新圖片，才更新圖片 (沒傳就不變)
			if (file != null && !file.isEmpty()) {
				product.setFoodImg(file.getBytes());
			}

			productsDao.save(product);
			return new AdminProductRes(ReplyMessage.PRODUCT_UPDATE_SUCCESS.getCode(), //
					ReplyMessage.PRODUCT_UPDATE_SUCCESS.getMessage(), convertToAdminVo(product), new ArrayList<>());

		} catch (IOException e) {
			return new AdminProductRes(ReplyMessage.IMAGE_ERROR.getCode(), //
					ReplyMessage.IMAGE_ERROR.getMessage());
		}
	}

	// 軟刪除商品
	@Transactional(rollbackFor = Exception.class)
	public AdminProductRes deleteProduct(int id, HttpSession session) {
		// 1. 權限檢查
		AdminProductRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return authRes;

		// 1. 先檢查商品是否存在
		Products product = productsDao.findById(id);
		if (product == null) {
			return new AdminProductRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(), //
					ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}

		try {
			// 2. 執行軟刪除
			int row = productsDao.softDeleteProduct(id);

			if (row > 0) {
				return new AdminProductRes(ReplyMessage.PRODUCT_DELETE_SUCCESS.getCode(), //
						ReplyMessage.PRODUCT_DELETE_SUCCESS.getMessage(), //
						convertToAdminVo(product), new ArrayList<>());
			} else {
				// 這邊代表雖然 ID 存在，但刪除動作因為某些原因 (如資料庫鎖定) 影響行數為 0
				String error = "Delete failed, no rows affected.";
				return new AdminProductRes(ReplyMessage.SYSTEM_ERROR.getCode(), //
						ReplyMessage.SYSTEM_ERROR.getMessage() + ": " + error);
			}

		} catch (Exception e) {
			// 3. 捕捉資料庫層級的所有異常 (包含連線中斷、SQL 語法錯誤等)
			return new AdminProductRes(ReplyMessage.SYSTEM_ERROR.getCode(), //
					ReplyMessage.SYSTEM_ERROR.getMessage() + ": " + e.getMessage());
		}
	}

	// 快速修正上下架
	@Transactional(rollbackFor = Exception.class)
	public AdminProductRes updateActiveStatus(int id, boolean active, HttpSession session) {
		// 管理者權限檢查
		AdminProductRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return authRes;

		Products product = productsDao.findById(id);
		if (product == null) {
			return new AdminProductRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(), //
					ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}

		// 更新狀態
		product.setActive(active);
		productsDao.save(product);

		return new AdminProductRes(ReplyMessage.PRODUCT_UPDATE_SUCCESS.getCode(), //
				ReplyMessage.PRODUCT_UPDATE_SUCCESS.getMessage(), //
				convertToAdminVo(product), new ArrayList<>());
	}

	// 查詢所有「未刪除」的商品 (給清單頁)
	public AdminProductRes getActiveProducts(HttpSession session) {
		AdminProductRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return authRes;

		List<Products> products = productsDao.findByDeletedAtIsNull();
		List<ProductAdminVo> voList = products.stream().map( //
				this::convertToAdminVo).collect(Collectors.toList());

		return new AdminProductRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), voList);
	}

	// 查詢所有「已刪除」的商品 (給垃圾桶頁面)
	public AdminProductRes getDeletedProducts(HttpSession session) {
		AdminProductRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return authRes;

		List<Products> products = productsDao.findByDeletedAtIsNotNull();
		List<ProductAdminVo> voList = new ArrayList<>();

		for (Products p : products) {
			voList.add(convertToAdminVo(p));
		}
		return new AdminProductRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), voList);
	}

	// 查詢單一商品的詳細資料
	public AdminProductRes getProductById(int id, HttpSession session) {
		AdminProductRes authRes = validateAdminAccess(session);
		if (authRes != null)
			return authRes;

		Products product = productsDao.findById(id);
		if (product == null) {
			return new AdminProductRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(), //
					ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}

		return new AdminProductRes(ReplyMessage.SUCCESS.getCode(), //
				ReplyMessage.SUCCESS.getMessage(), convertToAdminVo(product));
	}

	// 工具 - 檢查參數
	private AdminProductRes checkParam(ProductCreateReq req, MultipartFile file, //
			boolean isUpdate, Integer productId) {
		// 1. 名稱檢查
		if (isUpdate) {
			// 修改時：檢查「有沒有別的商品」已經用了這個名字
			if (productsDao.existsByNameAndIdNot(req.getName(), productId)) {
				return new AdminProductRes(ReplyMessage.PRODUCT_EXISTS.getCode() //
						, ReplyMessage.PRODUCT_EXISTS.getMessage());
			}
		} else {
			// 新增時：檢查名稱是否已存在
			if (productsDao.existsByName(req.getName())) {
				return new AdminProductRes(ReplyMessage.PRODUCT_EXISTS.getCode() //
						, ReplyMessage.PRODUCT_EXISTS.getMessage());
			}
		}

		// 2. 檔案大小檢查 (5MB)
		if (file != null && !file.isEmpty()) {
			if (file.getSize() > 5 * 1024 * 1024) {
				return new AdminProductRes(ReplyMessage.IMAGE_TOO_LARGE.getCode(), //
						ReplyMessage.IMAGE_TOO_LARGE.getMessage());
			}
		}
		return null;
	}

	// 工具 - 圖片轉換成前端能夠直接使用
	private String encodeImage(byte[] imageBytes) {
		return (imageBytes != null && imageBytes.length > 0) ? Base64.getEncoder().encodeToString(imageBytes) : "";
	}

	// 工具 - 轉換為 Admin VO (給管理者看完整資訊，且前端適用)
	private ProductAdminVo convertToAdminVo(Products p) {
		ProductAdminVo vo = new ProductAdminVo();
		vo.setId(p.getId());
		vo.setName(p.getName());
		vo.setCategory(p.getCategory());
		vo.setDescription(p.getDescription());
		vo.setActive(p.isActive());
		vo.setFoodImgBase64(encodeImage(p.getFoodImg()));
		return vo;
	}

	// 工具 - 轉換為 InventoryDetailVo VO (給管理者看完整資訊，且前端適用)
	private InventoryDetailVo convertToInventoryDetailVo(BranchInventory inv, Map<Integer, String> branchMap) {
		InventoryDetailVo vo = new InventoryDetailVo();
		vo.setProductId(inv.getProductId());
		vo.setGlobalAreaId(inv.getGlobalAreaId());
		vo.setBasePrice(inv.getBasePrice());
		vo.setStockQuantity(inv.getStockQuantity());
		vo.setMaxOrderQuantity(inv.getMaxOrderQuantity());

		// 從傳入的 Map 獲取分店名稱，如果找不到則預設為 "未知分店"
		vo.setBranchName(branchMap.getOrDefault(inv.getGlobalAreaId(), "未知分店"));

		return vo;
	}

	// 2. 工具 - 管理員權限檢查 (用於新增、修改、刪除)
	private AdminProductRes validateAdminAccess(HttpSession session) {
		Staff staff = (Staff) session.getAttribute(SESSION_KEY);

		// 檢查登入
		if (staff == null) {
			return new AdminProductRes(ReplyMessage.NOT_LOGIN.getCode(), ReplyMessage.NOT_LOGIN.getMessage());
		}
		// 檢查狀態
		if (!staff.isStatus()) {
			return new AdminProductRes(ReplyMessage.ACCOUNT_DISABLED.getCode(),
					ReplyMessage.ACCOUNT_DISABLED.getMessage());
		}
		// 檢查權限 (只有 ADMIN 能操作)
		if (staff.getRole() != StaffRole.ADMIN) {
			return new AdminProductRes(ReplyMessage.OPERATE_ERROR.getCode(), "權限不足，僅限管理員操作");
		}
		return null;
	}
	
	// 以下為艷羽寫的
	private static final int YEAR_MIN = 2020;
	@Autowired
	private OrdersDao ordersDao;
	@Autowired
	private RegionsDao regionsDao;

	/*
	 * =================================================================
	 * 【功能A】分店長查詢：某年某月 該分店 所有商品銷售量
	 *
	 * 權限規則： - 只有 REGION_MANAGER（分店長）可以呼叫此方法 - 分店長只能看自己分店（globalAreaId）的資料，不能跨店查詢 -
	 * globalAreaId 從 Session 的 operator 物件取，不信任前端傳入
	 *
	 * operator：從 Controller 傳進來的 Session 登入者物件
	 * =================================================================
	 */
	@Transactional(readOnly = true)
	public MonthlyProductsSalesRes getMonthlySalesByBranch(MonthlyProductsSalesReq req, Staff operator) {

		// Step 1：確認是分店長
		// operator.getRole() 取出 Session 裡存的 Staff 的身份（StaffRole enum）
		if (operator.getRole() != StaffRole.REGION_MANAGER) {
			return new MonthlyProductsSalesRes(ReplyMessage.OPERATE_ERROR.getCode(),
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// Step 2：年份基本防呆（不能查未來，不能查太久以前）
		int currentYear = java.time.LocalDate.now().getYear();
		if (req.getYear() < YEAR_MIN  || req.getYear() > currentYear) {
			return new MonthlyProductsSalesRes(400, "年份超出合法範圍");
		}
		// 未來月份防呆（加在年份防呆的正後面）──
		// 取得當前月份（1 ~ 12）
		int currentMonth = java.time.LocalDate.now().getMonthValue();
		// 條件：今年 且 查的月份 > 現在這個月 = 查未來，擋掉
		if (req.getYear() == currentYear && req.getMonth() > currentMonth) {
		    // %d 填年份，%02d 填月份（不足兩位補0，例如4→04）
		    return new MonthlyProductsSalesRes(400,
		        String.format("%d年%02d月還未結束，暫無銷售數據", req.getYear(), req.getMonth()));
		}

		// Step 3：把 year + month 組成 SQL LIKE 用的字串，例如 "202604%"
		// %d = 年份原樣輸出，%02d = 月份不足兩位補零（4 → "04"），%% = SQL 的萬用字元 %
		String yearMonth = String.format("%d%02d%%", req.getYear(), req.getMonth());

		// Step 4：取出分店長自己的分店 ID（從 Session 取，不信任前端）
		// 這是安全設計的核心：Session 存在伺服器端，前端無法偽造
		int globalAreaId = operator.getGlobalAreaId();

		// Step 5：呼叫 DAO 查詢該分店的銷售資料
		List<Object[]> rawData = ordersDao.getMonthlySalesByBranch(yearMonth, globalAreaId);
		// ── 空結果語意化──
		// 如果查回來是空的，代表這個月/分店確實沒有任何已完成的訂單
		if (rawData == null || rawData.isEmpty()) {
		    // 回傳 200（不是錯誤），但附上清楚的說明訊息
		    // 傳空清單，讓前端知道是「真的沒有資料」而不是系統錯誤
		    return new MonthlyProductsSalesRes(
		        ReplyMessage.SUCCESS.getCode(),
		        String.format("%d年%02d月查無銷售記錄", req.getYear(), req.getMonth()),
		        new ArrayList<>()  // 明確傳空清單，不是 null
		    );
		}
		
		List<MonthlyProductsSalesVo> salesList = toVoList(rawData);


		return new MonthlyProductsSalesRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(),
				salesList);
	}

	/*
	 * =================================================================
	 * 【功能B】老闆查詢：某年某月 指定國家 所有分店 銷售前5名商品（v2 更新）
	 *
	 * 權限規則： - 只有 ADMIN（老闆）可以呼叫此方法 - 老闆可以選擇任何國家（regionId 由前端傳入是安全的，因為角色已驗過） -
	 * regionId 對應 regions 表的 id 欄位
	 *
	 * operator：從 Controller 傳進來的 Session 登入者物件
	 * =================================================================
	 */
	@Transactional(readOnly = true)
	public MonthlyProductsSalesRes getTop5MonthlySalesByRegion(MonthlyProductsSalesReq req, Staff operator) {

		// Step 1：確認是老闆
		if (operator.getRole() != StaffRole.ADMIN) {
			return new MonthlyProductsSalesRes(ReplyMessage.OPERATE_ERROR.getCode(),
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// Step 2：年份防呆
		int currentYear = java.time.LocalDate.now().getYear();
		if (req.getYear() < YEAR_MIN || req.getYear() > currentYear) {
			return new MonthlyProductsSalesRes(400, "年份超出合法範圍");
		}
		// ── 未來月份防呆
		int currentMonth = java.time.LocalDate.now().getMonthValue();
		if (req.getYear() == currentYear && req.getMonth() > currentMonth) {
		    return new MonthlyProductsSalesRes(400,
		        String.format("%d年%02d月還未結束，暫無銷售數據", req.getYear(), req.getMonth()));
		}

		// Step 3：國家 ID 防呆（老闆查詢必須傳 regionId）
		if (req.getRegionId() == null || req.getRegionId() <= 0) {
			return new MonthlyProductsSalesRes(400, "請選擇國家");
		}
		Regions targetRegion = regionsDao.findById(req.getRegionId()).orElse(null);
		if (targetRegion == null) {
		    return new MonthlyProductsSalesRes(400,
		        "國家 ID " + req.getRegionId() + " 不存在，請重新選擇");
		}
		// Step 4：組成 LIKE 字串
		String yearMonth = String.format("%d%02d%%", req.getYear(), req.getMonth());

		// Step 5：查詢指定國家的所有分店 TOP 5
		// SQL 內部會 JOIN global_area + regions 過濾 r.id = regionId
		List<Object[]> rawData = ordersDao.getTop5MonthlySalesByRegion(yearMonth, req.getRegionId());
		// ── 空結果語意化
				// 如果查回來是空的，代表這個月份/國家確實沒有任何已完成的訂單
				if (rawData == null || rawData.isEmpty()) {
				    return new MonthlyProductsSalesRes(
				        ReplyMessage.SUCCESS.getCode(),
				        String.format("%d年%02d月在此國家查無銷售記錄", req.getYear(), req.getMonth()),
				        new ArrayList<>()  // 明確傳空清單，不是 null
				    );
				}
		// Step 6：轉 VO（與功能A共用同樣格式）
		// 原本那段 for 迴圈，刪掉，改成：
		List<MonthlyProductsSalesVo> salesList = toVoList(rawData);


		return new MonthlyProductsSalesRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(),
				salesList);
	}
	// =====================================================================
	// 私有工具方法：把 DB 原始資料（Object[]）轉換成前端看得懂的 VO 清單
	// Step 6：把 Object[] 轉成前端看得懂的 VO 物件
			// Object[0] = productName（商品名稱，String）
			// Object[1] = totalQuantity（銷售總量）
			//
			// 【為什麼用 ((Number) row[1]).intValue()？】
			// SUM() 在 MySQL 中永遠回傳 DECIMAL，JPA 會把它映射成 Java 的 BigDecimal。
			// 用 (Number) 父類別接住，再呼叫 intValue()，幫我轉成 int（整數）來用,
			//比直接 (Integer) 或 (BigDecimal) 更安全，
			// 不管 DB 回的是 BigDecimal / Long / Integer 都不會噴 ClassCastException。
			// 原本那段 for 迴圈，刪掉，改成：
	// =====================================================================
	private List<MonthlyProductsSalesVo> toVoList(List<Object[]> rawData) {
	    // 建立一個空清單，準備裝填轉換好的 VO
	    List<MonthlyProductsSalesVo> salesList = new ArrayList<>();
	    // 逐一把每列原始資料（Object[]）轉成乾淨的 VO 物件
	    for (Object[] row : rawData) {
	        MonthlyProductsSalesVo vo = new MonthlyProductsSalesVo();
	        // row[0] 是 SQL 第一欄 p.name（商品名稱），強制轉成 String
	     // row[0] 是 p.name（商品名稱），null 時顯示「未知商品」避免前端爆版
	        vo.setProductName(row[0] != null ? (String) row[0] : "未知商品");
	        // row[1] 是 SQL 第二欄 SUM(d.quantity)（銷售總量）
	        // MySQL SUM() 回傳 DECIMAL，JPA 映射成 BigDecimal
	        // 用 Number 父類別接住，再 .intValue() 轉成 int，最安全
	        // null 防護：如果 SUM 回傳 null（理論上 GROUP BY 後不該發生，但保險一下），設為 0
	        vo.setTotalQuantity(row[1] != null ? ((Number) row[1]).intValue() : 0);
	        salesList.add(vo);
	    }
	    return salesList; // 回傳裝好的清單
	}
}