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
						.orElseThrow(() -> new RuntimeException("更新失敗：此分店找不到商品 ID " + req.getProductId() + " 的庫存"));

				// 2. ✨ 新增：檢查主表狀態 (直接用 ID 查，不透過 Map)
	            // 假設你的 productsDao.findById 回傳的是實體，或者你有一個 getDeletedAt 的方法
	            Products masterProduct = productsDao.findById(req.getProductId());
	            
	            if (masterProduct == null || masterProduct.getDeletedAt() != null) {
	                throw new RuntimeException("更新失敗：商品 [" + masterProduct.getName() + "] 已被總部刪除，無法修改資料。");
	            }

	            // 3. ✨ 新增：檢查總部是否下架 (主表 is_active = false)
	            // 如果總部下架，但前端嘗試把分店改為 active=true，則攔截
	            if (req.isActive() && !masterProduct.isActive()) {
	                throw new RuntimeException("更新失敗：商品 [" + masterProduct.getName() + "] 總部目前處於下架狀態，分店無法開啟供應。");
	            }
				
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
			Map<Integer, String> productMap = productsDao.getProductNameMap();
			List<InventoryDetailVo> resultList = toUpdateList.stream()
					.map(inv -> convertToInventoryDetailVo(inv, branchMap, productMap)).collect(Collectors.toList());

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
			inventory.setMaxOrderQuantity(1);
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
		// 因為只需要告知前端成功，你可以直接轉成 VO 列表回傳，或是只回傳狀態碼
		Map<Integer, String> branchMap = globalAreaDao.getBranchNameMap();
		Map<Integer, String> productMap = productsDao.getProductNameMap();
		List<InventoryDetailVo> resultList = new ArrayList<>();
		resultList.add(convertToInventoryDetailVo(inv, branchMap, productMap));

		return new BranchInventoryRes(ReplyMessage.INVENTORY_UPDATE_SUCCESS.getCode(),
				ReplyMessage.INVENTORY_UPDATE_SUCCESS.getMessage(), resultList);
	}

	// 分店取得菜單
	public MenuListRes getMenuByArea(int globalAreaId) {
	    try {
	        // 1. 檢查分店 ID 是否存在 (使用你現有的 globalAreaDao)
	        boolean branchExists = globalAreaDao.existsById(globalAreaId);
	        if (!branchExists) {
	            return new MenuListRes(
	                ReplyMessage.BRANCH_NOT_FOUND.getCode(), 
	                ReplyMessage.BRANCH_NOT_FOUND.getMessage(), 
	                null
	            );
	        }
	
	        // 2. 取得組合資料 (Products 實體 + 價格 + 庫存 + 分店狀態)
	        List<Object[]> rawResults = branchInventoryDao.getMenuEntitiesByArea(globalAreaId);
	
	        // 3. 轉換為 MenuVo 清單
	        List<MenuVo> menuList = rawResults.stream().map(row -> {
	            Products p = (Products) row[0];            // 商品實體
	            BigDecimal price = (BigDecimal) row[1];     // 該店價格
	            Integer stock = (Integer) row[2];          // 該店庫存
	            Boolean biActive = (Boolean) row[3];       // 分店自己的上下架狀態
	
	            MenuVo vo = new MenuVo();
	            vo.setProductId(p.getId());
	            vo.setName(p.getName());
	            
	            // 利用 @ManyToOne 取得關聯名稱
	            vo.setCategory(p.getCategory() != null ? p.getCategory().getName() : "未分類");
	            vo.setStyle(p.getStyle() != null ? p.getStyle().getName() : "無風格");
	            
	            vo.setDescription(p.getDescription());
	            
	            // 處理圖片轉碼 (Lazy Loading 會在此時被觸發)
	            vo.setFoodImgBase64(getFullBase64(p.getFoodImg()));
	
	            // 設定分店專屬數值
	            vo.setBasePrice(price);
	            vo.setStockQuantity(stock);
	            vo.setActive(biActive);
	
	            return vo;
	        }).collect(Collectors.toList());
	
	        // 4. 回傳成功結果
	        return new MenuListRes(
	            ReplyMessage.SUCCESS.getCode(), 
	            ReplyMessage.SUCCESS.getMessage(), 
	            menuList
	        );
	
	    } catch (Exception e) {
	        // 捕捉系統錯誤
	        return new MenuListRes(
	            ReplyMessage.SYSTEM_ERROR.getCode(), 
	            ReplyMessage.SYSTEM_ERROR.getMessage() + ": " + e.getMessage(), 
	            null
	        );
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
		// 1. 取得分店名稱 Map
		Map<Integer, String> branchMap = globalAreaDao.getBranchNameMap();

		// 2. 取得商品名稱 Map (補上這行)
		Map<Integer, String> productMap = productsDao.getProductNameMap();

		List<InventoryDetailVo> voList = list.stream(). //
				map(inv -> convertToInventoryDetailVo(inv, branchMap, productMap)) //
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
			Map<Integer, String> productMap) {

		InventoryDetailVo vo = new InventoryDetailVo();

		vo.setProductId(inv.getProductId());
		vo.setStockQuantity(inv.getStockQuantity());
		vo.setBasePrice(inv.getBasePrice());
		vo.setCostPrice(inv.getCostPrice());
		vo.setMaxOrderQuantity(inv.getMaxOrderQuantity());
		vo.setActive(inv.isActive());
		vo.setGlobalAreaId(inv.getGlobalAreaId());

		// 設定分店名稱
		vo.setBranchName(branchMap.getOrDefault(inv.getGlobalAreaId(), "未知分店"));

		// ✨ 關鍵：從 Map 根據 productId 設定商品名稱 ✨
		vo.setProductName(productMap.getOrDefault(inv.getProductId(), "未知商品"));

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
