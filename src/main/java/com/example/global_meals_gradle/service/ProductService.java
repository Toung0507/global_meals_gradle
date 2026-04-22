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
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.entity.BranchInventory;
import com.example.global_meals_gradle.entity.GlobalArea;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.entity.Staff;
import com.example.global_meals_gradle.req.ProductCreateReq;
import com.example.global_meals_gradle.req.ProductUpdateReq;
import com.example.global_meals_gradle.res.AdminProductRes;
import com.example.global_meals_gradle.vo.InventoryDetailVo;
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
}