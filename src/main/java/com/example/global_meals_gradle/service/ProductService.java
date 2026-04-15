package com.example.global_meals_gradle.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.BranchInventoryDao;
import com.example.global_meals_gradle.dao.GlobalAreaDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.entity.BranchInventory;
import com.example.global_meals_gradle.entity.GlobalArea;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.req.ProductCreateReq;
import com.example.global_meals_gradle.req.ProductUpdateReq;
import com.example.global_meals_gradle.res.AdminProductRes;
import com.example.global_meals_gradle.vo.ProductAdminVo;

@Service
public class ProductService {

	@Autowired
	private ProductsDao productsDao;

	@Autowired
	private BranchInventoryDao branchInventoryDao;

	@Autowired
	private GlobalAreaDao globalAreaDao;

	// 檢查參數
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

	// 共用圖片轉換工具 (私有方法)
	private String encodeImage(byte[] imageBytes) {
		return (imageBytes != null && imageBytes.length > 0) ? Base64.getEncoder().encodeToString(imageBytes) : "";
	}

	// 轉換為 Admin VO (給管理者看完整資訊，且前端適用)
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

	// 新增商品 ( 同時會新增每個分店庫存為 0 )
	@Transactional(rollbackFor = Exception.class)
	public AdminProductRes createProduct(ProductCreateReq req, MultipartFile file) {
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

			// 4. 回傳包含完整資訊的 AdminProductRes
			return new AdminProductRes(ReplyMessage.PRODUCT_CREATE_SUCCESS.getCode(), //
					ReplyMessage.PRODUCT_CREATE_SUCCESS.getMessage(), convertToAdminVo(product), inventoryList);

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

	// 新增一個公開方法，專門處理「對特定分店初始化所有商品」
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
			inventory.setMaxOrderQuantity(0);
			inventory.setStockQuantity(0); // 初始化為 0
			inventory.setUpdatedAt(LocalDateTime.now());
			inventory.setVersion(1);
			inventoryList.add(inventory);
		}

		if (!inventoryList.isEmpty()) {
			branchInventoryDao.saveAll(inventoryList);
		}
	}

	// 修改商品
	@Transactional(rollbackFor = Exception.class)
	public AdminProductRes updateProduct(ProductUpdateReq req, MultipartFile file) {
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
					ReplyMessage.PRODUCT_UPDATE_SUCCESS.getMessage(), convertToAdminVo(product), null);

		} catch (IOException e) {
			return new AdminProductRes(ReplyMessage.IMAGE_ERROR.getCode(), //
					ReplyMessage.IMAGE_ERROR.getMessage());
		}
	}

	// 軟刪除商品
	@Transactional(rollbackFor = Exception.class)
	public AdminProductRes deleteProduct(int id) {
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
						convertToAdminVo(product), null);
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
	public AdminProductRes updateActiveStatus(int id, boolean active) {
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
				convertToAdminVo(product), null);
	}

	// 查詢所有「未刪除」的商品 (給清單頁)
	public List<ProductAdminVo> getActiveProducts() {
		List<Products> products = productsDao.findByDeletedAtIsNull();
		List<ProductAdminVo> voList = new ArrayList<>();

		for (Products p : products) {
			voList.add(convertToAdminVo(p)); // 使用你剛寫好的轉換方法
		}
		return voList;
	}

	// 查詢所有「已刪除」的商品 (給垃圾桶頁面)
	public List<ProductAdminVo> getDeletedProducts() {
		List<Products> products = productsDao.findByDeletedAtIsNotNull();
		List<ProductAdminVo> voList = new ArrayList<>();

		for (Products p : products) {
			voList.add(convertToAdminVo(p));
		}
		return voList;
	}

	// 查詢單一商品的詳細資料
	public ProductAdminVo getProductById(int id) {
		Products product = productsDao.findById(id);
		if (product == null) {
			return null;
		}
		return convertToAdminVo(product);
	}
}