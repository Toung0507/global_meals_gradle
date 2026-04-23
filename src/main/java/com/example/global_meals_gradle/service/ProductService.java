package com.example.global_meals_gradle.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.dao.BranchInventoryDao;
import com.example.global_meals_gradle.dao.ProductsDao;
import com.example.global_meals_gradle.entity.BranchInventory;
import com.example.global_meals_gradle.entity.Products;
import com.example.global_meals_gradle.req.ProductCreateReq;
import com.example.global_meals_gradle.req.ProductUpdateReq;
import com.example.global_meals_gradle.req.ToggleProductReq;
import com.example.global_meals_gradle.req.UpdateBranchInventoryReq;
import com.example.global_meals_gradle.res.BasicRes;
import com.example.global_meals_gradle.res.BranchInventoryRes;
import com.example.global_meals_gradle.res.ProductsRes;
import com.example.global_meals_gradle.vo.BranchInventoryVO;
import com.example.global_meals_gradle.vo.ProductVO;

@Service
public class ProductService {

	@Autowired
	private ProductsDao productsDao;

	@Autowired
	private BranchInventoryDao branchInventoryDao;

	// ── 查詢 ──────────────────────────────────────────

	public ProductsRes getActiveProducts(int globalAreaId) {
		return buildProductsRes(globalAreaId, true);
	}

	public ProductsRes getAllProducts(int globalAreaId) {
		return buildProductsRes(globalAreaId, false);
	}

	private ProductsRes buildProductsRes(int globalAreaId, boolean activeOnly) {
		List<BranchInventory> inventories = branchInventoryDao.findByGlobalAreaId(globalAreaId);
		if (inventories.isEmpty()) {
			return new ProductsRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), new ArrayList<>());
		}

		Map<Integer, BranchInventory> invMap = inventories.stream()
				.collect(Collectors.toMap(BranchInventory::getProductId, inv -> inv));

		List<Products> products = new ArrayList<>(productsDao.findAllById(invMap.keySet()));
		products = products.stream()
				.filter(p -> p.getDeletedAt() == null)
				.filter(p -> !activeOnly || p.isActive())
				.collect(Collectors.toList());

		List<ProductVO> voList = products.stream().map(p -> {
			BranchInventory inv = invMap.get(p.getId());
			ProductVO vo = new ProductVO();
			vo.setId(p.getId());
			vo.setName(p.getName());
			vo.setCategory(p.getCategory());
			vo.setDescription(p.getDescription());
			vo.setActive(p.isActive());
			vo.setBasePrice(inv != null ? inv.getBasePrice() : p.getBasePrice());
			vo.setStockQuantity(inv != null ? inv.getStockQuantity() : p.getStockQuantity());
			vo.setMaxOrderQuantity(inv != null ? inv.getMaxOrderQuantity() : p.getMaxOrderQuantity());
			return vo;
		}).collect(Collectors.toList());

		return new ProductsRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), voList);
	}

	public String getProductImage(int id) {
		Optional<Products> opt = productsDao.findByIdAndDeletedAtIsNull(id);
		if (opt.isEmpty() || opt.get().getFoodImg() == null) {
			return "";
		}
		return Base64.getEncoder().encodeToString(opt.get().getFoodImg());
	}

	// ── 新增 ──────────────────────────────────────────

	@Transactional
	public BasicRes createProduct(ProductCreateReq req) {
		if (productsDao.existsByName(req.getName())) {
			return new BasicRes(ReplyMessage.PRODUCT_EXISTS.getCode(), ReplyMessage.PRODUCT_EXISTS.getMessage());
		}

		byte[] imageBytes = decodeImage(req.getImageBase64());
		if (imageBytes == null && req.getImageBase64() != null && !req.getImageBase64().isBlank()) {
			return new BasicRes(ReplyMessage.IMAGE_ERROR.getCode(), ReplyMessage.IMAGE_ERROR.getMessage());
		}

		int maxQty = (req.getMaxOrderQuantity() != null && req.getMaxOrderQuantity() >= 1)
				? req.getMaxOrderQuantity() : 5;
		BigDecimal price = BigDecimal.valueOf(req.getBasePrice());

		Products product = new Products();
		product.setName(req.getName());
		product.setCategory(req.getCategory());
		product.setDescription(req.getDescription());
		product.setActive(req.isActive());
		product.setFoodImg(imageBytes);
		product.setBasePrice(price);
		product.setStockQuantity(req.getStockQuantity());
		product.setMaxOrderQuantity(maxQty);
		Products saved = productsDao.save(product);

		BranchInventory inv = new BranchInventory();
		inv.setProductId(saved.getId());
		inv.setGlobalAreaId(req.getGlobalAreaId());
		inv.setBasePrice(price);
		inv.setStockQuantity(req.getStockQuantity());
		inv.setMaxOrderQuantity(maxQty);
		branchInventoryDao.save(inv);

		return new BasicRes(ReplyMessage.PRODUCT_CREATE_SUCCESS.getCode(), ReplyMessage.PRODUCT_CREATE_SUCCESS.getMessage());
	}

	// ── 修改 ──────────────────────────────────────────

	public BasicRes updateProduct(ProductUpdateReq req) {
		Optional<Products> opt = productsDao.findByIdAndDeletedAtIsNull(req.getId());
		if (opt.isEmpty()) {
			return new BasicRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(), ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}
		Products product = opt.get();

		if (req.getName() != null && !req.getName().isBlank()) {
			if (productsDao.existsByNameAndIdNot(req.getName(), req.getId())) {
				return new BasicRes(ReplyMessage.PRODUCT_EXISTS.getCode(), ReplyMessage.PRODUCT_EXISTS.getMessage());
			}
			product.setName(req.getName());
		}
		if (req.getCategory() != null && !req.getCategory().isBlank()) {
			product.setCategory(req.getCategory());
		}
		if (req.getDescription() != null) {
			product.setDescription(req.getDescription());
		}
		if (req.getImageBase64() != null && !req.getImageBase64().isBlank()) {
			byte[] imageBytes = decodeImage(req.getImageBase64());
			if (imageBytes == null) {
				return new BasicRes(ReplyMessage.IMAGE_ERROR.getCode(), ReplyMessage.IMAGE_ERROR.getMessage());
			}
			product.setFoodImg(imageBytes);
		}

		productsDao.save(product);
		return new BasicRes(ReplyMessage.PRODUCT_UPDATE_SUCCESS.getCode(), ReplyMessage.PRODUCT_UPDATE_SUCCESS.getMessage());
	}

	// ── 切換上/下架 ──────────────────────────────────

	public BasicRes toggleProduct(ToggleProductReq req) {
		Optional<Products> opt = productsDao.findByIdAndDeletedAtIsNull(req.getId());
		if (opt.isEmpty()) {
			return new BasicRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(), ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}
		Products product = opt.get();
		product.setActive(req.isActive());
		productsDao.save(product);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	// ── 軟刪除 ───────────────────────────────────────

	public BasicRes deleteProduct(int id) {
		Optional<Products> opt = productsDao.findByIdAndDeletedAtIsNull(id);
		if (opt.isEmpty()) {
			return new BasicRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(), ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}
		Products product = opt.get();
		product.setDeletedAt(LocalDateTime.now());
		product.setActive(false);
		productsDao.save(product);
		return new BasicRes(ReplyMessage.PRODUCT_DELETE_SUCCESS.getCode(), ReplyMessage.PRODUCT_DELETE_SUCCESS.getMessage());
	}

	// ── 分店庫存 ─────────────────────────────────────

	public BranchInventoryRes getBranchInventory(int areaId) {
		List<BranchInventory> inventories = branchInventoryDao.findByGlobalAreaId(areaId);

		List<BranchInventoryVO> voList = inventories.stream().map(inv -> {
			Optional<Products> opt = productsDao.findByIdAndDeletedAtIsNull(inv.getProductId());
			if (opt.isEmpty()) return null;
			Products p = opt.get();

			BranchInventoryVO vo = new BranchInventoryVO();
			vo.setId(inv.getId());
			vo.setProductId(inv.getProductId());
			vo.setProductName(p.getName());
			vo.setCategory(p.getCategory());
			vo.setGlobalAreaId(inv.getGlobalAreaId());
			vo.setStockQuantity(inv.getStockQuantity());
			vo.setBasePrice(inv.getBasePrice());
			vo.setMaxOrderQuantity(inv.getMaxOrderQuantity());
			vo.setVersion(inv.getVersion());
			return vo;
		}).filter(vo -> vo != null).collect(Collectors.toList());

		return new BranchInventoryRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage(), voList);
	}

	public BasicRes updateBranchInventory(UpdateBranchInventoryReq req) {
		Optional<BranchInventory> opt = branchInventoryDao
				.findByProductIdAndGlobalAreaId(req.getProductId(), req.getGlobalAreaId());
		if (opt.isEmpty()) {
			return new BasicRes(ReplyMessage.PRODUCT_NOT_FOUND.getCode(), ReplyMessage.PRODUCT_NOT_FOUND.getMessage());
		}
		BranchInventory inv = opt.get();

		if (req.getBasePrice() != null) {
			inv.setBasePrice(BigDecimal.valueOf(req.getBasePrice()));
		}
		if (req.getStockQuantity() != null) {
			inv.setStockQuantity(req.getStockQuantity());
		}
		if (req.getMaxOrderQuantity() != null && req.getMaxOrderQuantity() >= 1) {
			inv.setMaxOrderQuantity(req.getMaxOrderQuantity());
		}
		branchInventoryDao.save(inv);
		return new BasicRes(ReplyMessage.SUCCESS.getCode(), ReplyMessage.SUCCESS.getMessage());
	}

	// ── 私有工具 ─────────────────────────────────────

	private byte[] decodeImage(String base64) {
		if (base64 == null || base64.isBlank()) return null;
		try {
			String data = base64.contains(",") ? base64.split(",", 2)[1] : base64;
			return Base64.getDecoder().decode(data);
		} catch (Exception e) {
			return null;
		}
	}
}
