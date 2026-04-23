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
				inv.setMaxOrderQuantity(req.getMaxOrderQuantity());
				inv.setUpdatedAt(LocalDateTime.now());

				toUpdateList.add(inv);
			}

			// 批次儲存
			branchInventoryDao.saveAll(toUpdateList);

			// 轉換 VO
			Map<Integer, String> branchMap = globalAreaDao.getBranchNameMap();
			List<InventoryDetailVo> resultList = toUpdateList.stream()
					.map(inv -> convertToInventoryDetailVo(inv, branchMap)).collect(Collectors.toList());

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
			inventory.setMaxOrderQuantity(1);
			inventory.setStockQuantity(0); // 初始化為 0
			inventory.setUpdatedAt(LocalDateTime.now());
			inventory.setVersion(1);
			inventoryList.add(inventory);
		}

		if (!inventoryList.isEmpty()) {
			branchInventoryDao.saveAll(inventoryList);
		}
	}

	// 分店取得菜單
	public MenuListRes getMenuByArea(int globalAreaId) {
		try {
			List<Object[]> rawList = branchInventoryDao.getMenuByArea(globalAreaId);

			// 將 Object[] 轉為 MenuVo
			List<MenuVo> menuList = rawList.stream().map(obj -> {
				MenuVo vo = new MenuVo();
				// 注意：這裡的索引順序必須完全對應你 SQL 語法 SELECT 出來的欄位順序
				// 假設 SELECT p.* (id, name, category, description, image, ...),
				// bi.base_price,bi.stock_quantity
				vo.setProductId((Integer) obj[0]);
				vo.setName((String) obj[1]);
				vo.setCategory((String) obj[2]);
				vo.setDescription((String) obj[3]);
				vo.setFoodImgBase64(encodeImage((byte[]) obj[4])); // 假設第5個是 image
				vo.setBasePrice((BigDecimal) obj[obj.length - 2]); // 取最後倒數第二個
				vo.setStockQuantity((Integer) obj[obj.length - 1]); // 取最後一個
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
		List<InventoryDetailVo> voList = list.stream(). //
				map(inv -> convertToInventoryDetailVo(inv, branchMap)) //
				.collect(Collectors.toList());

		return new BranchInventoryRes( //
				ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), voList);
	}

	// 工具 - 權限檢查
	// 權限檢查方法 (回傳 null 代表通過，回傳 BasicRes 代表失敗)
	private BasicRes validateAccess(HttpSession session, int targetGlobalAreaId) {
		// 1. 嘗試從 Session 取得使用者
		Staff staff = (Staff) session.getAttribute(SESSION_KEY);

		// 2. 檢查是否登入
		if (staff == null) {
			return new BasicRes(ReplyMessage.NOT_LOGIN.getCode(), ReplyMessage.NOT_LOGIN.getMessage());
		}

		// 3. 檢查帳號是否被停用
		if (!staff.isStatus()) {
			return new BasicRes(ReplyMessage.ACCOUNT_DISABLED.getCode(), //
					ReplyMessage.ACCOUNT_DISABLED.getMessage());
		}

		// 4. 權限判定
		// 如果是 ADMIN，直接放行 (回傳 null 代表沒有錯誤)
		if (staff.getRole() == StaffRole.ADMIN) {
			return null;
		}

		// 如果不是 ADMIN，檢查他是否有權限看該分店
		if (staff.getGlobalAreaId() != targetGlobalAreaId) {
			// 使用你的 OPERATE_ERROR 來代表權限不足
			return new BasicRes(ReplyMessage.OPERATE_ERROR.getCode(), //
					ReplyMessage.OPERATE_ERROR.getMessage());
		}

		// 通過所有驗證
		return null;
	}

	// 工具 - 轉換為 InventoryDetailVo VO (給管理者看完整資訊，且前端適用)
	private InventoryDetailVo convertToInventoryDetailVo(BranchInventory inv, //
			Map<Integer, String> branchMap) {
		InventoryDetailVo vo = new InventoryDetailVo();
		vo.setProductId(inv.getProductId());
		vo.setGlobalAreaId(inv.getGlobalAreaId());
		vo.setBranchName(branchMap.getOrDefault(inv.getGlobalAreaId(), "未知分店"));
		vo.setBasePrice(inv.getBasePrice());
		vo.setStockQuantity(inv.getStockQuantity());
		vo.setMaxOrderQuantity(inv.getMaxOrderQuantity());
		return vo;
	}

	// 圖片轉換工具
	private String encodeImage(byte[] imageBytes) {
		return (imageBytes != null && imageBytes.length > 0) ? //
				Base64.getEncoder().encodeToString(imageBytes) //
				: "";
	}
}
