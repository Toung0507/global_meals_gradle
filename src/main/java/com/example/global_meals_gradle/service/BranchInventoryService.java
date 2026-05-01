package com.example.global_meals_gradle.service;

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

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.StaffRole;
import com.example.global_meals_gradle.dao.BranchInventoryDao;
import com.example.global_meals_gradle.dao.GlobalAreaDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.entity.BranchInventory;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.BranchInventoryUpdateReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.BranchInventoryRes;
import com.example.global_meals_gradle.res.MenuListRes;
import com.example.global_meals_gradle.vo.InventoryDetailVo;
import com.example.global_meals_gradle.vo.MenuVo;

import jakarta.servlet.http.HttpSession;

@Service
public class BranchInventoryService {
	@Autowired
	private BranchInventoryDao branchInventoryDao;

	@Autowired
	private GlobalAreaDao globalAreaDao;

	@Autowired
	private ProductsDao productsDao;

	private static final String SESSION_KEY = "loginStaff";

	// 更新庫存
	@Transactional(rollbackFor = Exception.class)
	public BranchInventoryRes updateInventory(List<BranchInventoryUpdateReq> reqList) {
		try {
			List<BranchInventory> toUpdateList = new ArrayList<>();

			for (BranchInventoryUpdateReq req : reqList) {
				// 使用組員的一行式寫法 (強勢邏輯版)
				BranchInventory inv = branchInventoryDao
						.findByProductIdAndGlobalAreaId(req.getProductId(), req.getGlobalAreaId())
						.orElseThrow(() -> new RuntimeException("更新失敗：找不到商品 ID " + req.getProductId() + " 的庫存"));

				// 更新欄位
				inv.setStockQuantity(req.getStockQuantity());
				inv.setBasePrice(req.getBasePrice());
				inv.setCostPrice(req.getCostPrice());
				inv.setMaxOrderQuantity(req.getMaxOrderQuantity());
				inv.setActive(req.isActive());
				inv.setUpdatedAt(LocalDateTime.now());

				toUpdateList.add(inv);
			}

			// 批次儲存
			branchInventoryDao.saveAll(toUpdateList);

			// 轉換 VO
			Map<Integer, String> branchMap = globalAreaDao.getBranchNameMap();
			Map<Integer, String> productNameMap = productsDao.getProductNameMap();
			Map<Integer, String> productCategoryMap = productsDao.getProductCategoryMap();
			List<InventoryDetailVo> resultList = toUpdateList.stream()
					.map(inv -> convertToInventoryDetailVo(inv, branchMap, productNameMap, productCategoryMap)).collect(Collectors.toList());

			// 成功回傳
			return new BranchInventoryRes( //
					ReplyMessage.INVENTORY_UPDATE_SUCCESS.getCode(), //
					ReplyMessage.INVENTORY_UPDATE_SUCCESS.getMessage(), resultList);

		} catch (RuntimeException e) {
			// 捕捉到剛剛拋出的錯誤，回傳給前端錯誤訊息
			return new BranchInventoryRes( //
					ReplyMessage.INVENTORY_NOT_FOUND.getCode(), //
					e.getMessage() // 這裡是 RuntimeException 傳進來的 message
			);
		} catch (Exception e) {
			// 捕捉其他預期外的系統錯誤
			return new BranchInventoryRes(ReplyMessage.SYSTEM_ERROR.getCode(), //
					ReplyMessage.SYSTEM_ERROR.getMessage() + e.getMessage());
		}
	}

	// 專門處理「對特定分店初始化所有商品」
	@Transactional(rollbackFor = Exception.class)
	public void initInventoryForNewBranch(int branchId) {
		List<Products> allProducts = productsDao.findAll();
		// 如果資料庫裡還沒有任何商品，那就直接結束，不用跑迴圈
		if (allProducts.isEmpty()) {
			return;
		}
		List<BranchInventory> inventoryList = new ArrayList<>();

		for (Products p : allProducts) {
			BranchInventory inventory = new BranchInventory();
			inventory.setProductId(p.getId());
			inventory.setGlobalAreaId(branchId); // 傳入新分店的 ID
			inventory.setBasePrice(BigDecimal.ZERO);
			inventory.setCostPrice(BigDecimal.ZERO);
			inventory.setMaxOrderQuantity(0);
			inventory.setStockQuantity(0); // 初始化為 0
			inventory.setUpdatedAt(LocalDateTime.now());
			inventory.setActive(false);
			inventory.setVersion(1);
			inventoryList.add(inventory);
		}

		if (!inventoryList.isEmpty()) {
			branchInventoryDao.saveAll(inventoryList);
		}
	}

	// 快速修正分店內商品的上下架狀態
	@Transactional(rollbackFor = Exception.class)
	public BranchInventoryRes updateBranchActiveStatus(int productId, int globalAreaId, boolean active,
			HttpSession session) {
		// 1. 權限檢查 (重複使用你現有的 validateAccess)
		// 這樣可以確保分店長只能改自己店的，Admin 可以改所有店
		BasicRes authRes = validateAccess(session, globalAreaId);
		if (authRes != null) {
			return new BranchInventoryRes(authRes.getCode(), authRes.getMessage());
		}

		// 2. 找到該分店的庫存紀錄
		BranchInventory inv = branchInventoryDao.findByProductIdAndGlobalAreaId(productId, globalAreaId).orElse(null);

		if (inv == null) {
			return new BranchInventoryRes(ReplyMessage.INVENTORY_NOT_FOUND.getCode(),
					ReplyMessage.INVENTORY_NOT_FOUND.getMessage());
		}

		// 新增：如果要「上架」商品，檢查總部主表狀態
		if (active && !productsDao.isProductAvailable(productId)) {
		    return new BranchInventoryRes(ReplyMessage.OPERATE_ERROR.getCode(), //
		        "操作失敗：總部已將此商品下架或刪除，分店無法上架。");
		}

		// 3. 更新狀態
		inv.setActive(active);
		inv.setUpdatedAt(LocalDateTime.now());
		branchInventoryDao.save(inv);

		// 4. 回傳結果
		Map<Integer, String> branchMap = globalAreaDao.getBranchNameMap();
		Map<Integer, String> productNameMap = productsDao.getProductNameMap();
		Map<Integer, String> productCategoryMap = productsDao.getProductCategoryMap();
		List<InventoryDetailVo> resultList = new ArrayList<>();
		resultList.add(convertToInventoryDetailVo(inv, branchMap, productNameMap, productCategoryMap));

		return new BranchInventoryRes(ReplyMessage.INVENTORY_UPDATE_SUCCESS.getCode(),
				ReplyMessage.INVENTORY_UPDATE_SUCCESS.getMessage(), resultList);
	}

	// 分店取得菜單
	public MenuListRes getMenuByArea(int globalAreaId) {
		try {
			List<Object[]> rawList = branchInventoryDao.getMenuByArea(globalAreaId);

			// 將 Object[] 轉為 MenuVo
			List<MenuVo> menuList = rawList.stream().map(obj -> {
				MenuVo vo = new MenuVo();

				// 2. 處理 Boolean 的安全轉型邏輯：
				Object activeValue = obj[obj.length - 1]; // 取得最後一個欄位 bi.is_active

				if (activeValue instanceof Number) {
					// 如果是 Byte, Integer, Short 等數字型態 (TINYINT 常見情況)
					vo.setActive(((Number) activeValue).intValue() == 1);
				} else if (activeValue instanceof Boolean) {
					// 如果驅動程式已經幫你轉好 Boolean 了
					vo.setActive((Boolean) activeValue);
				} else {
					// 預設處理
					vo.setActive(false);
				}

				byte[] imgBytes = (byte[]) obj[4]; // 假設第5個是 image
				vo.setFoodImgBase64(getFullBase64(imgBytes));

				vo.setProductId((Integer) obj[0]);
				vo.setName((String) obj[1]);
				vo.setCategory((String) obj[2]);
				vo.setDescription((String) obj[3]);
				vo.setBasePrice((BigDecimal) obj[obj.length - 3]);
				vo.setStockQuantity((Integer) obj[obj.length - 2]);

				return vo;
			}).collect(Collectors.toList());

			return new MenuListRes( //
					ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), menuList);

		} catch (Exception e) {
			return new MenuListRes( //
					ReplyMessage.SYSTEM_ERROR.getCode(), //
					ReplyMessage.SYSTEM_ERROR.getMessage() + e.getMessage());
		}
	}

	// 用分店 ID 查該店所有商品庫存
	public BranchInventoryRes getInventoryByGlobalAreaId(int globalAreaId, HttpSession session) {
		// 1. 直接呼叫工具，如果失敗直接回傳
		BasicRes error = validateAccess(session, globalAreaId);
		if (error != null) {
			return new BranchInventoryRes(error.getCode(), error.getMessage());
		}

		// 2. 驗證通過，正常查詢
		List<BranchInventory> list = branchInventoryDao.findByGlobalAreaId(globalAreaId);
		return convertToRes(list);
	}

	// 用商品 ID 查該商品在 (所有) 分店的庫存
	public BranchInventoryRes getInventoryByProductId(int productId, HttpSession session) {
		// 1. 基礎檢查：確認登入與狀態 (直接複製 validateAccess 的前段邏輯)
		Staff staff = (Staff) session.getAttribute(SESSION_KEY);
		if (staff == null) {
			return new BranchInventoryRes(ReplyMessage.NOT_LOGIN.getCode(), //
					ReplyMessage.NOT_LOGIN.getMessage());
		}
		if (!staff.isStatus()) {
			return new BranchInventoryRes(ReplyMessage.ACCOUNT_DISABLED.getCode(), //
					ReplyMessage.ACCOUNT_DISABLED.getMessage());
		}

		// 2. 根據 Role 決定查詢範圍
		List<BranchInventory> list;
		if (staff.getRole() == StaffRole.ADMIN) {
			// 管理員：查所有分店
			list = branchInventoryDao.findByProductId(productId);
		} else {
			// 一般員工：只查自己分店 ( 列表內只有一筆是正常的，因為正常不會進到這邊 )
			list = branchInventoryDao.findByProductIdAndGlobalAreaIdForStaff( //
					productId, staff.getGlobalAreaId());
		}

		return convertToRes(list);
	}

	// 轉成 BranchInventoryRes
	private BranchInventoryRes convertToRes(List<BranchInventory> list) {
		Map<Integer, String> branchMap = globalAreaDao.getBranchNameMap();
		Map<Integer, String> productNameMap = productsDao.getProductNameMap();
		Map<Integer, String> productCategoryMap = productsDao.getProductCategoryMap();

		List<InventoryDetailVo> voList = list.stream()
				// 過濾掉 name 為 null 的無效商品（productNameMap 不含 null 名稱）
				.filter(inv -> productNameMap.containsKey(inv.getProductId()))
				.map(inv -> convertToInventoryDetailVo(inv, branchMap, productNameMap, productCategoryMap))
				.collect(Collectors.toList());

		return new BranchInventoryRes( //
				ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), voList);
	}

	// 工具 - 權限檢查
	// 權限檢查方法 (回傳 null 代表通過，回傳 BasicRes 代表失敗)
	private BasicRes validateAccess(HttpSession session, int targetGlobalAreaId) {
		Staff staff = (Staff) session.getAttribute(SESSION_KEY);

		// 1. 基礎驗證
		if (staff == null) {
			return new BasicRes(ReplyMessage.NOT_LOGIN.getCode(), ReplyMessage.NOT_LOGIN.getMessage());
		}
		if (!staff.isStatus()) {
			return new BasicRes(ReplyMessage.ACCOUNT_DISABLED.getCode(), ReplyMessage.ACCOUNT_DISABLED.getMessage());
		}

		// 2. [通過條件 A] 管理員：直接放行
		if (staff.getRole() == StaffRole.ADMIN) {
			return null; // 驗證通過
		}

		// 3. [通過條件 B] 分店長：檢查身分 AND 檢查是否為該店負責人
		if (staff.getRole() == StaffRole.REGION_MANAGER || staff.getRole() == StaffRole.MANAGER_AGENT) {
			if (staff.getGlobalAreaId() == targetGlobalAreaId) {
				return null; // 驗證通過
			} else {
				// 這裡是「身分對了，但權限範圍不對」
				return new BasicRes(ReplyMessage.OPERATE_ERROR.getCode(), //
						ReplyMessage.OPERATE_ERROR.getMessage());
			}
		}

		// 4. [結尾] 預設拒絕：只要程式跑到這裡，代表上述條件都不滿足
		// (例如他是普通員工，或者其他我們尚未定義權限的角色)
		return new BasicRes(ReplyMessage.OPERATE_ERROR.getCode(), //
				ReplyMessage.OPERATE_ERROR.getMessage());
	}

	// 工具 - 轉換為 InventoryDetailVo VO (給管理者看完整資訊，且前端適用)
	private InventoryDetailVo convertToInventoryDetailVo(BranchInventory inv, //
			Map<Integer, String> branchMap, //
			Map<Integer, String> productNameMap, //
			Map<Integer, String> productCategoryMap) {

		InventoryDetailVo vo = new InventoryDetailVo();

		vo.setProductId(inv.getProductId());
		vo.setStockQuantity(inv.getStockQuantity());
		vo.setBasePrice(inv.getBasePrice());
		vo.setCostPrice(inv.getCostPrice());
		vo.setMaxOrderQuantity(inv.getMaxOrderQuantity());
		vo.setActive(inv.isActive());
		vo.setGlobalAreaId(inv.getGlobalAreaId());
		vo.setBranchName(branchMap.getOrDefault(inv.getGlobalAreaId(), "未知分店"));
		vo.setProductName(productNameMap.getOrDefault(inv.getProductId(), ""));
		vo.setCategory(productCategoryMap.getOrDefault(inv.getProductId(), ""));

		return vo;
	}

	// 統一的工具方法
	private String getFullBase64(byte[] imgBytes) {
	    if (imgBytes == null || imgBytes.length == 0) return "";
	    String mimeType = detectMimeType(imgBytes);
	    String base64 = Base64.getEncoder().encodeToString(imgBytes);
	    return "data:" + mimeType + ";base64," + base64;
	}
	
	// 圖片轉換工具
	private String encodeImage(byte[] imageBytes) {
		return (imageBytes != null && imageBytes.length > 0) ? Base64.getEncoder().encodeToString(imageBytes) : "";
	}

	// 建議直接回傳一個小物件，或是把兩者串起來
	private String detectMimeType(byte[] bytes) {
		if (bytes == null || bytes.length < 4)
			return "image/jpeg"; // 預設值

		// 檢查文件頭 (Magic Numbers)
		String hex = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);

		if (hex.startsWith("89504E47"))
			return "image/png";
		if (hex.startsWith("FFD8FF"))
			return "image/jpeg";
		if (hex.startsWith("47494638"))
			return "image/gif";
		if (hex.startsWith("52494646") && new String(bytes, 8, 4).equals("WEBP"))
			return "image/webp";

		return "image/jpeg"; // 真的都認不出來就猜 jpeg
	}
}
